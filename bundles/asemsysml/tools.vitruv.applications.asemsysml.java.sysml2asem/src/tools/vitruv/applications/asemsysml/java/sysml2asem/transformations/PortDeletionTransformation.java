package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Port;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.RemoveEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the deletion of a SysML port. <br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link RemoveEReference} change.
 * 
 * @author Benjamin Rupp
 *
 */
public class PortDeletionTransformation extends AbstractTransformationRealization<RemoveEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PortDeletionTransformation.class);

    public PortDeletionTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return RemoveEReference.class;
    }

    @Override
    protected void executeTransformation(RemoveEReference<EObject, EObject> change) {

        final Port port = (Port) change.getOldValue();

        logger.info("[ASEMSysML][Java] Delete port reference for port " + port.getName() + " ...");

        final TypedElement correspondingElement = ASEMSysMLHelper.getFirstCorrespondingASEMElement(
                this.executionState.getCorrespondenceModel(), port, TypedElement.class);

        if (correspondingElement == null) {
            logger.info("[ASEMSysML][Java] No corresponding element for port " + port.getName() + " found.");
            return;
        }

        EcoreUtil.delete(correspondingElement);
        this.executionState.getCorrespondenceModel()
                .removeCorrespondencesThatInvolveAtLeastAndDependend(Collections.singleton(port));

    }

    @Override
    protected boolean checkPreconditions(RemoveEReference<EObject, EObject> change) {
        return isPort(change);
    }

    private boolean isPort(RemoveEReference<EObject, EObject> change) {
        return (change.getOldValue() instanceof Port);
    }

}
