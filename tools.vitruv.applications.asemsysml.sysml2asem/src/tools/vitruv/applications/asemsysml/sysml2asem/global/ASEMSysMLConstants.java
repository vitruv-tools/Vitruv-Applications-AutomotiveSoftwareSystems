package tools.vitruv.applications.asemsysml.sysml2asem.global;

/**
 * Class to encapsulate all the ASEMSysML constants. They can be used in the reflections and in the
 * test cases.
 * 
 * Use e.g.:
 * 
 * <pre>
 * import static ASEMSysML.ASEM_FILE_EXTENSION;
 * </pre>
 * 
 * @author Benjamin Rupp
 *
 */
public final class ASEMSysMLConstants {

    private ASEMSysMLConstants() {
    }

    // ASEM
    /**
     * ASEM models are created for a SysML block. Therefore a model name prefix is used followed by
     * a {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator} and the name of the SysML block.
     * 
     * @see #getASEMModelName(String)
     */
    public static final String TEST_ASEM_MODEL_NAME_PREFIX = "ASEM-Model";
    public static final String TEST_ASEM_MODEL_NAME_SEPARATOR = "-";

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
        return TEST_ASEM_MODEL_NAME_PREFIX + TEST_ASEM_MODEL_NAME_SEPARATOR + blockName;
    }

    // SysML
    public static final String TEST_SYSML_MODEL_NAME = "SysML-Model";

}