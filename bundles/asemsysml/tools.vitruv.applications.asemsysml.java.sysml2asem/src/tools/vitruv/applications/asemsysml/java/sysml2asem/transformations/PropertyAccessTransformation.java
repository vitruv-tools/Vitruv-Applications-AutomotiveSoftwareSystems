package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.change.echange.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming an added SysML property.
 * 
 * The transformation reacts on a {@link ReplaceSingleValuedEReference} change which will be
 * triggered if the <code>type</code> of a SysML property was set.
 * 
 * [Requirement 1.f)iii][Requirement 2.g)iii]
 * 
 * @author Benjamin Rupp
 *
 */
public class PropertyAccessTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEAttribute<EObject, Object>> {

    private static Logger logger = Logger.getLogger(PropertyAccessTransformation.class);

    public PropertyAccessTransformation(UserInteracting userInteracting) {
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
        final Variable variable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), property, Variable.class);

        logger.info("[ASEMSysML][Java] Set the access parameters of the variable corresponding to the SysML property "
                + property.getName() + "...");

        variable.setReadable(true);
        variable.setWritable(!property.isReadOnly());

        final String asemModelName = ASEMSysMLHelper.getASEMModelName(propertyContainer.getName());
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        persistASEMElement(property, variable, asemProjectModelPath);
        addCorrespondence(property, variable);
    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (isProperty(change) && isNotAPartReference(change) && readOnlyChanged(change));
    }

    private boolean isProperty(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Property && !(change.getAffectedEObject() instanceof Port));
    }

    private boolean readOnlyChanged(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedFeature().equals(UMLPackage.Literals.STRUCTURAL_FEATURE__IS_READ_ONLY));
    }

    private boolean isNotAPartReference(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        Property property = (Property) change.getAffectedEObject();
        // TODO [BR] Use the aggregation kind 'COMPOSITE' to check, whether its a part reference or
        // not. But this information is not set at this moment in time.
        // return !(property.getAggregation().equals(AggregationKind.COMPOSITE));
        return !property.getName().contains("part");
    }

}
