package tools.vitruv.applications.asemsysml;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.framework.tests.TestUserInteractor;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.userinteraction.UserInteractionType;

/**
 * A helper class containing methods which are useful for the simulation of user interacting.
 * 
 * @author Benjamin Rupp
 *
 */

public final class ASEMSysMLUserInteractionHelper {

    /** Utility classes should not have a public or default constructor. */
    private ASEMSysMLUserInteractionHelper() {
    }

    /**
     * Get the number of the given ASEM component type which has to be used to set the next
     * selection of the {@link TestUserInteractor}.
     * 
     * @param expectedComponentType
     *            Type of the ASEM component which should be selected for the transformation of a
     *            SysML block.
     * @return The magic number for the {@link TestUserInteractor}
     * @see TestUserInteractor#addNextSelections(Integer...)
     */
    public static int getNextUserInteractorSelectionForASEMComponent(
            final java.lang.Class<? extends Component> expectedComponentType) {
        // FIXME [BR] Remove magic numbers!
        // The order must be the same as in the transformation!

        int selection = 0;
        if (expectedComponentType.getSimpleName().equals("Module")) {
            selection = 0;
        } else if (expectedComponentType.getSimpleName().equals("Class")) {
            selection = 1;
        }

        return selection;
    }

    /**
     * Simulate the user interaction for selecting a ASEM component type.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @return An ASEM component class.
     */
    public static Class<? extends Component> simulateUserInteractionForASEMComponentType(
            final UserInteracting userInteracting) {

        List<Class<? extends Component>> asemComponentTypes = new ArrayList<java.lang.Class<? extends Component>>();
        asemComponentTypes.add(Module.class);
        asemComponentTypes.add(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

        List<String> asemComponentNames = new ArrayList<String>();
        for (Class<?> asemComponent : asemComponentTypes) {
            asemComponentNames.add(asemComponent.getName());
        }

        int selectedComponentType = userInteracting.selectFromMessage(UserInteractionType.MODAL,
                ASEMSysMLConstants.MSG_SELECT_COMPONENT_TYPE,
                asemComponentNames.toArray(new String[asemComponentNames.size()]));
        Class<? extends Component> selectedComponentTypeClass = asemComponentTypes.get(selectedComponentType);

        return selectedComponentTypeClass;
    }

}
