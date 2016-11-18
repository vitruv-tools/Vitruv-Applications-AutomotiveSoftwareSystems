package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.ClassifiersFactory;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming a added SysML block.<br>
 * <br>
 * 
 * The transformation reacts on a {@link ReplaceSingleValuedEAttribute} change which will be
 * triggered if the <code>isEncapsulated</code> flag of a SysML block was set. This is necessary
 * because the corresponding ASEM component will only be generated if this flag is set to
 * <code>true</code>. <br>
 * <br>
 * 
 * [Requirement 1.b)][Requirement 2.b)]
 * 
 * @see Block
 * 
 * @author Benjamin Rupp
 *
 */
public class CreatedSysMLBlockTransformation extends AbstractTransformationRealization {

    public CreatedSysMLBlockTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkPreconditions(EChange untypedChange) {

        boolean preconditionsFulfilled = false;

        preconditionsFulfilled = isEncapsulatedFlagSet((ReplaceSingleValuedEAttribute<EObject, Object>) untypedChange);

        return preconditionsFulfilled;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeTransformation(EChange change) {

        System.out.println("[ASEMSysML][Java] Transforming a SysML Block ...");

        createASEMModule((ReplaceSingleValuedEAttribute<EObject, Object>) change);

    }

    private boolean isEncapsulatedFlagSet(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Block
                && change.getAffectedFeature() == BlocksPackage.Literals.BLOCK__IS_ENCAPSULATED
                && (Boolean) change.getNewValue());
    }

    private void createASEMModule(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        Block block = (Block) change.getAffectedEObject();

        Module module = ClassifiersFactory.eINSTANCE.createModule();
        module.setName(block.getBase_Class().getName());

        String asemModelName = ASEMSysMLHelper.getASEMModelName(block.getBase_Class().getName());
        String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION);

        persistASEMElement(block, module, asemProjectModelPath);
        addCorrespondence(block, module);

    }
}
