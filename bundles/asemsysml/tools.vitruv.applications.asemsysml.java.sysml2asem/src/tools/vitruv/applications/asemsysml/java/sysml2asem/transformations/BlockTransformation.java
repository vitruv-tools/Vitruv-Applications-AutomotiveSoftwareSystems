package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.ClassifiersFactory;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
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
public class BlockTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(BlockTransformation.class);

    public BlockTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    public boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        boolean preconditionsFulfilled = false;

        preconditionsFulfilled = isEncapsulatedFlagSet(change);

        return preconditionsFulfilled;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        logger.info("[ASEMSysML][Java] Transforming a SysML Block ...");

        createASEMComponent(change);

    }

    private boolean isEncapsulatedFlagSet(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Block
                && change.getAffectedFeature() == BlocksPackage.Literals.BLOCK__IS_ENCAPSULATED
                && (Boolean) change.getNewValue());
    }

    private void createASEMComponent(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        Block block = (Block) change.getAffectedEObject();

        Class<?> asemComponentType = ASEMSysMLUserInteractionHelper.selectASEMComponentType(this.userInteracting);

        Component asemComponent;

        if (Module.class.isAssignableFrom(asemComponentType)) {

            Module asemModule = ClassifiersFactory.eINSTANCE.createModule();
            asemModule.setName(block.getBase_Class().getName());
            asemComponent = asemModule;

        } else if (edu.kit.ipd.sdq.ASEM.classifiers.Class.class.isAssignableFrom(asemComponentType)) {

            edu.kit.ipd.sdq.ASEM.classifiers.Class asemClass = ClassifiersFactory.eINSTANCE.createClass();
            asemClass.setName(block.getBase_Class().getName());
            asemComponent = asemClass;

        } else {
            return;
        }

        String asemModelName = ASEMSysMLHelper.getASEMModelName(block.getBase_Class().getName());
        String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION);

        persistASEMElement(block, asemComponent, asemProjectModelPath);
        addCorrespondence(block, asemComponent);

    }
}
