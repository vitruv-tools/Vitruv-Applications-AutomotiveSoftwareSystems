package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.RemoveEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the deletion of a SysML part reference. <br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link RemoveEReference} change.
 * 
 * @author Benjamin Rupp
 *
 */
public class PartDeletionTransformation extends AbstractTransformationRealization<RemoveEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PartDeletionTransformation.class);

    public PartDeletionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return RemoveEReference.class;
    }

    @Override
    protected void executeTransformation(RemoveEReference<EObject, EObject> change) {

        Property partProperty = (Property) change.getOldValue();

        logger.info("[ASEMSysML][Java] Delete part reference to " + partProperty.getType().getName() + "...");

        Constant constant = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), partProperty, Constant.class);

        if (constant == null) {
            logger.info("[ASEMSysML][Java] No corresponding element for part property " + partProperty.getName()
                    + " found.");
            return;
        }

        EcoreUtil.delete(constant);

        this.executionState.getCorrespondenceModel()
                .removeCorrespondencesThatInvolveAtLeastAndDependend(Collections.singleton(partProperty));

    }

    @Override
    protected boolean checkPreconditions(RemoveEReference<EObject, EObject> change) {

        return (isAffectedObjectABlock(change) && isOldValueAProperty(change) && isPropertyTypeABlock(change)
                && isAggregationKindSetToComposite(change));
    }

    private boolean isAffectedObjectABlock(RemoveEReference<EObject, EObject> change) {
        if (!(change.getAffectedEObject() instanceof org.eclipse.uml2.uml.Class)) {
            return false;
        }
        org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) change.getAffectedEObject();

        return (UMLUtil.getStereotypeApplication(baseClass, Block.class) != null);
    }

    private boolean isOldValueAProperty(RemoveEReference<EObject, EObject> change) {
        return (change.getOldValue() instanceof Property);
    }

    private boolean isPropertyTypeABlock(RemoveEReference<EObject, EObject> change) {
        Property prop = (Property) change.getOldValue();
        return (prop.getType() != null
                && prop.getType().getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null);
    }

    private boolean isAggregationKindSetToComposite(RemoveEReference<EObject, EObject> change) {
        Property prop = (Property) change.getOldValue();
        return (prop.getAggregation() != null && prop.getAggregation().equals(AggregationKind.COMPOSITE_LITERAL));
    }

}
