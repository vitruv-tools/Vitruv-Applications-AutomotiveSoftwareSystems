package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertTrue;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UMLPackage;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all initialization tests. For each test case the SysML model must be initialized with a
 * UML model element as root and the primitive types must be added to the ASEM model and transformed
 * to the SysML model.
 * 
 * @author Benjamin Rupp
 *
 */
public class InitializationTest extends ASEM2SysMLTest {

    /**
     * Before each test case, the SysML model must be initialized with a UML model element.
     */
    @Test
    public void testIfSysMLModelIsInitialized() {

        final Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);

        this.assertModelExists(this.sysmlProjectModelPath);
        ASEMSysMLTestHelper.assertValidModelResource(sysmlModelResource, Model.class);
    }

    /**
     * Before each test case, the primitive types must be added to the ASEM model and transformed to
     * the SysML model.
     */
    @Test
    public void testIfPrimitiveTypesAreInitialized() {

        final String primitiveTypesProjectModelPath = ASEMSysMLPrimitiveTypeHelper.getPrimitiveTypeProjectModelPath();
        final Resource primitiveTypesResource = this.getModelResource(primitiveTypesProjectModelPath);

        // Check the ASEM primitive types repository.
        this.assertModelExists(primitiveTypesProjectModelPath);
        ASEMSysMLTestHelper.assertValidModelResource(primitiveTypesResource, PrimitiveTypeRepository.class);

        PrimitiveTypeRepository repo = (PrimitiveTypeRepository) this.getRoot(primitiveTypesProjectModelPath);
        assertTrue("The ASEM primitive types repository must contain at least one primitive type.",
                !repo.getPrimitiveTypes().isEmpty());

        // Check the transformation to the SysML model.
        final Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);
        final Model sysmlModel = (Model) EcoreUtil.getObjectByType(sysmlModelResource.getContents(),
                UMLPackage.eINSTANCE.getModel());
        final int numberOfPrimitiveTypes = EcoreUtil
                .getObjectsByType(sysmlModel.getPackagedElements(), UMLPackage.eINSTANCE.getPrimitiveType()).size();

        assertTrue("The SysML model element must contain at least one primitive type.", numberOfPrimitiveTypes > 0);

    }

}
