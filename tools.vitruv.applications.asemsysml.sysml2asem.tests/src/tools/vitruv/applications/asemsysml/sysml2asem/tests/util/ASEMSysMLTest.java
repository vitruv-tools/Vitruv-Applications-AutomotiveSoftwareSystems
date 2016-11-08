package tools.vitruv.applications.asemsysml.sysml2asem.tests.util;

import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.ASEM_FILE_EXTENSION;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.ASEM_FILE_EXTENSIONS;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.ASEM_METAMODEL_NAMESPACE;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.ASEM_METAMODEL_NAMESPACE_URIS;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.SYSML_FILE_EXTENSION;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.SYSML_FILE_EXTENSIONS;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.UML_METAMODEL_NAMESPACE;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.UML_METAMODEL_NAMESPACE_URIS;
import static tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLConstants.getASEMModelName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.util.SysMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.junit.runner.Description;

import tools.vitruv.applications.asemsysml.sysml2asem.global.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.sysml2asem.global.SysML2ASEMChangePropagationSpecification;
import tools.vitruv.framework.change.description.VitruviusChangeFactory;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;
import tools.vitruv.framework.metamodel.Metamodel;
import tools.vitruv.framework.tests.VitruviusEMFCasestudyTest;
import tools.vitruv.framework.util.bridges.EcoreResourceBridge;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * Abstract test case class for ASEM <-> SysML transformation test cases. Encapsulates the
 * ASEM-SysML-specific constants and initialization parts and provides some helper methods for the
 * implementing test case.
 * 
 * @author Benjamin Rupp
 *
 */
public abstract class ASEMSysMLTest extends VitruviusEMFCasestudyTest {

    /*
     * TEST CASE methods. -------------------------------------------------------------------------
     * VitruviusEMFCasestudyTest methods which should be implemented.
     */

    @Override
    protected List<Metamodel> createMetamodels() {

        List<Metamodel> createdMetaModels = new ArrayList<Metamodel>();

        // ASEM
        final VURI asemMetaModelURI = VURI.getInstance(ASEM_METAMODEL_NAMESPACE);
        final Set<String> asemNSURIs = new HashSet<String>();
        asemNSURIs.addAll(Arrays.asList(ASEM_METAMODEL_NAMESPACE_URIS));
        final Metamodel asemMetaModel = new Metamodel(asemNSURIs, asemMetaModelURI, ASEM_FILE_EXTENSIONS);
        createdMetaModels.add(asemMetaModel);

        // UML
        final VURI umlMetaModelURI = VURI.getInstance(UML_METAMODEL_NAMESPACE);
        final Set<String> umlNSURIs = new HashSet<String>();
        umlNSURIs.addAll(Arrays.asList(UML_METAMODEL_NAMESPACE_URIS));
        final Metamodel umlMetaModel = new Metamodel(umlNSURIs, umlMetaModelURI, SYSML_FILE_EXTENSIONS);
        createdMetaModels.add(umlMetaModel);

        return createdMetaModels;

    }

    @Override
    protected Iterable<ChangePropagationSpecification> createChangePropagationSpecifications() {
        return Collections.singletonList(new SysML2ASEMChangePropagationSpecification());
    }

    @Override
    public void beforeTest(Description description) throws Throwable {
        super.beforeTest(description);
        init();
    }

    /*
     * INIT methods. -------------------------------------------------------------------------
     * Methods for initializing the models.
     */

    /**
     * Use this method to initialize the test source model. Therefore you can call the
     * {@link #initializeSysMLAsSourceModel()} method to use SysML as source model.
     * 
     * 
     * This method will be called {@link #beforeTest(Description) before each test}.
     * 
     * @throws Throwable
     */
    protected abstract void init();

    /**
     * Initialize a SysML model as source model. The corresponding target model should be generated
     * using a reaction and the persistProjectRelative() method.
     */
    protected void initializeSysMLAsSourceModel() {

        Model sysmlModel = SysMLResource.createSysMLModel(this.resourceSet, "SysMLResource", TEST_SYSML_MODEL_NAME);
        createAndSyncSourceModel(TEST_SYSML_MODEL_NAME, SYSML_FILE_EXTENSION, sysmlModel);
    }

    private void createAndSyncSourceModel(final String modelName, final String modelFileExtension,
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
                    Collections.<Notifier> unmodifiableList(CollectionLiterals.<Notifier> newArrayList(rootElement)));

        } catch (IOException e) {
            // TODO [BR] Replace with logger message!?
            System.err.println("[ASEMSysML] Model resource for " + modelName + " could not be saved!");
            e.printStackTrace();
        }

    }

    /*
     * HELPER methods. -------------------------------------------------------------------------
     * Helper methods which are useful for all test cases and need information of the parent test
     * classes. E.g. test project related stuff like currentTestProjectName.
     */
    /**
     * Get the platform model path based on the current test project name.
     * 
     * @param modelPathInProject
     *            The project model path. Use
     *            {@link ASEMSysMLHelper#getProjectModelPath(String, String)}.
     * @return The platform model path String.
     */
    protected String getPlatformModelPath(final String modelPathInProject) {
        return ((this.currentTestProjectName + "/") + modelPathInProject);
    }

    /**
     * Get the model VURI.
     * 
     * @param modelPathInProject
     *            The project model path. Use {@link #getPlatformModelPath(String)}.
     * @return The model VURI.
     * @see VURI
     */
    protected VURI getModelVURI(final String modelPathInProject) {
        String platformModelPath = this.getPlatformModelPath(modelPathInProject);
        return VURI.getInstance(platformModelPath);
    }

    /**
     * Get the model resource if the resource exists otherwise return null.
     * 
     * @param projectModelPath
     *            The project model path. Use {@link #getPlatformModelPath(String)}.
     * @return The model resource or null.
     */
    protected Resource getModelResource(final String projectModelPath) {

        VURI modelVURI = this.getModelVURI(projectModelPath);
        URI eMFUri = modelVURI.getEMFUri();

        Resource resource;

        try {
            resource = this.resourceSet.getResource(eMFUri, true);
        } catch (Exception e) {
            // Return null if the resource could not be loaded. E.g. if no
            // resource exists.
            // TODO [BR] Is there a better way to do this?
            resource = null;
        }

        return resource;
    }

    /**
     * Save and synchronize the changes of the given object. This method will save the resource and
     * trigger the synchronization of the virtual model.
     * 
     * @param object
     *            EObject which should be saved and synchronized.
     */
    public void saveAndSynchronizeChanges(final EObject object) {

        try {

            // TODO [BR] Remove second resource variable (code used from code generation)?!
            Resource eResource = object.eResource();
            EcoreResourceBridge.saveResource(eResource);
            Resource eResource1 = object.eResource();
            VURI instance = VURI.getInstance(eResource1);
            this.triggerSynchronization(instance);

        } catch (IOException e) {
         // TODO [BR] Replace with logger message!?
            System.err.println("[ASEMSysML] Could not save and synchronize changes of " + object);
            e.printStackTrace();
        }
    }

    /**
     * Get the ASEM model resource which belongs to a SysML block, because for each SysML block a
     * separate ASEM model is created.
     * 
     * @param sysmlBlockName
     *            The name of the SysML block the ASEM model belongs to.
     * @return ASEM model resource.
     */
    protected Resource getASEMModelResource(final String sysmlBlockName) {

        final String asemModelName = getASEMModelName(sysmlBlockName);

        // Get ASEM model resource for the SysML block.
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, ASEM_FILE_EXTENSION);
        Resource asemModelResource = this.getModelResource(asemProjectModelPath);

        return asemModelResource;
    }
}
