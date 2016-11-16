package tools.vitruv.applications.asemsysml.reactions.sysml2asem.util;

import static tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.ASEMSysMLConstants.getASEMModelName;

import tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.ASEMSysMLHelper;
import tools.vitruv.domains.asem.metamodel.AsemMetamodel;

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
        String projectModelPath = ASEMSysMLHelper.getProjectModelPath(modelName, AsemMetamodel.FILE_EXTENSION);

        return projectModelPath;

    }

}
