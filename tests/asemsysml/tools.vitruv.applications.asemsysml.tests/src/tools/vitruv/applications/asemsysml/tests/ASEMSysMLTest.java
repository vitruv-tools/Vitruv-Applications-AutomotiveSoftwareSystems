package tools.vitruv.applications.asemsysml.tests;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;
import tools.vitruv.domains.asem.AsemDomain;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.domains.sysml.SysMlDomain;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.metamodel.Metamodel;
import tools.vitruv.framework.tests.VitruviusChangePropagationTest;

/**
 * Abstract test case class for ASEM <-> SysML transformation test cases. Encapsulates the
 * ASEM-SysML-specific constants and initialization parts and provides some helper methods for the
 * implementing test case.
 * 
 * @author Benjamin Rupp
 *
 */
public abstract class ASEMSysMLTest extends VitruviusChangePropagationTest {

    private static TransformationType transformationType = TransformationType.REACTIONS;

    protected final String sysmlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    /*
     * TEST CASE methods. -------------------------------------------------------------------------
     * VitruviusEMFCasestudyTest methods which should be implemented.
     */

    @Override
    protected List<Metamodel> createMetamodels() {

        List<Metamodel> createdMetaModels = new ArrayList<Metamodel>();

        createdMetaModels.add(new AsemDomain().getMetamodel());
        createdMetaModels.add(new SysMlDomain().getMetamodel());

        return createdMetaModels;

    }

    /**
     * Create the change propagation specifications for the given transformation type. This method
     * has to be implemented in the direction specific transformation test class.
     * 
     * @param transformationType
     *            The {@link TransformationType type of the transformation} which should be used.
     * @return The change propagation specifications for the given transformation type. If no change
     *         propagation specification is available <code>null</code> is returned.
     * @see ChangePropagationSpecification
     */
    protected abstract Iterable<ChangePropagationSpecification> createDirectionSpecificChangePropagationSpecifications(
            TransformationType transformationType);

    @Override
    protected Iterable<ChangePropagationSpecification> createChangePropagationSpecifications() {
        return this.createDirectionSpecificChangePropagationSpecifications(transformationType);
    }

    /**
     * Set the transformation which shall be tested. Use this method in a JUnit test suite to set
     * the transformation type which shall be used for the test run. The default value is
     * {@link TransformationType#REACTIONS}.
     * 
     * @see TransformationType
     * 
     * @param type
     *            The transformation type which shall be tested during the test execution.
     */
    public static void setTransformationType(final TransformationType type) {
        transformationType = type;
    }

    /*
     * HELPER methods. -------------------------------------------------------------------------
     * Helper methods which are useful for all test cases and need information of the parent test
     * classes. E.g. test project related stuff like currentTestProjectName.
     */

    /**
     * Save and synchronize the changes of the given object. This method will save the resource and
     * trigger the synchronization of the virtual model.
     *
     * @param object
     *            EObject which should be saved and synchronized.
     */
    public void saveAndSynchronizeChanges(final EObject object) {
        super.saveAndSynchronizeChanges(object);
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

        final String asemModelName = ASEMSysMLHelper.getASEMModelName(sysmlBlockName);

        // Get ASEM model resource for the SysML block.
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);
        Resource asemModelResource = this.getModelResource(asemProjectModelPath);

        return asemModelResource;
    }

    /**
     * Get the correspondence model. If accessing the correspondence model fails, the test will
     * fail, too.
     * 
     * @return The current correspondence model.
     */
    @Override
    protected CorrespondenceModel getCorrespondenceModel() {
        // Override the method of VitruviusEMFCasestudyTest to handle the exception in one place.

        CorrespondenceModel correspondenceModel = null;

        try {
            correspondenceModel = super.getCorrespondenceModel();
        } catch (Throwable e) {
            fail("No correspondence model was found.");
            e.printStackTrace();
        }

        return correspondenceModel;
    }

    /**
     * Set the ASEM component type for the next test user interaction.
     * 
     * @param asemComponentType
     *            The ASEM component type which shall be used next.
     */
    public void setNextUserInteractorSelection(final Class<? extends Component> asemComponentType) {

        final int componentSelectionClass = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMComponent(asemComponentType);

        this.testUserInteractor.addNextSelections(componentSelectionClass);
    }
}
