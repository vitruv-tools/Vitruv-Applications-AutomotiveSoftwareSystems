package tools.vitruv.applications.asemsysml.sysml2asem.global;

/**
 * Global helper class which can be used in the reflections and in the test
 * cases.
 * 
 * @author Benjamin Rupp
 */
public class ASEMSysMLHelper {

	/**
	 * Get the project model path for a given model (model name and file
	 * extension).
	 * 
	 * @param modelName
	 *            Name of the model.
	 * @param fileExtension
	 *            Associated file extension of the model.
	 * @return Project model path.
	 */
	public static String getProjectModelPath(final String modelName, final String fileExtension) {
		return ((("model/" + modelName) + ".") + fileExtension);
	}

}
