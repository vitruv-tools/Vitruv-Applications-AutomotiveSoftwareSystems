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

    public static final String MSG_SELECT_COMPONENT_TYPE = "Please select an ASEM component type the SysML block should mapped to.";
    public static final String MSG_INSERT_METHOD_NAME = "Please insert a method name which can be used for mapping a SysML port to an ASEM method.";
    public static final String MSG_WARN_MODULE_AS_SUBCOMPONENT = "The corresponding ASEM component of the part of the SysML block is a ASEM Module.\n"
            + "An ASEM module cannot be used as a subcomponent in other ASEM components.\n"
            + "Therefore this part reference will NOT be transformed to the ASEM model!";
    public static final String MSG_SELECT_PARAMTER_MODE = "Please select which mode shall be used to add the ASEM parameter to the ASEM model.";
    public static final String MSG_SELECT_METHOD_FOR_PARAMETER = "Please select a method the parameter shall be added to.";
    public static final String MSG_SELECT_METHOD_FOR_RETURN_TYPE = "Please select a method the return type shall be added to.";

    /**
     * Available modes for adding an ASEM parameter or an ASEM return type to an ASEM method.
     * 
     * @author Benjamin Rupp
     *
     */
    public static enum ASEMMethodMode {
        /**
         * Mode for adding an ASEM parameter or return type to a <i>new</i> ASEM Method.
         */
        CREATE_NEW,
        /**
         * Mode for adding an ASEM parameter or return type to an <i>existing</i> ASEM Method.
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
    public static int getNextUserInteractionSelectionForASEMComponent(
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
     * Get the position of the given ASEM method in the given ASEM model resource. This magic number
     * can be used for the next selection of the {@link TestUserInteractor}. <br>
     * <b>Important:</b> This method must be used if <i>all methods</i> were allowed to select (with
     * and without return types). If only methods <i>without</i> return types were shown in the user
     * interaction dialog, the
     * {@link #getNextUserInteractionSelectionForASEMMethodSelectionForReturnTypes(Method, Resource)}
     * method must be used instead.
     * 
     * @param method
     *            The method which shall be selected next.
     * @param asemResource
     *            The ASEM model resource which must contain the method.
     * @return The magic number for the {@link TestUserInteractor}
     * 
     * @see #selectASEMMethodForParameter(UserInteracting, Resource)
     */
    public static int getNextUserInteractionSelectionForASEMMethodSelectionForParameter(final Method method,
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
     * Get the position of the given ASEM method in the given ASEM model resource. This magic number
     * can be used for the next selection of the {@link TestUserInteractor}. <br>
     * <b>Important:</b> This method must be used if <i>only methods without return types</i> were
     * allowed to select. If all methods (with and without return types) were shown in the user
     * interaction dialog, the
     * {@link #getNextUserInteractionSelectionForASEMMethodSelectionForParameter(Method, Resource)}
     * method must be used instead.
     * 
     * @param method
     *            The method which shall be selected next.
     * @param asemResource
     *            The ASEM model resource which must contain the method.
     * @return The magic number for the {@link TestUserInteractor}
     * 
     * @see #selectASEMMethodForReturnType(UserInteracting, Resource)
     */
    public static int getNextUserInteractionSelectionForASEMMethodSelectionForReturnTypes(final Method method,
            final Resource asemResource) {

        List<Method> methods = ASEMSysMLHelper.getAllASEMMethodsWithoutReturnType(asemResource);

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
     * Do user interaction for selecting a ASEM component type.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @return An ASEM component class.
     */
    public static Class<? extends Component> selectASEMComponentType(
            final UserInteracting userInteracting) {

        List<Class<? extends Component>> asemComponentTypes = new ArrayList<java.lang.Class<? extends Component>>();
        asemComponentTypes.add(Module.class);
        asemComponentTypes.add(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

        List<String> asemComponentNames = new ArrayList<String>();
        for (Class<?> asemComponent : asemComponentTypes) {
            asemComponentNames.add(asemComponent.getName());
        }

        int selectedComponentType = userInteracting.selectFromMessage(UserInteractionType.MODAL,
                ASEMSysMLUserInteractionHelper.MSG_SELECT_COMPONENT_TYPE,
                asemComponentNames.toArray(new String[asemComponentNames.size()]));
        Class<? extends Component> selectedComponentTypeClass = asemComponentTypes.get(selectedComponentType);

        return selectedComponentTypeClass;
    }

    /**
     * Do user interaction for determine the mode for adding an ASEM parameter. The user can select
     * whether a new ASEM method shall be created or an existing method shall be used.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @return The {@link ASEMMethodMode} the user has selected.
     */
    public static ASEMMethodMode selectASEMParameterMode(final UserInteracting userInteracting) {

        ASEMMethodMode mode = ASEMMethodMode.CREATE_NEW;

        ASEMMethodMode[] modeTypes = ASEMMethodMode.values();
        List<String> modeNames = new ArrayList<>();

        for (ASEMMethodMode asemParameterMode : modeTypes) {
            modeNames.add(asemParameterMode.toString());
        }

        int selectedModeType = userInteracting.selectFromMessage(UserInteractionType.MODAL,
                ASEMSysMLUserInteractionHelper.MSG_SELECT_PARAMTER_MODE,
                modeNames.toArray(new String[modeNames.size()]));

        mode = modeTypes[selectedModeType];

        return mode;
    }

    /**
     * Do user interaction for selecting an ASEM method from all available methods in the given ASEM
     * model resource where the return value is not set yet.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @param asemResource
     *            The ASEM model resource.
     * @return The selected method.
     */
    public static Method selectASEMMethodForReturnType(final UserInteracting userInteracting,
            final Resource asemResource) {

        Method selectedMethod = null;
        List<Method> methods = ASEMSysMLHelper.getAllASEMMethodsWithoutReturnType(asemResource);

        selectedMethod = selectMethodFromList(methods, MSG_SELECT_METHOD_FOR_RETURN_TYPE, userInteracting);

        return selectedMethod;
    }

    /**
     * Do user interaction for selecting an ASEM method from all available methods in the given ASEM
     * model resource.
     * 
     * @param userInteracting
     *            User interacting of the current transformation.
     * @param asemResource
     *            The ASEM model resource.
     * @return The selected method.
     */
    public static Method selectASEMMethodForParameter(UserInteracting userInteracting, Resource asemResource) {

        Method selectedMethod = null;
        List<Method> methods = ASEMSysMLHelper.getAllASEMMethods(asemResource);

        selectedMethod = selectMethodFromList(methods, MSG_SELECT_METHOD_FOR_PARAMETER, userInteracting);

        return selectedMethod;
    }

    private static Method selectMethodFromList(final List<Method> methods, final String msg,
            final UserInteracting userInteracting) {

        List<String> methodNames = new ArrayList<String>();
        for (Method method : methods) {
            methodNames.add(getMethodSignature(method));
        }

        int selectedMethodSignature = userInteracting.selectFromMessage(UserInteractionType.MODAL, msg,
                methodNames.toArray(new String[methodNames.size()]));

        return methods.toArray(new Method[methods.size()])[selectedMethodSignature];
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
