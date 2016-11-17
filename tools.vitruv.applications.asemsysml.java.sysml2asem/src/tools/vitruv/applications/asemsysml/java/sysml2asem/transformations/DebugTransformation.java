package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * Debug transformation which catches all changes and print some debug messages to the console.
 * 
 * @author Benjamin Rupp
 *
 */
public class DebugTransformation extends AbstractTransformationRealization {

    private static Logger logger = Logger.getLogger(DebugTransformation.class);

    public DebugTransformation(UserInteracting userInteracting) {
        super(userInteracting);

        // Debug logging level must be set to see the debug messages.
        logger.setLevel(Level.DEBUG);
    }

    @Override
    public boolean checkPreconditions(EChange change) {
        return true;
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return EChange.class;
    }

    @Override
    protected void executeTransformation(EChange change) {
        logger.debug("[ASEMSysML][Java][Change] " + change);
    }

}
