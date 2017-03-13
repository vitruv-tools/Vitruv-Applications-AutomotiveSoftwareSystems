package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.userinteraction.UserInteractionType;

/**
 * The transformation class for transforming the direction of a SysML part. <br>
 * <br>
 * 
 * To transform a part the following elements must exist:
 * <ul>
 * <li>part name</li>
 * <li>part type</li>
 * </ul>
 * 
 * A property is a part property if
 * <ul>
 * <li>the containing element of the property is a block</li>
 * <li>the type of the property is a block</li>
 * <li>the property is not a port</li>
 * </ul>
 * 
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEAttribute} change which will
 * be triggered if the <code>aggregation kind</code> of a SysML part property was set. <br>
 * <br>
 * 
 * [Requirement 1.e)]
 * 
 * @author Benjamin Rupp
 *
 */
public class PartTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(PortDirectionTransformation.class);

    public PartTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        logger.info("[ASEMSysML][Java] Transform part of a SysML block ...");

        EObject container = change.getAffectedEObject().eContainer();

        if (!(container instanceof org.eclipse.uml2.uml.Class)) {
            throw new IllegalArgumentException(
                    "The eContainer of the change object is not an instance of org.eclipse.uml2.uml.Class!");
        }

        org.eclipse.uml2.uml.Class blockBaseClass = (org.eclipse.uml2.uml.Class) container;
        Property partProperty = (Property) change.getAffectedEObject();

        createASEMPartReference(blockBaseClass, partProperty);
    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        return (isProperty(change) && isNotAPort(change) && isPropertyTypeSet(change)
                && isPartProperty((Property) change.getAffectedEObject()));
    }

    private boolean isProperty(final ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Property);
    }

    private boolean isNotAPort(final ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return !(change.getAffectedEObject() instanceof Port);
    }

    private boolean isPropertyTypeSet(final ReplaceSingleValuedEAttribute<EObject, Object> change) {
        final Property property = (Property) change.getAffectedEObject();
        return (property.getType() != null);
    }

    private void createASEMPartReference(final org.eclipse.uml2.uml.Class blockBaseClass, final Property partProperty) {

        Block parentBlock = UMLUtil.getStereotypeApplication(blockBaseClass, Block.class);
        Block partBlock = UMLUtil.getStereotypeApplication(partProperty.getType(), Block.class);

        Component correspondingASEMBlockComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), parentBlock, Component.class);
        Component correspondingASEMPartComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), partBlock, Component.class);

        // ASEM modules cannot be used as subcomponents in ASEM components.
        if (correspondingASEMPartComponent instanceof Module) {
            userInteracting.showMessage(UserInteractionType.MODAL,
                    ASEMSysMLUserInteractionHelper.MSG_WARN_MODULE_AS_SUBCOMPONENT);
            return;
        }

        Constant asemConstant = DataexchangeFactory.eINSTANCE.createConstant();
        asemConstant.setName(partProperty.getName());
        asemConstant.setType(correspondingASEMPartComponent);

        correspondingASEMBlockComponent.getTypedElements().add(asemConstant);

        // Persist component which corresponds to the SysML block and add correspondence between
        // part property and constant.
        String asemModelName = ASEMSysMLHelper.getASEMModelName(blockBaseClass.getName());
        String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION);

        persistASEMElement(blockBaseClass, correspondingASEMBlockComponent, asemProjectModelPath);
        addCorrespondence(partProperty, asemConstant);

    }

    private boolean isPartProperty(final Property property) {

        boolean isContainingElementABlock = false;
        boolean isPropertyTypeABlock = false;
        boolean isAggregationKindSetToComposite = false;

        if (property.eContainer() instanceof org.eclipse.uml2.uml.Class) {
            org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) property.eContainer();

            if (baseClass.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {
                isContainingElementABlock = true;
            }
        }

        isPropertyTypeABlock = (property.getType()
                .getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null);

        /*
         * Check the aggregation kind of the property because the aggregation kind must be set for
         * the getParts() method of a SysML block. Furthermore this check prevents the handling of a
         * port property.
         */
        final AggregationKind aggregationKind = property.getAggregation();
        isAggregationKindSetToComposite = (aggregationKind != null
                && aggregationKind.equals(AggregationKind.COMPOSITE_LITERAL));

        return (isContainingElementABlock && isPropertyTypeABlock && isAggregationKindSetToComposite);
    }
}
