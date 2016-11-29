package tools.vitruv.applications.asemsysml.java.sysml2asem.util;

import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.util.command.ChangePropagationResult;

/**
 * Represents the state of a transformation execution.
 * 
 * @author Benjamin Rupp
 *
 */
public class TransformationExecutionState {

    private final UserInteracting userInteracting;
    private final CorrespondenceModel correspondenceModel;
    private final ChangePropagationResult transformationResult;

    public TransformationExecutionState(final UserInteracting userInteracting,
            final CorrespondenceModel correspondenceModel, final ChangePropagationResult transformationResult) {
        this.userInteracting = userInteracting;
        this.correspondenceModel = correspondenceModel;
        this.transformationResult = transformationResult;
    }

    public UserInteracting getUserInteracting() {
        return this.userInteracting;
    }

    public CorrespondenceModel getCorrespondenceModel() {
        return this.correspondenceModel;
    }

    public ChangePropagationResult getTransformationResult() {
        return this.transformationResult;
    }

}
