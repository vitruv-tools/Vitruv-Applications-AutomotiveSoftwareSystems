package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming an added SysML property.
 * 
 * The transformation reacts on a {@link ReplaceSingleValuedEAttribute} change which will be
 * triggered if the <code>name</code> of a SysML property was set.
 * 
 * [Requirement 1.f)i][Requirement 2.g)i]
 * 
 * @author Benjamin Rupp
 *
 */
public class PropertyTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(PropertyTransformation.class);

    public PropertyTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        final Property property = (Property) change.getAffectedEObject();
        final org.eclipse.uml2.uml.Class propertyContainer = property.getClass_();

        logger.info("[ASEMSysML][Java] Transforming the SysML property " + property.getName() + "...");
        logger.info("[ASEMSysML][Java] Aggregation kind: " + property.getAggregation());

        Component correspondingContainer = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), propertyContainer, Component.class);

        Variable variable = DataexchangeFactory.eINSTANCE.createVariable();
        variable.setName(property.getName());

        correspondingContainer.getTypedElements().add(variable);

        final String asemModelName = ASEMSysMLHelper.getASEMModelName(propertyContainer.getName());
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        persistASEMElement(property, correspondingContainer, asemProjectModelPath);
        addCorrespondence(property, variable);
    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (isProperty(change) && isNotAPort(change) && isNotAPartReference(change) && nameHasChanged(change));
    }

    private boolean isProperty(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Property);
    }

    private boolean isNotAPort(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return !(change.getAffectedEObject() instanceof Port);
    }

    private boolean isNotAPartReference(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        Property property = (Property) change.getAffectedEObject();
        // TODO [BR] Use the aggregation kind 'COMPOSITE' to check, whether its a part reference or
        // not. But this information is not set at this moment in time.
        // return !(property.getAggregation().equals(AggregationKind.COMPOSITE));
        return !property.getName().contains("part");
    }

    private boolean nameHasChanged(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedFeature().equals(UMLPackage.Literals.NAMED_ELEMENT__NAME)
                && change.getOldValue() == null && change.getNewValue() != null);
    }

}
