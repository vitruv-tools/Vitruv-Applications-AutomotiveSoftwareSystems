package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import java.io.IOException;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.util.SysMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;

import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTest;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.change.description.VitruviusChangeFactory;
import tools.vitruv.framework.util.bridges.EcoreResourceBridge;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * Test case class for transforming a SysML model to ASEM models.
 * 
 * @author Benjamin Rupp
 *
 */
public class SysML2ASEMTest extends ASEMSysMLTest {

    private static Logger logger = Logger.getLogger(SysML2ASEMTest.class);

    protected final String sysmlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    @Override
    protected void initializeTestModel() {

        Model sysmlModel = SysMLResource.createSysMLModel(this.resourceSet, "SysMLResource", TEST_SYSML_MODEL_NAME);

        // TODO [BR] Use the createAndSychronizeModel() method of the VitruviusChangePropagationTest
        // instead!?
        // The VitruviusChangePropagationTest#createAndSychronizeModel() method uses the
        // root element for initializing the change recorder. The problem is, that SysML changes
        // like the change of the isEncapsulated flag of a SysML block are not recognized by the
        // change recorder. In case of that, no block transformation will be triggered.

        // String projectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
        // SysMlNamspace.FILE_EXTENSION);
        // createAndSychronizeModel(projectModelPath, sysmlModel);

        createAndSynchronize(TEST_SYSML_MODEL_NAME, SysMlNamspace.FILE_EXTENSION, sysmlModel);

        // Add primitive types to SysML model after the model element was saved and synchronized!
        // This is necessary for VITRUV to detect the primitive type changes.
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_BOOLEAN);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_REAL);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_UNLIMITED_NATURAL);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_STRING);

        saveAndSynchronizeChanges(sysmlModel);

    }

    private void createAndSynchronize(final String modelName, final String modelFileExtension,
            final EObject rootElement) {

        // Set up model paths and URI.
        String projectModelPath = ASEMSysMLHelper.getProjectModelPath(modelName, modelFileExtension);
        String platformModelPath = this.getPlatformModelPath(projectModelPath);
        URI platformResourceURI = URI.createPlatformResourceURI(platformModelPath, true);

        try {

            // Create resource for model file.
            final Resource resource = this.resourceSet.createResource(platformResourceURI);

            if (rootElement != null) {
                resource.getContents().add(rootElement);
            }

            EcoreResourceBridge.saveResource(resource);

            // SYNC changes.

            // Propagate changes to VSUM.
            VURI modelVURI = VURI.getInstance(resource);
            this.synchronizeFileChange(VitruviusChangeFactory.FileChangeKind.Create, modelVURI);

            // Start change recorder to record changes on the model.
            VURI modelVURIAfterSync = VURI.getInstance(resource);
            this.changeRecorder.beginRecording(modelVURIAfterSync,
                    Collections.<Notifier> unmodifiableList(CollectionLiterals.<Notifier> newArrayList(resource)));

        } catch (IOException e) {
            logger.error("[ASEMSysML] Model resource for " + modelName + " could not be saved!");
            e.printStackTrace();
        }

    }

}
