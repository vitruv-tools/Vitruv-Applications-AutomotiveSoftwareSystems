package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;
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

    private void assertModelRootExists(final String projectModelPath) {

        final Resource modelResource = this.getModelResource(projectModelPath);
        ASEMSysMLTestHelper.assertRootElementExists(modelResource);

    }

}
