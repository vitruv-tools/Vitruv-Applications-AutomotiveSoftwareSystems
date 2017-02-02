package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for renaming an ASEM Named element which represents a SysML
 * NamedElement. <br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEAttribute} change.
 * 
 * @author Benjamin Rupp
 *
 */
public class NamedElementNameTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(NamedElementNameTransformation.class);

    public NamedElementNameTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        NamedElement namedElement = (NamedElement) change.getAffectedEObject();

        Named correspondingElement = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), namedElement, Named.class);

        if (correspondingElement == null) {
            logger.info("[ASEMSysML][Java] No corresponding elmement for NamedElement " + namedElement.getName()
                    + " found.");
            return;
        }

        if (correspondingElement instanceof Component) {
            // Component renaming must be handled specifically because their names are used as the
            // name of the ASEM models and therefore the models must be persisted with the new name.
            // See BlockNameTransformation class which handles this kind of transformation.
            return;
        }

        final EObject rootElement = EcoreUtil.getRootContainer(correspondingElement);

        if (!(rootElement instanceof Component)) {
            logger.info("[ASEMSysML][Java] Corresponding element of NamedElement " + namedElement.getName()
                    + " is not contained in a ASEM component.");
            return;
        }

        logger.info(
                "[ASEMSysML][Java] Rename corresponding element of NamedElement " + namedElement.getName() + " ...");

        correspondingElement.setName(change.getNewValue().toString());

        final Component component = (Component) rootElement;
        final String asemProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(component.getName());
        persistASEMElement(namedElement, correspondingElement, asemProjectModelPath);

    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (isNamedElementTheAffectedObject(change) && isNameTheAffectedAttribute(change)
                && isNotNameInitialization(change));
    }

    private boolean isNamedElementTheAffectedObject(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof NamedElement);
    }

    private boolean isNameTheAffectedAttribute(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedFeature().equals(UMLPackage.Literals.NAMED_ELEMENT__NAME));
    }

    private boolean isNotNameInitialization(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getOldValue() != null);
    }

}
