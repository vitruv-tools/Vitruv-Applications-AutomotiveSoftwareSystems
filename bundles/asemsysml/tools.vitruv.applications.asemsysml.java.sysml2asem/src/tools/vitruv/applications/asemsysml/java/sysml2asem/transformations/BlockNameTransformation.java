package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.tuid.TuidManager;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for renaming the ASEM element which represents a SysML block. <br>
 * <br>
 * 
 * After changing the name of a SysML block, the name of the corresponding ASEM element will be
 * changed, too. After that, the element will be persisted using the new name. The old resource will
 * be deleted.
 * 
 * @author Benjamin Rupp
 *
 */
public class BlockNameTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(BlockNameTransformation.class);

    public BlockNameTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) change.getAffectedEObject();
        Block block = UMLUtil.getStereotypeApplication(baseClass, Block.class);

        logger.info(
                "[ASEMSysML][Java] Transform a block name change of block " + block.getBase_Class().getName() + " ...");

        changeNameOfCorrespondingASEMElement(block, (String) change.getNewValue());

    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        boolean preconditionsFulfilled = (isBlockTheAffectedObject(change) && isNameTheAffectedAttribute(change));

        return preconditionsFulfilled;
    }

    private boolean isBlockTheAffectedObject(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        boolean isBlock = false;

        if (change.getAffectedEObject() instanceof Block) {
            isBlock = true;
        }

        if (change.getAffectedEObject() instanceof org.eclipse.uml2.uml.Class) {
            org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) change.getAffectedEObject();

            if (baseClass.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {
                isBlock = true;
            }
        }

        return isBlock;

    }

    private boolean isNameTheAffectedAttribute(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        boolean isName = false;

        if (change.getAffectedFeature().getName().equals("name")) {
            isName = true;
        }

        return isName;
    }

    private void changeNameOfCorrespondingASEMElement(final Block block, final String newName) {

        Component asemComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), block, Component.class);

        TuidManager.getInstance().registerObjectUnderModification(asemComponent);

        asemComponent.setName(newName);

        final String asemModelName = ASEMSysMLHelper.getASEMModelName(newName);
        persistASEMElement(block, asemComponent,
                ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION));

        TuidManager.getInstance().updateTuidsOfRegisteredObjects();
        TuidManager.getInstance().flushRegisteredObjectsUnderModification();

    }
}
