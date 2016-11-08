package tools.vitruv.applications.asemsysml.sysml2asem.tests;

import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.SYSML_FILE_EXTENSION;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.Test;

import tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.sysml2asem.tests.util.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.sysml2asem.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all model initialization tests. The following models are needed: ASEM model and SysML
 * (UML) model.
 * 
 * @author Benjamin Rupp
 *
 */
public class ModelInitializationTest extends ASEMSysMLTest {

    private final String umlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SYSML_FILE_EXTENSION);

    @Override
    protected void init() {

        initializeSysMLAsSourceModel();

    }

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

    private void assertModelExists(final String projectModelPath) {

        final Resource modelResource = this.getModelResource(projectModelPath);
        ASEMSysMLTestHelper.assertResourceExists(modelResource);

    }

    private void assertModelRootExists(final String projectModelPath) {

        final Resource modelResource = this.getModelResource(projectModelPath);
        ASEMSysMLTestHelper.assertRootElementExists(modelResource);

    }

}
