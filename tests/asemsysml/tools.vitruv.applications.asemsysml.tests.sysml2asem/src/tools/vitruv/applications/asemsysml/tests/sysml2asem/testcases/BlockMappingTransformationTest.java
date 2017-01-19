package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper;

/**
 * Class for all SysML block mapping tests. A SysML block will be mapped to an ASEM model containing
 * an ASEM component as root element.
 * 
 * @author Benjamin Rupp
 */
public class BlockMappingTransformationTest extends SysML2ASEMTest {

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
                "BlockWhichShouldNotBeTransformed", isEncapsulated, Module.class, this);
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

        Block blockToModule;
        Block blockToClass;

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        // Create two blocks. One which corresponds to an ASEM module and one which corresponds to
        // an ASEM class.
        blockToModule = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockToModule", true, Module.class,
                this);
        blockToClass = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockToClass", true,
                edu.kit.ipd.sdq.ASEM.classifiers.Class.class, this);

        // Check if the names of the SysML block an the ASEM component are equal.
        this.assertSysMLBlockAndASEMComponentNamesAreEqual(blockToModule);
        this.assertSysMLBlockAndASEMComponentNamesAreEqual(blockToClass);

        final String newNameForModule = "NewBlockToModule";
        final String newNameForClass = "NewBlockToClass";

        // Change the name of both of the blocks and save the changes.
        blockToModule.getBase_Class().setName(newNameForModule);
        this.saveAndSynchronizeChanges(blockToModule);
        blockToClass.getBase_Class().setName(newNameForClass);
        this.saveAndSynchronizeChanges(blockToClass);

        // Check the names again.
        this.assertSysMLBlockAndASEMComponentNamesAreEqual(blockToModule);
        this.assertSysMLBlockAndASEMComponentNamesAreEqual(blockToClass);

    }

    private void assertSysMLBlockAndASEMComponentNamesAreEqual(final Block sysmlBlock) {

        Component asemRootComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                sysmlBlock, Component.class);

        final String sysmlBlockName = sysmlBlock.getBase_Class().getName();
        final String asemComponentName = asemRootComponent.getName();

        assertEquals("The name of the ASEM component is not equal to the name of the SysML block!", asemComponentName,
                sysmlBlockName);

    }

    private void assertASEMModelDoesNotExistForSysMLBlock(final Block block) {

        String projectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(block.getBase_Class().getName());
        assertModelNotExists(projectModelPath);

    }

    private void assertExpectedASEMComponentType(final Class<? extends Component> expectedComponentType) {

        final String sysmlBlockName = "BlockTo" + expectedComponentType.getSimpleName();
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        Block block = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, sysmlBlockName, true,
                expectedComponentType, this);

        // Check that the ASEM model was created and the root element is of the expected type.
        Resource asemModelResource = this.getASEMModelResource(sysmlBlockName);
        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, expectedComponentType);

        // Check that the correspondence was created, too.
        Component asemComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), block,
                expectedComponentType);

        assertTrue("No correspondence for the SysML block " + block.getBase_Class().getName() + " was created!",
                asemComponent != null);
        assertTrue("Corresponding ASEM component of the SysML block " + block.getBase_Class().getName()
                + " has wrong type!", expectedComponentType.isAssignableFrom(asemComponent.getClass()));
    }

}
