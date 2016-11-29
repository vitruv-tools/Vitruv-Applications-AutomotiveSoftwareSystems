package tools.vitruv.applications.asemsysml.java.sysml2asem;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.ASEMPackage;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.CreatedSysMLBlockTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortTypeTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.DebugTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.PortDirectionTransformation;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.RenameSysMLBlock;
import tools.vitruv.applications.asemsysml.java.sysml2asem.util.Change2TransformationMap;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.processing.impl.AbstractEChangePropagationSpecification;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.util.command.ChangePropagationResult;
import tools.vitruv.framework.util.datatypes.MetamodelPair;

/**
 * Change executor for the SysML2ASEM transformation using java transformations. This class is
 * responsible for the mapping of a change and the java transformation which reacts to this change.
 * 
 * @author Benjamin Rupp
 * 
 */
public class SysML2ASEMJavaExecutor extends AbstractEChangePropagationSpecification {

    private Change2TransformationMap change2TransformationMap;
    private final MetamodelPair metamodelPair;

    /**
     * Create a new change executor with the given user interactor.
     * 
     * @param userInteracting
     *            The user interactor for the current session.
     */
    public SysML2ASEMJavaExecutor(final UserInteracting userInteracting) {
        super(userInteracting);
        this.change2TransformationMap = new Change2TransformationMap();
        this.metamodelPair = new MetamodelPair(UMLPackage.eNS_URI, ASEMPackage.eNS_URI);
        this.setup();
    }

    private void setup() {

        // Add the available java transformations.
        this.addTransformation(new DebugTransformation(this.getUserInteracting()));
        this.addTransformation(new CreatedSysMLBlockTransformation(this.getUserInteracting()));
        this.addTransformation(new RenameSysMLBlock(this.getUserInteracting()));
        this.addTransformation(new PortTransformation(this.getUserInteracting()));
        this.addTransformation(new PortDirectionTransformation(this.getUserInteracting()));
        this.addTransformation(new PortTypeTransformation(this.getUserInteracting()));

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
        final Set<JavaTransformationRealization> relevantTransformations = this.getRelevantTransformations(change);

        ChangePropagationResult currentResult;

        for (JavaTransformationRealization transformation : relevantTransformations) {

            currentResult = transformation.applyChange(change, correspondenceModel);

            // Add the result of each transformation to the overall propagation result.
            propagationResult.integrateResult(currentResult);
        }

        return propagationResult;
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
