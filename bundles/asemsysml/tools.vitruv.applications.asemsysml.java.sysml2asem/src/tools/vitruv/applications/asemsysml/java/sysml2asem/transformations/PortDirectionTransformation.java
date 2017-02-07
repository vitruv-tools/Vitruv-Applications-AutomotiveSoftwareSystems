package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.BindingConnector;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;

import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper.ASEMParameterMode;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the direction of a SysML port.<br>
 * <br>
 * 
 * To transform the port direction the following elements must exist:
 * <ul>
 * <li>{@link FlowProperty} with a valid direction</li>
 * <li>{@link BindingConnector} which links the flow property to the given port</li>
 * </ul>
 * 
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEReference} change which will
 * be triggered if the <code>base connector</code> of a SysML binding connector was set. <br>
 * <br>
 * 
 * [Requirement 1.d)ii] [Requirement 2.d)iii] [Requirement 2.e)ii]
 * 
 * @author Benjamin Rupp
 *
 */
public class PortDirectionTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PortDirectionTransformation.class);

    public PortDirectionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEReference.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEReference<EObject, EObject> change) {

        logger.info("[ASEMSysML][Java] Transform direction of a SysML port ...");

        transformPortDirection(change);
    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (isBindingConnector(change) && isConnectorReferenceSet(change));
    }

    private void transformPortDirection(ReplaceSingleValuedEReference<EObject, EObject> change) {

        final BindingConnector bindingConnector = (BindingConnector) change.getAffectedEObject();
        final Connector connector = bindingConnector.getBase_Connector();
        EList<ConnectorEnd> connectorEnds = connector.getEnds();

        Port port = null;

        for (ConnectorEnd connectorEnd : connectorEnds) {
            if (connectorEnd.getRole() instanceof Port) {
                port = (Port) connectorEnd.getRole();
            }
        }

        if (port == null) {
            logger.warn("[ASEMSysML][Java] No port was found in the list of the connector ends. "
                    + "ASEM message access properties are NOT set!");
            return;
        }

        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final String blockName = block.getBase_Class().getName();
        final String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        final Component correspondingComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), block, Component.class);

        if (correspondingComponent instanceof Module) {

            setMessageAccessParameters(port, asemProjectModelPath);

        } else if (correspondingComponent instanceof edu.kit.ipd.sdq.ASEM.classifiers.Class) {

            edu.kit.ipd.sdq.ASEM.classifiers.Class asemClass = (edu.kit.ipd.sdq.ASEM.classifiers.Class) correspondingComponent;
            createASEMMethodAndSetName(port, asemClass, asemProjectModelPath);

        }

    }

    private boolean isBindingConnector(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof BindingConnector);
    }

    private boolean isConnectorReferenceSet(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedFeature().equals(BlocksPackage.Literals.BINDING_CONNECTOR__BASE_CONNECTOR));
    }

    private void setMessageAccessParameters(final Port port, final String asemProjectModelPath) {

        Message message = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(),
                port, Message.class);

        if (message == null) {
            logger.warn("[ASEMSysML][Java] No corresponding ASEM message found for UML port " + port.getName());
            return;
        }

        // Get ports flow direction.
        final FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);
        if (flowProperty == null) {
            logger.warn("[ASEMSysML][Java] There is no flow property for port " + port.getName() + " available.");
            return;
        }
        final FlowDirection flowDirection = flowProperty.getDirection();

        // Set access parameters of ASEM message.
        switch (flowDirection) {
        case IN:
            message.setReadable(true);
            message.setWritable(false);
            break;
        case OUT:
            message.setReadable(false);
            message.setWritable(true);
            break;
        case INOUT:
            message.setReadable(true);
            message.setWritable(true);
            break;

        default:
            break;
        }

        // Persist message.
        EObject messageContainer = message.eContainer();

        if (!(messageContainer instanceof Module)) {
            throw new IllegalArgumentException("The eContainer of the message object is not an instance of Module!");
        }

        final Module module = (Module) messageContainer;

        persistASEMElement(port, module, asemProjectModelPath);
    }

    private void createASEMMethodAndSetName(final Port port,
            final edu.kit.ipd.sdq.ASEM.classifiers.Class correspondingASEMClass, final String asemProjectModelPath) {

        Method method;
        ASEMParameterMode mode = ASEMParameterMode.CREATE_NEW;
        Resource asemResource = ASEMSysMLHelper.getModelResource(this.executionState.getCorrespondenceModel(), port,
                asemProjectModelPath);

        if (ASEMSysMLHelper.areMethodsAvailable(asemResource)) {

            mode = ASEMSysMLUserInteractionHelper.selectASEMParameterMode(this.userInteracting);

        }

        logger.info("[ASEMSysML][Java] Selected ASEM parameter mode: " + mode);

        method = this.getMethodDependingOnParamterMode(mode, port, asemProjectModelPath);

        FlowProperty flowProp = ASEMSysMLHelper.getFlowProperty(port);
        FlowDirection direction = flowProp != null ? flowProp.getDirection() : null;
        Classifier type = ASEMSysMLHelper.getClassifierForASEMVariable(port.getType(),
                this.executionState.getCorrespondenceModel());

        switch (direction) {
        case IN:

            Parameter parameter = DataexchangeFactory.eINSTANCE.createParameter();
            parameter.setName(port.getName());
            parameter.setType(type);
            method.getParameters().add(parameter);

            correspondingASEMClass.getMethods().add(method);

            persistASEMElement(port, correspondingASEMClass, asemProjectModelPath);
            addCorrespondence(port, parameter);
            break;

        case OUT:

            ReturnType returnType = DataexchangeFactory.eINSTANCE.createReturnType();
            returnType.setType(type);
            method.setReturnType(returnType);

            correspondingASEMClass.getMethods().add(method);
            persistASEMElement(port, correspondingASEMClass, asemProjectModelPath);

            addCorrespondence(port, returnType);
            break;

        case INOUT:

            logger.warn("INOUT ports are not mapped to ASEM models at the moment!");

        default:
            break;
        }

    }

    private Method getMethodDependingOnParamterMode(final ASEMParameterMode mode, final Port port,
            final String asemProjectModelPath) {
        switch (mode) {

        case CREATE_NEW:
            return this.createNewMethod();

        case USE_EXISTING:
            return this.selectMethod(port, asemProjectModelPath);

        default:
            return null;
        }
    }

    private Method selectMethod(final EObject alreadyPersistedObject, final String asemProjectModelPath) {

        Resource asemResource = ASEMSysMLHelper.getModelResource(this.executionState.getCorrespondenceModel(),
                alreadyPersistedObject, asemProjectModelPath);
        Method selectedMethod = ASEMSysMLUserInteractionHelper.selectASEMMethod(this.userInteracting, asemResource);

        return selectedMethod;
    }

    private Method createNewMethod() {

        String methodName = this.userInteracting.getTextInput(ASEMSysMLUserInteractionHelper.MSG_INSERT_METHOD_NAME);

        if (methodName.isEmpty()) {
            methodName = "SampleMethodName";
        }

        Method method = DataexchangeFactory.eINSTANCE.createMethod();
        method.setName(methodName);

        return method;
    }

}
