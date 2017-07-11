package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.domains.sysml.SysMlNamspace;

/**
 * Class for all model initialization tests. The following models are needed: ASEM model and SysML
 * (UML) model.
 * 
 * @author Benjamin Rupp
 *
 */
public class ModelInitializationTest extends SysML2ASEMTest {

    private final String umlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    /**
     * After initializing the test model an ASEM model should exists.
     */
    @Test
    public void testIfModelsExist() {

        this.assertModelExists(umlProjectModelPath);

    }

    /**
     * Each of the initialized models should have a root element.
     */
    @Test
    public void testIfModelRootsExist() {

        this.assertModelRootExists(umlProjectModelPath);

    }

    /**
     * A ASEM {@link PrimitiveTypeRepository} must be created and initialized.
     */
    @Test
    public void testIfPrimitiveTypesRepositoryIsCreatedAndInitialized() {

        final String repositoryProjectModelPath = ASEMSysMLPrimitiveTypeHelper.getPrimitiveTypeProjectModelPath();
        this.assertModelExists(repositoryProjectModelPath);

        final String primitiveTypesProjectModelPath = ASEMSysMLPrimitiveTypeHelper.getPrimitiveTypeProjectModelPath();
        final ResourceSet resourceSet = this.getVirtualModel().getCorrespondenceModel().getResource().getResourceSet();
        final Resource primitiveTypesRepoResource = resourceSet
                .getResource(getModelVuri(primitiveTypesProjectModelPath).getEMFUri(), true);

        ASEMSysMLAssertionHelper.assertValidModelResource(primitiveTypesRepoResource, PrimitiveTypeRepository.class);

        final int numberOfRootElements = primitiveTypesRepoResource.getContents().size();
        assertTrue("The primitive type repository resource contains more than one root element!",
                !(numberOfRootElements > 1));
        assertTrue("The primitive type repository resource does not contain a repository element!",
                numberOfRootElements == 1);
    }

    private void assertModelRootExists(final String projectModelPath) {

        final Resource modelResource = this.getModelResource(projectModelPath);
        ASEMSysMLAssertionHelper.assertRootElementExists(modelResource);

    }

}
