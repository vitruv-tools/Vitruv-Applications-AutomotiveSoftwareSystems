package tools.vitruv.applications.asemsysml.java.sysml2asem.global;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.ASEMPackage;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.BlockTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortTypeTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PrimitiveTypeTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.DebugTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.NamedElementNameTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PartDeletionTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PartTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortDeletionTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortDirectionTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.JavaTransformationRealization;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.BlockDeletionTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.BlockNameTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.util.Change2TransformationMap;
import tools.vitruv.framework.change.echange.AtomicEChange;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.compound.CompoundEChange;
import tools.vitruv.framework.change.processing.impl.AbstractEChangePropagationSpecification;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.userinteraction.impl.UserInteractor;
import tools.vitruv.framework.util.command.ChangePropagationResult;
import tools.vitruv.framework.util.datatypes.MetamodelPair;

/**
 * Change propagation specification for the SysML2ASEM transformation using java transformations.
 * This class is responsible for the mapping of a change and the java transformation which reacts to
 * this change.
 * 
 * @author Benjamin Rupp
 * 
 */
public class SysML2ASEMJavaChangePropagationSpecification extends AbstractEChangePropagationSpecification {

    private Change2TransformationMap change2TransformationMap;
    private final MetamodelPair metamodelPair;

    /**
     * Create a new change executor with the given user interactor.
     *
     */
    public SysML2ASEMJavaChangePropagationSpecification() {
        super(new UserInteractor());
        this.change2TransformationMap = new Change2TransformationMap();
        this.metamodelPair = new MetamodelPair(UMLPackage.eNS_URI, ASEMPackage.eNS_URI);
        this.setup();
    }

    private void setup() {

        // Add the available java transformations.
        this.addTransformation(new DebugTransformation(this.getUserInteracting()));
        this.addTransformation(new BlockTransformation(this.getUserInteracting()));
        this.addTransformation(new BlockNameTransformation(this.getUserInteracting()));
        this.addTransformation(new BlockDeletionTransformation(this.getUserInteracting()));
        this.addTransformation(new PortTransformation(this.getUserInteracting()));
        this.addTransformation(new PortDirectionTransformation(this.getUserInteracting()));
        this.addTransformation(new PortTypeTransformation(this.getUserInteracting()));
        this.addTransformation(new PortDeletionTransformation(this.getUserInteracting()));
        this.addTransformation(new PrimitiveTypeTransformation(this.getUserInteracting()));
        this.addTransformation(new PartTransformation(this.getUserInteracting()));
        this.addTransformation(new PartDeletionTransformation(this.getUserInteracting()));
        this.addTransformation(new NamedElementNameTransformation(this.getUserInteracting()));

    }

    @Override
    public MetamodelPair getMetamodelPair() {
        return this.metamodelPair;
    }

    @Override
    protected boolean doesHandleChange(EChange change, CorrespondenceModel correspondenceModel) {

        final Set<JavaTransformationRealization> relevantTransformations = this.getRelevantTransformations(change);
        final Boolean handleChange = !relevantTransformations.isEmpty();

        return handleChange;
    }

    @Override
    protected ChangePropagationResult propagateChange(EChange change, CorrespondenceModel correspondenceModel) {

        final ChangePropagationResult propagationResult = new ChangePropagationResult();

        if (change instanceof CompoundEChange) {
            for (AtomicEChange atomicChange : ((CompoundEChange) change).getAtomicChanges()) {
                propagationResult.integrateResult(propagateChange(atomicChange, correspondenceModel));
            }
        }

        final Set<JavaTransformationRealization> relevantTransformations = this.getRelevantTransformations(change);

        ChangePropagationResult currentResult;

        for (JavaTransformationRealization transformation : relevantTransformations) {

            currentResult = transformation.applyChange(change, correspondenceModel);

            // Add the result of each transformation to the overall propagation result.
            propagationResult.integrateResult(currentResult);
        }

        return propagationResult;
    }

    @Override
    public void setUserInteracting(UserInteracting userInteracting) {
        /*
         * If the user interacting has changed, set the new user interacting, clean the change to
         * transformation map and initialize the java transformation classes again.
         */
        super.setUserInteracting(userInteracting);

        this.change2TransformationMap = new Change2TransformationMap();
        this.setup();
    }

    private Set<JavaTransformationRealization> getRelevantTransformations(final EChange change) {

        Set<JavaTransformationRealization> relevantTransformations = new HashSet<JavaTransformationRealization>();
        final Set<JavaTransformationRealization> transformations = this.change2TransformationMap
                .getJavaTransformations(change);

        for (JavaTransformationRealization transformation : transformations) {

            if (transformation.doesHandleChange(change)) {
                relevantTransformations.add(transformation);
            }
        }

        return relevantTransformations;
    }

    private void addTransformation(final JavaTransformationRealization transformation) {

        this.change2TransformationMap.addJavaTransformation(transformation.getExpectedChangeType(), transformation);

    }

}
