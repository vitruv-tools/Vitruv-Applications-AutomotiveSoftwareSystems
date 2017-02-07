package tools.vitruv.applications.asemsysml.java.sysml2asem;

import java.util.Collections;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import edu.kit.ipd.sdq.ASEM.base.Named;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.util.TransformationExecutionState;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.tuid.TuidManager;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.util.command.ChangePropagationResult;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * Abstract class for the java transformation implementations.
 * 
 * @author Benjamin Rupp
 * 
 * @param <T>
 *            Change type the transformation reacts to.
 *
 */
public abstract class AbstractTransformationRealization<T extends EChange> implements JavaTransformationRealization {

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
    protected abstract void executeTransformation(final T change);

    /**
     * Check if the java transformation fulfills the preconditions for the given change. This method
     * will only be called if the change type is a valid change type. Valid change types can be
     * defined using the {@link #getExpectedChangeType()} method.
     * 
     * @param change
     *            The change for which the preconditions have to be fulfilled.
     * @return <code>True</code> if the preconditions are fulfilled, otherwise <code>false</code>.
     */
    protected abstract boolean checkPreconditions(final T change);

    @Override
    public boolean doesHandleChange(final EChange change) {

        if (!isValidChangeType(change.getClass())) {
            return false;
        }

        @SuppressWarnings("unchecked")
        T typedChange = (T) change;
        return checkPreconditions(typedChange);
    }

    private boolean isValidChangeType(final Class<? extends EChange> changeType) {
        return getExpectedChangeType().isAssignableFrom(changeType);
    }

    @Override
    public ChangePropagationResult applyChange(EChange change, CorrespondenceModel correspondenceModel) {

        this.executionState = new TransformationExecutionState(userInteracting, correspondenceModel,
                new ChangePropagationResult());

        if (doesHandleChange(change)) {
            try {
                @SuppressWarnings("unchecked")
                T typedChange = (T) change;
                executeTransformation(typedChange);
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

    /**
     * Persist an ASEM element.
     * 
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @param element
     *            The ASEM element which should be persisted.
     * @param asemProjectModelPath
     *            The project model path which starts with
     *            {@link ASEMSysMLConstants#MODEL_DIR_NAME}.
     */
    protected void persistASEMElement(final EObject alreadyPersistedObject, final Named element,
            final String asemProjectModelPath) {

        // The element has to be the root element of the resource. Therefore check if this is true.
        final EObject rootElement = EcoreUtil.getRootContainer(element);

        VURI oldVURI = null;
        if (rootElement.eResource() != null) {
            oldVURI = VURI.getInstance(rootElement.eResource());
        }

        VURI asemElementVURI = ASEMSysMLHelper.getModelVURI(alreadyPersistedObject, asemProjectModelPath);

        EcoreUtil.remove(rootElement);
        executionState.getTransformationResult().addRootEObjectToSave(rootElement, asemElementVURI);
        executionState.getTransformationResult().addVuriToDeleteIfNotNull(oldVURI);

    }

    /**
     * Add correspondence between a SysML and an ASEM element.
     * 
     * @param sysmlElement
     *            The SysML element which corresponds to the ASEM element.
     * @param asemElement
     *            The ASEM element which corresponds to the SysML element.
     */
    protected void addCorrespondence(final EObject sysmlElement, final Named asemElement) {
        TuidManager.getInstance().updateTuidsOfRegisteredObjects();
        executionState.getCorrespondenceModel().createAndAddCorrespondence(Collections.singletonList(sysmlElement),
                Collections.singletonList(asemElement));
    }
}
