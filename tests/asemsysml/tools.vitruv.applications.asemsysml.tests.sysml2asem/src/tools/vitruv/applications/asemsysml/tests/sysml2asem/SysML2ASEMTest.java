package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.util.SysMLResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resource.UMLResource;

import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.global.SysML2ASEMJavaChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.SysML2ASEMChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;
import tools.vitruv.domains.asem.AsemDomainProvider;
import tools.vitruv.domains.sysml.SysMlDomainProvider;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;
import tools.vitruv.framework.domains.VitruvDomain;

/**
 * Test case class for transforming a SysML model to ASEM models.
 * 
 * @author Benjamin Rupp
 *
 */
public class SysML2ASEMTest extends ASEMSysMLTest {

    @Override
    protected void setup() {

        try {

            Logger.getRootLogger().setLevel(Level.INFO);

            Model sysmlModel = UMLFactory.eINSTANCE.createModel();
            sysmlModel.setName(TEST_SYSML_MODEL_NAME);

            ResourceSet resourceSet = getCorrespondenceModel().getResource().getResourceSet();

            Resource umlStdProfileResource = resourceSet.getResource(URI.createURI(UMLResource.STANDARD_PROFILE_URI),
                    true);
            Profile umlStdProfile = (Profile) EcoreUtil.getObjectByType(umlStdProfileResource.getContents(),
                    UMLPackage.Literals.PACKAGE);
            sysmlModel.applyProfile(umlStdProfile);

            Resource sysmlProfileResource = resourceSet.getResource(URI.createURI(SysMLResource.PROFILE_PATH), true);
            Profile sysmlProfile = (Profile) EcoreUtil.getObjectByType(sysmlProfileResource.getContents(),
                    UMLPackage.Literals.PACKAGE);
            sysmlModel.applyProfile(sysmlProfile);

            String projectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
                    SysMlNamspace.FILE_EXTENSION);
            createAndSynchronizeModel(projectModelPath, sysmlModel);

            ASEMSysMLPrimitiveTypeHelper.resetRepoInitializationFlag();

            // Add primitive types to SysML model after the model element was saved and
            // synchronized! This is necessary for VITRUV to detect the primitive type changes.
            sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_BOOLEAN);
            sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER);
            sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_REAL);
            sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_UNLIMITED_NATURAL);
            sysmlModel.getPackagedElements().add(ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_STRING);

            saveAndSynchronizeChanges(sysmlModel);

        } catch (IOException e) {
            fail("Could not create and synchronize the SysML model!");
            e.printStackTrace();
        }

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

    @Override
    protected void cleanup() {
    }

    @Override
    public void saveAndSynchronizeChangesWrapper(final EObject object) {

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
        domains.add(new SysMlDomainProvider().getDomain());
        domains.add(new AsemDomainProvider().getDomain());
        return domains;
    }

}
