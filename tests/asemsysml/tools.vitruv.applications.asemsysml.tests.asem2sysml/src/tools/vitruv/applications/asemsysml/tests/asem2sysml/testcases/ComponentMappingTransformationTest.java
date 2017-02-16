package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.papyrus.sysml14.blocks.Block;
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
}
