package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
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
public class PortTransformation extends AbstractTransformationRealization {

    public PortTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(EChange untypedChange) {

        @SuppressWarnings("unchecked")
        ReplaceSingleValuedEAttribute<EObject, Object> change = (ReplaceSingleValuedEAttribute<EObject, Object>) untypedChange;
        
        System.out.println("[ASEMSysML][Java] Transforming a SysML port ...");
        createASEMMessageAndSetName(change);
    }

    @Override
    protected boolean checkPreconditions(EChange untypedChange) {

        @SuppressWarnings("unchecked")
        ReplaceSingleValuedEAttribute<EObject, Object> change = (ReplaceSingleValuedEAttribute<EObject, Object>) untypedChange;

        return (isPortOfABlock(change) && isPortNameSet(change));
    }

    private boolean isPortOfABlock(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        if (!(change.getAffectedEObject() instanceof Port)) {
            return false;
        }

        Port port = (Port) change.getAffectedEObject();

        if (!(port.getOwner() instanceof org.eclipse.uml2.uml.Class
                || port.getOwner().getAppliedStereotype("SysML::Blocks::Block") == null)) {
            return false;
        }

        return true;

    }

    private boolean isPortNameSet(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedFeature() == UMLPackage.Literals.NAMED_ELEMENT__NAME && change.getNewValue() != null);
    }

    private void createASEMMessageAndSetName(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        Port port = (Port) change.getAffectedEObject();
        Message message = DataexchangeFactory.eINSTANCE.createMessage();
        message.setName(port.getName());

        // Add ASEM message to ASEM module which corresponds to the block of the given port.
        Block block = ASEMSysMLHelper.getPortsBlock(port);
        String blockName = block.getBase_Class().getName();

        Module module = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(),
                block, Module.class);
        module.getTypedElements().add(message);

        // Persist module and add correspondence between port and message.
        String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION);

        persistASEMElement(port, module, asemProjectModelPath);
        addCorrespondence(port, message);
    }

}
