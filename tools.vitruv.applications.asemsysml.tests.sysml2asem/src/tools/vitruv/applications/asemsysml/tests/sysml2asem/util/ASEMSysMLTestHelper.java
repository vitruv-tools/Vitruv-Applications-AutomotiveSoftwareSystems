package tools.vitruv.applications.asemsysml.tests.sysml2asem.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;

/**
 * A helper class which contains useful methods for the ASEMSysML test cases.
 * 
 * @author Benjamin Rupp
 */
public class ASEMSysMLTestHelper {

    /**
     * Create a SysML block and add it to the SysML model.
     * 
     * @param sysmlModelResource
     *            SysML model resource.
     * @param blockName
     *            Name of the SysML block to add.
     * @param isEncapsulated
     *            Encapsulated flag, see {@link Block#isEncapsulated()}
     * @param testCaseClass
     *            Test case class. Needed for synchronization.
     * @return The created {@link Block SysML Block}.
     * 
     * @see Block#isEncapsulated
     */
    public static Block createSysMLBlock(Resource sysmlModelResource, final String blockName,
            final Boolean isEncapsulated, final ASEMSysMLTest testCaseClass) {

        assertRootElementExists(sysmlModelResource);
        assertRootElementIsTypeOf(sysmlModelResource, Model.class);

        // Get SysML model and check if SysML profile is applied.
        Model sysmlRootModel = (Model) sysmlModelResource.getContents().get(0);
        assertFalse("The SysML profil must be applied.", sysmlRootModel.getAppliedProfiles().isEmpty());

        // Create a SysML block with its base class.
        Class baseClass = sysmlRootModel.createOwnedClass(blockName, false);
        Block sysmlBlock = (Block) StereotypeApplicationHelper.getInstance(null).applyStereotype(baseClass,
                BlocksPackage.eINSTANCE.getBlock());
        sysmlBlock.setIsEncapsulated(isEncapsulated);

        testCaseClass.saveAndSynchronizeChanges(sysmlRootModel);

        return sysmlBlock;

    }

    /**
     * The given model resource should exists.
     * 
     * @param modelResource
     *            The model resource.
     */
    public static void assertResourceExists(final Resource modelResource) {

        final Boolean modelResourceExists = (modelResource != null);
        assertTrue("Model resource doesn't exist.", modelResourceExists);
    }

    /**
     * The given model resource should have a root element.
     * 
     * @param modelResource
     *            The model resource.
     */
    public static void assertRootElementExists(final Resource modelResource) {
        if (modelResource.getContents() == null || modelResource.getContents().get(0) == null) {
            fail("Model " + modelResource.getURI() + " doesn't contain a root element.");
        }
    }

    /**
     * The root element of the given model should be an instance of the given type.
     * 
     * @param <T>
     *            Type of the root class.
     * @param modelResource
     *            The given model resource.
     * @param rootClass
     *            The type the root element should be an instance of.
     */
    public static <T> void assertRootElementIsTypeOf(final Resource modelResource, java.lang.Class<T> rootClass) {
        if (!(rootClass.isInstance(modelResource.getContents().get(0)))) {
            fail("SysML root element is not an instance of " + rootClass.getTypeName() + ".");
        }
    }

}
