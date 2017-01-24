package tools.vitruv.applications.asemsysml;

import org.eclipse.papyrus.sysml14.sysmlPackage;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;

/**
 * Class to encapsulate all the ASEMSysML constants. They can be used in the reactions and java
 * transformations and in the test cases, too.
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

    /*
     * This constant is needed because the UML2Util.getQualifiedName() returns
     * "sysml14::blocks::Block" instead of "SysML::Blocks::Block", which cannot be used for the
     * getAppliedStereotype() method.
     */
    private static final String QUALIFIED_NAME_SEPARATOR = "::";
    public static final String QUALIFIED_BLOCK_NAME = sysmlPackage.eNS_PREFIX + QUALIFIED_NAME_SEPARATOR
            + BlocksPackage.eNS_PREFIX + QUALIFIED_NAME_SEPARATOR + BlocksPackage.eINSTANCE.getBlock().getName();

    // User interacting
    public static final String MSG_SELECT_COMPONENT_TYPE = "Please select an ASEM component type the SysML block should mapped to.";
    public static final String MSG_INSERT_METHOD_NAME = "Please insert a method name which can be used for mapping a SysML port to an ASEM method.";
}