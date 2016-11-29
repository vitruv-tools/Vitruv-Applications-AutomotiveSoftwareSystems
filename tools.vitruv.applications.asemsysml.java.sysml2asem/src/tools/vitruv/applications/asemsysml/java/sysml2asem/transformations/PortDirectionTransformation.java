package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.BindingConnector;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;

import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
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
 * [Requirement 1.d)ii]
 * 
 * @author Benjamin Rupp
 *
 */
public class PortDirectionTransformation extends AbstractTransformationRealization {

    private final Logger logger = Logger.getLogger(PortDirectionTransformation.class);

    public PortDirectionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEReference.class;
    }

    @Override
    protected void executeTransformation(EChange untypedChange) {

        @SuppressWarnings("unchecked")
        ReplaceSingleValuedEReference<EObject, EObject> change = (ReplaceSingleValuedEReference<EObject, EObject>) untypedChange;

        System.out.println("[ASEMSysML][Java] Transform direction of a SysML port ...");
        setMessageAccess(change);
    }

    @Override
    protected boolean checkPreconditions(EChange untypedChange) {

        @SuppressWarnings("unchecked")
        ReplaceSingleValuedEReference<EObject, EObject> change = (ReplaceSingleValuedEReference<EObject, EObject>) untypedChange;

        return (isBindingConnector(change) && isConnectorReferenceSet(change));
    }

    private void setMessageAccess(ReplaceSingleValuedEReference<EObject, EObject> change) {

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

        Message message = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(),
                port, Message.class);

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
        final Module module = (Module) message.eContainer();
        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final String blockName = block.getBase_Class().getName();
        final String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        persistASEMElement(port, module, asemProjectModelPath);

    }

    private boolean isBindingConnector(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof BindingConnector);
    }

    private boolean isConnectorReferenceSet(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedFeature().equals(BlocksPackage.Literals.BINDING_CONNECTOR__BASE_CONNECTOR));
    }
}
