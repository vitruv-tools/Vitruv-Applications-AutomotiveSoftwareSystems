package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper;
import tools.vitruv.domains.sysml.SysMlNamspace;

/**
 * Class for all SysML block mapping tests. A SysML block will be mapped to an ASEM model containing
 * an ASEM component as root element.
 * 
 * @author Benjamin Rupp
 */
public class SysMLBlockMappingTransformationTest extends ASEMSysMLTest {

    private Block sysmlBlock;

    private final String umlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    @Override
    protected void init() {

        initializeSysMLAsSourceModel();

    }

    /**
     * Create a SysML block which is needed in all the test cases.
     */
    @Before
    public void setUp() {

        Resource sysmlModelResource = this.getModelResource(umlProjectModelPath);
        this.sysmlBlock = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "SampleBlock", true, this);

    }

    /**
     * After adding a SysML block to a SysML model, a ASEM model should be created with a ASEM
     * component (e.g. a class or module) as root element.
     */
    @Test
    public void testIfASysMLBlockIsMappedToAnASEMModel() {

        this.assertASEMModelForSysMLBlockExists(sysmlBlock);

    }

    /**
     * A SysML block should only be transformed to an ASEM component if the
     * <code>isEncapsulated</code> attribute is set to <code>true</code>. [Requirement 1.b)]
     */
    @Test
    public void testEncapsulatedRestriction() {

        final Boolean isEncapsulated = false;

        Resource sysmlModelResource = this.getModelResource(this.umlProjectModelPath);
        Block blockWhichShouldNotBeTransformed = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource,
                "BlockWhichShouldNotBeTransformed", isEncapsulated, this);
        assertASEMModelDoesNotExistForSysMLBlock(blockWhichShouldNotBeTransformed);

    }

    /**
     * The name of the ASEM module should be equal to the name of the SysML block (the name of its
     * base class). [Requirement 1.c)]
     */
    @Test
    public void testIfNamesAreEqual() {

        this.assertSysMLBlockAndASEMModuleNamesAreEqual(sysmlBlock);

    }

    private void assertASEMModelForSysMLBlockExists(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertResourceExists(asemModelResource);
        ASEMSysMLTestHelper.assertRootElementExists(asemModelResource);
        ASEMSysMLTestHelper.assertRootElementIsTypeOf(asemModelResource, Component.class);

    }

    private void assertSysMLBlockAndASEMModuleNamesAreEqual(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertResourceExists(asemModelResource);
        ASEMSysMLTestHelper.assertRootElementExists(asemModelResource);
        ASEMSysMLTestHelper.assertRootElementIsTypeOf(asemModelResource, Component.class);

        Component asemRootComponent = (Component) asemModelResource.getContents().get(0);

        assertTrue("Root component of ASEM module is not of the type 'ASEMModule'!",
                asemRootComponent instanceof Module);

        final String sysmlBlockName = this.sysmlBlock.getBase_Class().getName();
        final String asemModuleName = asemRootComponent.getName();

        assertEquals("The name of the ASEM module is not equal to the name of the SysML block!", asemModuleName,
                sysmlBlockName);

    }

    private void assertASEMModelDoesNotExistForSysMLBlock(final Block block) {

        Resource asemModelResource = this.getASEMModelResource(block.getBase_Class().getName());
        final Boolean resourceDoesNotExist = (asemModelResource == null);

        assertTrue("An ASEM model does exist for SysML block " + block.getBase_Class().getName() + "!",
                resourceDoesNotExist);

    }

}
