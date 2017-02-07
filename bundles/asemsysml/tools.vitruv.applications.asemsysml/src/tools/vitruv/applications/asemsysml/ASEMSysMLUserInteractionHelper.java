package tools.vitruv.applications.asemsysml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
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

    /**
     * Available modes for adding an ASEM parameter to an ASEM method.
     * 
     * @author Benjamin Rupp
     *
     */
    public static enum ASEMParameterMode {
        /**
         * Mode for adding an ASEM parameter to a <i>new</i> ASEM Method.
         */
        CREATE_NEW,
        /**
         * Mode for adding an ASEM parameter to an <i>existing</i> ASEM Method.
         */
        USE_EXISTING
    }

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
     * Get the number of the given ASEM method in the given ASEM model resource. This number can be
     * used for the next selection of the {@link TestUserInteractor}.
     * 
     * @param method
     *            The method which shall be selected next.
     * @param asemResource
     *            The ASEM model resource which must contain the method.
     * @return The magic number for the {@link TestUserInteractor}
     */
    public static int getNextUserInteractorSelectionForASEMMethodSelection(final Method method,
            final Resource asemResource) {

        List<Method> methods = ASEMSysMLHelper.getAllASEMMethods(asemResource);

        for (int i = 0; i < methods.size(); i++) {

            if (methods.get(i).getId().equals(method.getId())) {
                // FIXME [BR] Remove magic numbers!
                // TODO [BR] Hopefully the order of the methods is the same as in the user
                // interaction.
                return i;
            }
        }

        throw new IllegalArgumentException(
                "The method " + method.getName() + " is not contained in the ASEM model resource " + asemResource);
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

    /**
     * Do user interacting for determine the mode for adding an ASEM parameter. The user can select
     * whether a new ASEM method shall be created or an existing method shall be used.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @return The {@link ASEMParameterMode} the user has selected.
     */
    public static ASEMParameterMode selectASEMParameterMode(final UserInteracting userInteracting) {

        ASEMParameterMode mode = ASEMParameterMode.CREATE_NEW;

        ASEMParameterMode[] modeTypes = ASEMParameterMode.values();
        List<String> modeNames = new ArrayList<>();

        for (ASEMParameterMode asemParameterMode : modeTypes) {
            modeNames.add(asemParameterMode.toString());
        }

        int selectedModeType = userInteracting.selectFromMessage(UserInteractionType.MODAL,
                ASEMSysMLConstants.MSG_SELECT_PARAMTER_MODE, modeNames.toArray(new String[modeNames.size()]));

        mode = modeTypes[selectedModeType];

        return mode;
    }

    /**
     * Do user interacting for selecting an ASEM method from all available methods in the given ASEM
     * model resource.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @param asemResource
     *            The ASEM model resource.
     * @return The selected Method.
     */
    public static Method selectASEMMethod(final UserInteracting userInteracting, final Resource asemResource) {

        Method selectedMethod = null;
        List<Method> methods = ASEMSysMLHelper.getAllASEMMethods(asemResource);

        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods) {
            methodNames.add(getMethodSignature(method));
        }

        int selectedMethodSignature = userInteracting.selectFromMessage(UserInteractionType.MODAL,
                ASEMSysMLConstants.MSG_SELECT_METHOD, methodNames.toArray(new String[methodNames.size()]));

        selectedMethod = (Method) methods.toArray()[selectedMethodSignature];

        return selectedMethod;
    }

    private static String getMethodSignature(Method method) {

        String methodSignature = "";

        methodSignature += method.getName();
        methodSignature += "(";

        int parameterCount = 0;
        for (Parameter parameter : method.getParameters()) {

            if (parameterCount > 0) {
                methodSignature += ", ";
            }

            methodSignature += parameter.getType().getName() + " " + parameter.getName();
            parameterCount++;
        }

        methodSignature += ")";

        if (method.getReturnType() != null) {
            methodSignature += " : " + method.getReturnType().getType().getName();
        }

        return methodSignature;
    }

}
