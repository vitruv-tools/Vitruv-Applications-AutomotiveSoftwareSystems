package tools.vitruv.applications.asemsysml.sysml2asem.util;

import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.ASEM_FILE_EXTENSION;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.getASEMModelName;

import tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLHelper;

/**
 * A helper class containing methods which are useful for the transformation using reactions.
 * 
 * @author Benjamin Rupp
 */
public class ASEMSysMLReactionHelper {

    /**
     * Get the project model path for the ASEM model which corresponds to a SysML Block.
     * 
     * @param blockName
     *            Name of the corresponding SysML block.
     * @return Project model path of the ASEM model.
     */
    public static String getASEMProjectModelPath(final String blockName) {

        String modelName = getASEMModelName(blockName);
        String projectModelPath = ASEMSysMLHelper.getProjectModelPath(modelName, ASEM_FILE_EXTENSION);

        return projectModelPath;

    }

}
