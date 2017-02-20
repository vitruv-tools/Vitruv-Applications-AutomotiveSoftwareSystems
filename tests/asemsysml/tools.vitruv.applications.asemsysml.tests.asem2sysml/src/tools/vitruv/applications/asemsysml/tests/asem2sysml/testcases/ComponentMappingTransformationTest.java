package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all ASEM component mapping tests. An ASEM component must be transformed to a SysML
 * block.
 * 
 * @author Benjamin Rupp
 *
 */
public class ComponentMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * After adding a ASEM Component to an ASEM model, a SysML block with the same name must be
     * added to the SysML model.
     * 
     * [Requirement 1.][Requirement 1.c)][Requirement 2.][Requirement 2.c)]
     */
    @Test
    public void testIfAComponentIsMappedToASysMLBlock() {

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleClass", Class.class, this);
        Module asemModule = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleModule", Module.class,
                this);

        // Check correspondence.
        Block blockForClass = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemClass, Block.class);
        Block blockForModule = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemModule, Block.class);

        assertTrue("No corresponding element for ASEM class " + asemClass.getName() + " exists.",
                blockForClass != null);
        assertTrue("No corresponding element for ASEM module " + asemModule.getName() + " exists.",
                blockForModule != null);
        assertEquals("The names of the ASEM component and the SysML block must be equal!", asemClass.getName(),
                blockForClass.getBase_Class().getName());
        assertEquals("The names of the ASEM component and the SysML block must be equal!", asemModule.getName(),
                blockForModule.getBase_Class().getName());

    }

    /**
     * After changing the name of an ASEM component, the name of the corresponding SysML block must
     * be changed, too.
     */
    @Test
    public void testIfASysMLBlockIsRenamed() {

        final String componentNameBefore = "Sample";
        final String componentNameAfter = "RenamedSample";

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync(
                componentNameBefore + Class.class.getSimpleName(), Class.class, this);
        Module asemModule = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync(
                componentNameBefore + Module.class.getSimpleName(), Module.class, this);

        // Check names before renaming.
        Block blockForClass = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemClass, Block.class);
        Block blockForModule = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemModule, Block.class);

        assertEquals("Invalid block name!", componentNameBefore + Class.class.getSimpleName(),
                blockForClass.getBase_Class().getName());
        assertEquals("Invalid block name!", componentNameBefore + Module.class.getSimpleName(),
                blockForModule.getBase_Class().getName());

        // Rename components.
        asemClass.setName(componentNameAfter + Class.class.getSimpleName());
        this.saveAndSynchronizeChanges(asemClass);
        asemModule.setName(componentNameAfter + Module.class.getSimpleName());
        this.saveAndSynchronizeChanges(asemModule);

        // Check names after renaming.
        Block blockForClassRenamed = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemClass, Block.class);
        Block blockForModuleRenamed = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemModule, Block.class);

        assertEquals("Renaming of SysML block " + blockForClassRenamed.getBase_Class().getName() + " failed!",
                componentNameAfter + Class.class.getSimpleName(), blockForClassRenamed.getBase_Class().getName());
        assertEquals("Renaming of SysML block " + blockForModuleRenamed.getBase_Class().getName() + " failed!",
                componentNameAfter + Module.class.getSimpleName(), blockForModuleRenamed.getBase_Class().getName());

    }

    /**
     * After a ASEM component was deleted, the corresponding SysML block and the correspondence
     * between both must be deleted, too.
     */
    @Test
    public void testIfASysMLBlockIsDeleted() {

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleClassToDelete", Class.class,
                this);
        Module asemModule = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleModuleToDelete",
                Module.class, this);

        // Check that corresponding SysML blocks exist.
        Block blockForClass = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemClass, Block.class);
        Block blockForModule = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemModule, Block.class);

        assertTrue("No corresponding SysML block for ASEM class " + asemClass.getName() + " was created!",
                blockForClass != null);
        assertTrue("No corresponding SysML block for ASEM module " + asemModule.getName() + " was created!",
                blockForModule != null);

        // Delete ASEM components.
        final String classProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(asemClass.getName());
        this.deleteAndSynchronizeModel(classProjectModelPath);
        final String moduleProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(asemModule.getName());
        this.deleteAndSynchronizeModel(moduleProjectModelPath);

        // Check after deletion.
        assertTrue("Deletion of ASEM class " + asemClass.getName() + " failed!", asemClass.eResource() == null);
        assertTrue("Deletion of ASEM module " + asemModule.getName() + " failed!", asemModule.eResource() == null);

        Block blockForClassDeleted = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemClass, Block.class);
        Block blockForModuleDeleted = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                asemModule, Block.class);

        assertTrue("Deletion of SysML block for ASEM class " + asemClass.getName() + " failed!",
                blockForClassDeleted == null);
        assertTrue("Deletion of SysML block for ASEM module " + asemModule.getName() + " failed!",
                blockForModuleDeleted == null);

        final Resource sysmlResource = this.getModelResource(this.sysmlProjectModelPath);

        assertTrue("SysML blocks were not deleted from SysML model!", EcoreUtil
                .getObjectsByType(sysmlResource.getContents(), BlocksPackage.eINSTANCE.getBlock()).size() == 0);

    }

}
