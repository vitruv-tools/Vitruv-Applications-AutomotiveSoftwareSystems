package tools.vitruv.applications.asemsysml.tests.asem2sysml;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;

import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitivetypesFactory;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.reactions.asem2sysml.global.ASEM2SysMLChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;
import tools.vitruv.domains.asem.AsemDomainProvider;
import tools.vitruv.domains.sysml.SysMlDomainProvider;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;
import tools.vitruv.framework.domains.VitruvDomain;

/**
 * Test case class for transforming ASEM models to a SysML model.
 * 
 * @author Benjamin Rupp
 *
 */
public class ASEM2SysMLTest extends ASEMSysMLTest {

    private static Logger logger = Logger.getLogger(ASEM2SysMLTest.class);

    @Override
    protected void setup() {

        final String primitiveTypesProjectModelPath = ASEMSysMLPrimitiveTypeHelper.getPrimitiveTypeProjectModelPath();

        try {
            // First, add the primitive type repository to a new model. This will trigger the SysML
            // model initialization reaction.
            // TODO [BR] Make the initialization process order independent.
            PrimitiveTypeRepository primitiveTypeRepo = PrimitivetypesFactory.eINSTANCE.createPrimitiveTypeRepository();
            primitiveTypeRepo.setName("PrimitiveTypeRepo");
            this.createAndSynchronizeModel(primitiveTypesProjectModelPath, primitiveTypeRepo);

            // Now, add the primitive types to the repository.
            PrimitiveType pUnsignedDiscrete = PrimitivetypesFactory.eINSTANCE.createUnsignedDiscreteType();
            PrimitiveType pSignedDiscrete = PrimitivetypesFactory.eINSTANCE.createSignedDiscreteType();
            PrimitiveType pBoolean = PrimitivetypesFactory.eINSTANCE.createBooleanType();
            PrimitiveType pContinuous = PrimitivetypesFactory.eINSTANCE.createContinuousType();

            primitiveTypeRepo.getPrimitiveTypes().add(pUnsignedDiscrete);
            primitiveTypeRepo.getPrimitiveTypes().add(pSignedDiscrete);
            primitiveTypeRepo.getPrimitiveTypes().add(pBoolean);
            primitiveTypeRepo.getPrimitiveTypes().add(pContinuous);
            this.saveAndSynchronizeChanges(primitiveTypeRepo);

        } catch (IOException e) {
            fail("Could not save and synchronize the primitive type repository!");
            e.printStackTrace();
        }

    }

    @Override
    protected Iterable<ChangePropagationSpecification> createDirectionSpecificChangePropagationSpecifications(
            TransformationType transformationType) {

        switch (transformationType) {
        case REACTIONS:
            return Collections.singletonList(new ASEM2SysMLChangePropagationSpecification());

        case JAVA:
            // TODO Java transformations are not supported at the moment.
            logger.warn(
                    "Java transformations for the transformation direction ASEM -> SysML are not supported at the moment.");
            return Collections.singletonList(null);

        default:
            return null;
        }

    }

    @Override
    protected void cleanup() {
    }

    @Override
    public void saveAndSynchronizeChangesWrapper(EObject object) {

        try {
            saveAndSynchronizeChanges(object);
        } catch (IOException e) {
            fail("Could not save and synchronize change!");
            e.printStackTrace();
        }
    }

    @Override
    public void createAndSynchronizeModelWrapper(String modelPathInProject, EObject rootElement) {

        try {
            createAndSynchronizeModel(modelPathInProject, rootElement);
        } catch (IOException e) {
            fail("Could not create and synchronize model " + modelPathInProject + "!");
            e.printStackTrace();
        }
    }

    @Override
    protected Iterable<VitruvDomain> getVitruvDomains() {
        List<VitruvDomain> domains = new ArrayList<VitruvDomain>();
        domains.add(new AsemDomainProvider().getDomain());
        domains.add(new SysMlDomainProvider().getDomain());
        return domains;
    }

}
