package tools.vitruv.applications.asemsysml;

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
    
    public static final String MODEL_DIR_NAME = "model";

    // ASEM
    /**
     * ASEM models are created for a SysML block. Therefore a model name prefix is used followed by
     * a {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator} and the name of the SysML block.
     * 
     * @see #getASEMModelName(String)
     */
    public static final String TEST_ASEM_MODEL_NAME_PREFIX = "ASEM-Model";
    public static final String TEST_ASEM_MODEL_NAME_SEPARATOR = "-";

    // SysML
    public static final String TEST_SYSML_MODEL_NAME = "SysML-Model";

}