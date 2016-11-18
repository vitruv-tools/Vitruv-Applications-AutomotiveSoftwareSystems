package tools.vitruv.applications.asemsysml;

/**
 * Global helper class which can be used in the reflections and in the test cases.
 * 
 * @author Benjamin Rupp
 */
public class ASEMSysMLHelper {

    /**
     * Get the project model path for a given model (model name and file extension).
     * 
     * @param modelName
     *            Name of the model.
     * @param fileExtension
     *            Associated file extension of the model.
     * @return Project model path.
     */
    public static String getProjectModelPath(final String modelName, final String fileExtension) {
        return (((ASEMSysMLConstants.MODEL_DIR_NAME + "/" + modelName) + ".") + fileExtension);
    }

    /**
     * Get the ASEM model name which is composed of the {@link #TEST_ASEM_MODEL_NAME_PREFIX ASEM
     * model prefix}, a {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator} and the given SysML block
     * name.
     * 
     * @param blockName
     *            Name of the SysML block for which the ASEM model exists.
     * @return The complete ASEM model name.
     */
    public static final String getASEMModelName(final String blockName) {
        return ASEMSysMLConstants.TEST_ASEM_MODEL_NAME_PREFIX + ASEMSysMLConstants.TEST_ASEM_MODEL_NAME_SEPARATOR
                + blockName;
    }

}
