package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper;

/**
 * Class for all SysML block mapping tests. A SysML block will be mapped to an ASEM model containing
 * an ASEM component as root element.
 * 
 * @author Benjamin Rupp
 */
public class BlockMappingTransformationTest extends SysML2ASEMTest {

    private Block sysmlBlock;

    /**
     * Create a SysML block which is needed in all the test cases.
     */
    @Before
    public void setUp() {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        int selection = ASEMSysMLUserInteractionHelper.getNextUserInteractorSelectionForASEMComponent(Module.class);
        this.testUserInteractor.addNextSelections(selection);

        this.sysmlBlock = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "SampleBlock", true, this);

    }

    /**
     * After adding a SysML block to a SysML model, a ASEM model should be created with a ASEM
     * component (e.g. a class or module) as root element. The user decides if a SysML block shall
     * be mapped to an ASEM class or an ASEM module.<br>
     * <br>
     * 
     * [Requirement 1.a)] [Requirement 2.a)]
     */
    @Test
    public void testIfASysMLBlockIsMappedToAnASEMComponent() {

        this.assertASEMComponentForSysMLBlockExists(sysmlBlock);

        this.assertExpectedASEMComponentType(Module.class);
        this.assertExpectedASEMComponentType(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

    }

    /**
     * A SysML block should only be transformed to an ASEM component if the
     * <code>isEncapsulated</code> attribute is set to <code>true</code>.<br>
     * <br>
     * 
     * [Requirement 1.b)]
     */
    @Test
    public void testEncapsulatedRestriction() {

        final Boolean isEncapsulated = false;

        Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);
        Block blockWhichShouldNotBeTransformed = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource,
                "BlockWhichShouldNotBeTransformed", isEncapsulated, this);
        assertASEMModelDoesNotExistForSysMLBlock(blockWhichShouldNotBeTransformed);

    }

    /**
     * The name of the ASEM module should be equal to the name of the SysML block (the name of its
     * base class).<br>
     * <br>
     * 
     * [Requirement 1.c)]
     */
    @Test
    public void testIfNamesAreEqual() {

        this.assertSysMLBlockAndASEMModuleNamesAreEqual(sysmlBlock);

        final String newName = "NewBlockName";

        this.sysmlBlock.getBase_Class().setName(newName);
        this.saveAndSynchronizeChanges(this.sysmlBlock);

        // TODO [BR] Use assertSysMLBlockAndASEMModuleNamesAreEqual() method instead?!
        this.assertASEMComponentNameHasChangedAfterBlockNameChanged(sysmlBlock);

    }

    private void assertASEMComponentForSysMLBlockExists(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

    }

    private void assertSysMLBlockAndASEMModuleNamesAreEqual(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

        Component asemRootComponent = (Component) asemModelResource.getContents().get(0);

        assertTrue("Root component of ASEM module is not of the type 'ASEMModule'!",
                asemRootComponent instanceof Module);

        final String sysmlBlockName = this.sysmlBlock.getBase_Class().getName();
        final String asemModuleName = asemRootComponent.getName();

        assertEquals("The name of the ASEM module is not equal to the name of the SysML block!", asemModuleName,
                sysmlBlockName);

    }

    private void assertASEMComponentNameHasChangedAfterBlockNameChanged(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

        Component asemRootComponent = (Component) asemModelResource.getContents().get(0);

        assertEquals("The name of the ASEM component is not equal to its SysML block name!",
                asemRootComponent.getName(), sysmlBlock.getBase_Class().getName());

    }

    private void assertASEMModelDoesNotExistForSysMLBlock(final Block block) {

        String projectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(block.getBase_Class().getName());
        assertModelNotExists(projectModelPath);

    }

    private void assertExpectedASEMComponentType(final Class<? extends Component> expectedComponentType) {

        final String sysmlBlockName = "BlockTo" + expectedComponentType.getSimpleName();
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        int selection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(expectedComponentType);

        this.testUserInteractor.addNextSelections(selection);
        ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, sysmlBlockName, true, this);

        Resource asemModelResource = this.getASEMModelResource(sysmlBlockName);
        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, expectedComponentType);

    }

}