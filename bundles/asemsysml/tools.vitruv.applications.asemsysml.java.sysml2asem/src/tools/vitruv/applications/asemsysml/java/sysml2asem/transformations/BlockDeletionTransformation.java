package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.io.IOException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.papyrus.sysml14.blocks.Block;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.compound.RemoveAndDeleteRoot;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the deletion of a SysML block. <br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link RemoveAndDeleteRoot} change.
 * 
 * @author Benjamin Rupp
 *
 */
public class BlockDeletionTransformation extends AbstractTransformationRealization<RemoveAndDeleteRoot<Block>> {

    private static Logger logger = Logger.getLogger(BlockDeletionTransformation.class);

    public BlockDeletionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return RemoveAndDeleteRoot.class;
    }

    @Override
    protected void executeTransformation(RemoveAndDeleteRoot<Block> change) {

        final Block block = change.getRemoveChange().getOldValue();
        final org.eclipse.uml2.uml.Class baseClass = block.getBase_Class();

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
    protected boolean checkPreconditions(RemoveAndDeleteRoot<Block> change) {
        return (affectedObjectIsBlock(change));
    }

    private boolean affectedObjectIsBlock(RemoveAndDeleteRoot<Block> change) {
        return change.getRemoveChange().getOldValue() instanceof Block;
    }
}
