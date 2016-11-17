package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import tools.vitruv.applications.asemsysml.java.sysml2asem.util.TransformationExecutionState;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.tuid.TuidManager;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.util.command.ChangePropagationResult;

/**
 * Abstract class for the java transformation implementations.
 * 
 * @author Benjamin Rupp
 *
 */
public abstract class AbstractTransformationRealization implements JavaTransformationRealization {

    protected final UserInteracting userInteracting;
    protected TransformationExecutionState executionState;

    public AbstractTransformationRealization(final UserInteracting userInteracting) {
        this.userInteracting = userInteracting;
    }

    /**
     * Execute the transformation for the given change. Use the {@link #executionState} variable to
     * store the transformation result. This information will be used in the
     * {@link #applyChange(EChange, CorrespondenceModel)} method. <br>
     * <br>
     * 
     * Here is an usage example: <br>
     * <code>
     * this.executionState.getTransformationResult().addRootEObjectToSave(eObjectToSave, eObjectVURI);
     * this.executionState.getTransformationResult().addVuriToDeleteIfNotNull(oldVURI);
     * </code>
     * 
     * @param change
     *            The change which have to be applied.
     * 
     * @see TransformationExecutionState
     * @see ChangePropagationResult
     */
    protected abstract void executeTransformation(final EChange change);

    @Override
    public boolean doesHandleChange(final EChange change) {
        return (isValidChangeType(change.getClass()) && checkPreconditions(change));
    }

    private boolean isValidChangeType(final Class<? extends EChange> changeType) {
        return getExpectedChangeType().isAssignableFrom(changeType);
    }

    /**
     * Check if the java transformation fulfills the preconditions for the given change. This method
     * will only be called if the change type is a valid change type. Valid change types can be
     * defined using the {@link #getExpectedChangeType()} method.
     * 
     * @param change
     *            The change for which the preconditions have to be fulfilled.
     * @return <code>True</code> if the preconditions are fulfilled, otherwise <code>false</code>.
     */
    protected abstract boolean checkPreconditions(final EChange change);

    @Override
    public ChangePropagationResult applyChange(EChange change, CorrespondenceModel correspondenceModel) {

        this.executionState = new TransformationExecutionState(userInteracting, correspondenceModel,
                new ChangePropagationResult());

        if (doesHandleChange(change)) {
            try {
                executeTransformation(change);
            } finally {
                /*
                 * The transformation was completely executed, so remove all objects registered for
                 * modification as they are no longer under modification even if there was an
                 * exception!
                 */
                TuidManager.getInstance().flushRegisteredObjectsUnderModification();
            }

        }

        return executionState.getTransformationResult();
    }
}
