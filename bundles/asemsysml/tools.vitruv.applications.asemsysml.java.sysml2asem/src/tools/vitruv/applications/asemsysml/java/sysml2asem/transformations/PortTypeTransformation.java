package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the type of a port.<br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEReference} change which will
 * be triggered if the <code>type</code> of a port was set. <br>
 * <br>
 * 
 * [Requirement 1.d)iii]
 * 
 * @author Benjamin Rupp
 *
 */
public class PortTypeTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PortTypeTransformation.class);

    public PortTypeTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEReference.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEReference<EObject, EObject> change) {

        logger.info("[ASEMSysML][JAVA] Transforming port type ...");

        Port port = (Port) change.getAffectedEObject();
        Type portType = port.getType();
        Classifier messageType = getClassifierForASEMMessage(portType);

        // Set message type.
        Message message = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(),
                port, Message.class);
        message.setType(messageType);

        // Persist message.
        EObject messageContainer = message.eContainer();

        if (!(messageContainer instanceof Module)) {
            throw new IllegalArgumentException("The eContainer of the message object is not an instance of Module!");
        }

        final Module module = (Module) messageContainer;
        final String blockName = ASEMSysMLHelper.getPortsBlock(port).getBase_Class().getName();
        final String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        persistASEMElement(port, module, asemProjectModelPath);

    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (isPort(change) && isPortTypeSet(change));
    }

    private boolean isPort(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof Port);
    }

    private boolean isPortTypeSet(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedFeature().equals(UMLPackage.Literals.TYPED_ELEMENT__TYPE));
    }

    private Classifier getClassifierForASEMMessage(final Type portType) {

        if (portType instanceof org.eclipse.uml2.uml.Class) {
            org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) portType;
            Block portTypeBlock = UMLUtil.getStereotypeApplication(baseClass, Block.class);

            Component correspondingComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                    this.executionState.getCorrespondenceModel(), portTypeBlock, Component.class);

            return correspondingComponent;

        } else if (portType instanceof PrimitiveType) {

            final PrimitiveType primitivePortType = (PrimitiveType) portType;

            final Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> primitiveMessageType;
            primitiveMessageType = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(primitivePortType);

            final edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType messageType = ASEMSysMLPrimitiveTypeHelper
                    .getASEMPrimitiveTypeFromRepository(primitiveMessageType, portType);

            return messageType;
        }

        return null;

    }
}
