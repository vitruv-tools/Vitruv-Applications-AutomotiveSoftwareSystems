package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.io.IOException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.RemoveEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the deletion of a SysML block. <br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link RemoveEReference} change.
 * 
 * @author Benjamin Rupp
 *
 */
public class BlockDeletionTransformation extends AbstractTransformationRealization<RemoveEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(BlockDeletionTransformation.class);

    public BlockDeletionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return RemoveEReference.class;
    }

    @Override
    protected void executeTransformation(RemoveEReference<EObject, EObject> change) {

        final org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) change.getOldValue();
        final Block block = UMLUtil.getStereotypeApplication(baseClass, Block.class);

        logger.info("[ASEMSysML][Java] Delete ASEM component which corresponds to the SysML block "
                + baseClass.getName() + " ...");

        final Component component = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), block, Component.class);

        if (component == null) {
            logger.info("[ASEMSysML][Java] No corresponding element for block " + baseClass.getName() + "found.");
            return;
        }

        try {

            component.eResource().delete(null);

        } catch (IOException e) {
            logger.warn("Could not delete ASEM model resource for " + component.getName() + "!");
        }

        this.executionState.getCorrespondenceModel()
                .removeCorrespondencesThatInvolveAtLeastAndDependend(Collections.singleton(block));

    }

    @Override
    protected boolean checkPreconditions(RemoveEReference<EObject, EObject> change) {
        return (affectedObjectIsModel(change) && deletedElementWasABlock(change));
    }

    private boolean affectedObjectIsModel(RemoveEReference<EObject, EObject> change) {
        return change.getAffectedEObject() instanceof Model;
    }

    private boolean deletedElementWasABlock(RemoveEReference<EObject, EObject> change) {

        if (!(change.getOldValue() instanceof org.eclipse.uml2.uml.Class)) {
            return false;
        }

        org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) change.getOldValue();

        return baseClass.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null;
    }
}
