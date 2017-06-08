package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.RemoveEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for deleting an ASEM variable which corresponds to a SysML property.
 * 
 * The transformation reacts on a {@link RemoveEReference} change which will be triggered if a
 * <code>property</code> of a UML class was deleted.
 * 
 * @author Benjamin Rupp
 *
 */
public class PropertyDeletionTransformation
        extends AbstractTransformationRealization<RemoveEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PropertyAccessTransformation.class);

    public PropertyDeletionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return RemoveEReference.class;
    }

    @Override
    protected void executeTransformation(RemoveEReference<EObject, EObject> change) {

        final Property property = (Property) change.getOldValue();
        final Variable variable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), property, Variable.class);

        logger.info(
                "[ASEMSysML][Java] Delete variable corresponding to the SysML property " + property.getName() + "...");

        EcoreUtil.delete(variable);
        this.executionState.getCorrespondenceModel()
                .removeCorrespondencesThatInvolveAtLeastAndDependend(Collections.singleton(property));

    }

    @Override
    protected boolean checkPreconditions(RemoveEReference<EObject, EObject> change) {
        return (objectIsClass(change) && propertyWasRemoved(change) && propertyWasNotAPartReference(change));
    }

    private boolean objectIsClass(RemoveEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof org.eclipse.uml2.uml.Class);
    }

    private boolean propertyWasRemoved(RemoveEReference<EObject, EObject> change) {       
        return (change.getAffectedFeature().equals(UMLPackage.Literals.STRUCTURED_CLASSIFIER__OWNED_ATTRIBUTE)
                && change.getOldValue() instanceof Property && !(change.getOldValue() instanceof Port));
    }

    private boolean propertyWasNotAPartReference(RemoveEReference<EObject, EObject> change) {
        Property property = (Property) change.getOldValue();
        // TODO [BR] Use the aggregation kind 'COMPOSITE' to check, whether its a part reference or
        // not. But this information is not set at this moment in time.
        // return !(property.getAggregation().equals(AggregationKind.COMPOSITE));
        return !property.getName().contains("part");
    }

}
