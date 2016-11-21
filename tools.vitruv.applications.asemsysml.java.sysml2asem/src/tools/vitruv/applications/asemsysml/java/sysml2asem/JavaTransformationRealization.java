package tools.vitruv.applications.asemsysml.java.sysml2asem;

import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.util.command.ChangePropagationResult;

/**
 * 
 * Interface which has to be implemented by the java transformation classes.
 * 
 * @author Benjamin Rupp
 *
 */
public interface JavaTransformationRealization {

    /**
     * Get the expected change type of the transformation. <br>
     * <br>
     * 
     * Implement this method to return the change type to which to transformation listens to.
     * 
     * @return The expected change type.
     * 
     * @see EChange
     */
    public abstract Class<? extends EChange> getExpectedChangeType();

    /**
     * Check if the transformation handles the given change.
     * 
     * @param change
     *            The given change.
     * @return <code>True</code> if the transformation handles the change. Otherwise
     *         <code>false</code>.
     */
    public boolean doesHandleChange(final EChange change);

    /**
     * Apply a change to the java transformation to get a change propagation result.
     * 
     * @param change
     *            Change which has to be applied.
     * @param correspondenceModel
     *            The given correspondence model.
     * @return The change propagation result of the java transformation.
     */
    public abstract ChangePropagationResult applyChange(final EChange change,
            final CorrespondenceModel correspondenceModel);

}
