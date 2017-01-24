package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * 
 * The transformation class for transforming a added SysML port.<br>
 * <br>
 * 
 * The transformation reacts on a {@link ReplaceSingleValuedEAttribute} change which will be
 * triggered if the <code>name</code> of a SysML port was set. The transformation will <b>not</b>
 * handle the port direction (the direction of the ports flow property). To handle the port
 * direction the {@link PortDirectionTransformation} is used.<br>
 * <br>
 * 
 * [Requirement 1.d)i]
 * 
 * @author Benjamin Rupp
 *
 */
public class PortTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(PortTransformation.class);

    public PortTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        logger.info("[ASEMSysML][Java] Transforming a SysML port ...");

        final Port port = (Port) change.getAffectedEObject();
        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final String blockName = block.getBase_Class().getName();
        final String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        Component correspondingASEMComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), block, Component.class);

        if (correspondingASEMComponent instanceof Module) {

            Module asemModule = (Module) correspondingASEMComponent;
            createASEMMessageAndSetName(port, asemModule, asemProjectModelPath);

        } else if (correspondingASEMComponent instanceof edu.kit.ipd.sdq.ASEM.classifiers.Class) {

            // In this case, the mapping of the port is direction specific. Therefore the
            // transformation will be handled in the PortDirectionTransformation class.
            return;

        } else {
            throw new IllegalArgumentException(
                    "Unhandled ASEM component type of the ASEM element which corresponds to the SysML block "
                            + block.getBase_Class().getName());
        }

    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (isPortOfABlock(change) && isPortNameSet(change));
    }

    private boolean isPortOfABlock(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        if (!(change.getAffectedEObject() instanceof Port)) {
            return false;
        }

        Port port = (Port) change.getAffectedEObject();

        if (!(port.getOwner() instanceof org.eclipse.uml2.uml.Class
                || port.getOwner().getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) == null)) {
            return false;
        }

        return true;

    }

    private boolean isPortNameSet(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedFeature() == UMLPackage.Literals.NAMED_ELEMENT__NAME && change.getNewValue() != null);
    }

    private void createASEMMessageAndSetName(final Port port, final Module correspondingASEMModule,
            final String asemProjectModelPath) {

        Message message = DataexchangeFactory.eINSTANCE.createMessage();
        message.setName(port.getName());

        correspondingASEMModule.getTypedElements().add(message);

        persistASEMElement(port, correspondingASEMModule, asemProjectModelPath);
        addCorrespondence(port, message);
    }

}
