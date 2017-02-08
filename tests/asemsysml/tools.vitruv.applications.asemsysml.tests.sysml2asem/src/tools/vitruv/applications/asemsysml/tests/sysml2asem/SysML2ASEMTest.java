package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import java.util.Collections;

import org.eclipse.papyrus.sysml14.util.SysMLResource;
import org.eclipse.uml2.uml.Model;

import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.global.SysML2ASEMJavaChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.SysML2ASEMChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;

/**
 * Test case class for transforming a SysML model to ASEM models.
 * 
 * @author Benjamin Rupp
 *
 */
public class SysML2ASEMTest extends ASEMSysMLTest {

    protected final String sysmlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    @Override
    protected void initializeTestModel() {

        Model sysmlModel = SysMLResource.createSysMLModel(this.resourceSet, "SysMLResource", TEST_SYSML_MODEL_NAME);

        String projectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
                SysMlNamspace.FILE_EXTENSION);
        createAndSynchronizeModel(projectModelPath, sysmlModel);

        // Add primitive types to SysML model after the model element was saved and synchronized!
        // This is necessary for VITRUV to detect the primitive type changes.
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_BOOLEAN);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_REAL);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_UNLIMITED_NATURAL);
        sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_STRING);

        saveAndSynchronizeChanges(sysmlModel);

    }

    @Override
    protected Iterable<ChangePropagationSpecification> createDirectionSpecificChangePropagationSpecifications(
            TransformationType transformationType) {
        
        switch (transformationType) {
        case REACTIONS:
            return Collections.singletonList(new SysML2ASEMChangePropagationSpecification());

        case JAVA:
            return Collections.singletonList(new SysML2ASEMJavaChangePropagationSpecification());

        default:
            return null;
        }
    }
}
