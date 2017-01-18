package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

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
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEReference} change which will
 * be triggered if the <code>type</code> of a SysML part property was set. <br>
 * <br>
 * 
 * [Requirement 1.e)]
 * 
 * @author Benjamin Rupp
 *
 */
public class PartTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PortDirectionTransformation.class);

    public PartTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEReference.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEReference<EObject, EObject> change) {

        logger.info("[ASEMSysML][Java] Transform part of a SysML block ...");

        org.eclipse.uml2.uml.Class blockBaseClass = (org.eclipse.uml2.uml.Class) change.getAffectedEObject()
                .eContainer();
        Property partProperty = (Property) change.getAffectedEObject();

        createASEMPartReference(blockBaseClass, partProperty);
    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEReference<EObject, EObject> change) {

        return (isProperty(change) && isPartProperty(change));
    }

    private boolean isProperty(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof Property);
    }

    private boolean isPartProperty(final ReplaceSingleValuedEReference<EObject, EObject> change) {

        boolean isContainingElementABlock = false;
        boolean isPropertyTypeABlock = false;
        boolean isPropertyAPort = false;

        final Property prop = (Property) change.getAffectedEObject();

        if (prop.eContainer() instanceof org.eclipse.uml2.uml.Class) {
            org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) prop.eContainer();

            if (baseClass.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {
                isContainingElementABlock = true;
            }
        }

        isPropertyTypeABlock = (prop.getType().getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null);

        isPropertyAPort = (prop instanceof Port);

        return (isContainingElementABlock && isPropertyTypeABlock && !isPropertyAPort);
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
            // TODO [BR] Show user feedback.
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
}
