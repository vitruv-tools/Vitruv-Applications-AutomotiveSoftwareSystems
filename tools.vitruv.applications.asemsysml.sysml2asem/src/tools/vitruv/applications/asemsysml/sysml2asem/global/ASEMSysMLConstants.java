package tools.vitruv.applications.asemsysml.sysml2asem.global;

/**
 * Class to encapsulate all the ASEMSysML constants. They can be used in the
 * reflections and in the test cases.
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
	public static final String ASEM_FILE_EXTENSION = "asem";
	public static final String[] ASEM_FILE_EXTENSIONS = new String[] { ASEM_FILE_EXTENSION };

	public static final String ASEM_METAMODEL_NAMESPACE = "edu.kit.ipd.sdq.asem";
	public static final String[] ASEM_METAMODEL_NAMESPACE_URIS = new String[] { ASEM_METAMODEL_NAMESPACE };

	/**
	 * ASEM models are created for a SysML block. Therefore a model name prefix
	 * is used followed by a {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator}
	 * and the name of the SysML block.
	 * 
	 * @see #getASEMModelName(String)
	 */
	public static final String TEST_ASEM_MODEL_NAME_PREFIX = "ASEM-Model";
	public static final String TEST_ASEM_MODEL_NAME_SEPARATOR = "-";

	/**
	 * Get the ASEM model name which is composed of the
	 * {@link #TEST_ASEM_MODEL_NAME_PREFIX ASEM model prefix}, a
	 * {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator} and the given SysML
	 * block name.
	 * 
	 * @param blockName
	 *            Name of the SysML block for which the ASEM model exists.
	 * @return The complete ASEM model name.
	 */
	public static final String getASEMModelName(final String blockName) {
		return TEST_ASEM_MODEL_NAME_PREFIX + TEST_ASEM_MODEL_NAME_SEPARATOR + blockName;
	}

	// SysML
	public static final String SYSML_FILE_EXTENSION = "uml";
	public static final String[] SYSML_FILE_EXTENSIONS = new String[] { SYSML_FILE_EXTENSION };

	public static final String UML_METAMODEL_NAMESPACE = "http://www.eclipse.org/uml2/5.0.0/UML";
	public static final String SYSML_METAMODEL_NAMESPACE = "http://www.eclipse.org/papyrus/sysml/1.4/SysML";
	public static final String SYSML_BLOCKS_NAMESPACE = "http://www.eclipse.org/papyrus/sysml/1.4/SysML/Blocks";
	public static final String[] UML_METAMODEL_NAMESPACE_URIS = new String[] { UML_METAMODEL_NAMESPACE,
			SYSML_METAMODEL_NAMESPACE, SYSML_BLOCKS_NAMESPACE };

	public static final String TEST_SYSML_MODEL_NAME = "SysML-Model";

}