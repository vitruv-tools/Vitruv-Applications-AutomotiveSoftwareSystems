package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all test cases checking the mapping of a SysML property to an ASEM variable.
 * 
 * @author Benjamin Rupp
 *
 */
public class PropertyMappingTest extends SysML2ASEMTest {

    /**
     * A SysML property must be mapped to an ASEM variable. The name of the variable must be the
     * same as the property name. The type the ASEM variable must be the corresponding ASEM element
     * of the property type. If a property is read-only, the variables writable attribute must be
     * set to false.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfPropertyMappingExists() throws IOException {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        Block blockWithProperty = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource,
                "SampleBlockWithProperty", true, Class.class, this);
        final Block blockAsType = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource,
                "SampleBlockAsType", true, Module.class, this);

        Property property = ASEMSysMLTestHelper.createUMLPropertyAddToBlockAndSync(blockWithProperty, "SampleProperty",
                blockAsType.getBase_Class(), this);

        assertPropertyMappingWasSuccessful(property);
        assertPropertyTypeWasMappedCorrectly(property);

        Property readOnlyProperty = ASEMSysMLTestHelper.createUMLPropertyAddToBlockAndSync(blockWithProperty,
                "SampleReadOnlyProperty", blockAsType.getBase_Class(), true, this);

        assertPropertyMappingWasSuccessful(readOnlyProperty);
        assertPropertyTypeWasMappedCorrectly(readOnlyProperty);
        assertReadOnlyPropertyMappingWasSuccessful(readOnlyProperty);

        readOnlyProperty.setIsReadOnly(false);
        saveAndSynchronizeChanges(blockWithProperty);
        assertReadOnlyPropertyMappingWasSuccessful(readOnlyProperty);
    }

    /**
     * If a SysML property is deleted, the ASEM variable must be deleted, too.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfPropertyMappingIsRemovedAfterPropertyDeletion() throws IOException {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        Block blockWithProperty = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource,
                "BlockForPropertyToDelete", true, Class.class, this);
        Block blockAsType = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockAsType",
                true, Class.class, this);

        Property propertyToDelete = ASEMSysMLTestHelper.createUMLPropertyAddToBlockAndSync(blockWithProperty,
                "PropertyToDelete", blockAsType.getBase_Class(), this);

        // Delete property.
        Variable variableBckp = (Variable) ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), propertyToDelete, Variable.class);
        EObject rootContainer = EcoreUtil.getRootContainer(propertyToDelete);
        EcoreUtil.delete(propertyToDelete);
        saveAndSynchronizeChanges(rootContainer);

        // Check if ASEM variable was deleted.
        Component correspondingContainerElement = (Component) ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockWithProperty, Component.class);

        assertFalse("ASEM variable was not deleted!", correspondingContainerElement.getTypedElements().contains(variableBckp));

        // Check if correspondence was deleted, too.
        ASEMSysMLAssertionHelper.assertCorrespondenceWasDeleted(propertyToDelete, Variable.class,
                this.getCorrespondenceModel());

    }

    private void assertPropertyMappingWasSuccessful(final Property property) {

        Variable variable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), property,
                Variable.class);
        assertTrue("No corresponding ASEM variable found for SysML property " + property.getName() + "!",
                variable != null);

        assertEquals("The ASEM variable must have the same name as the SysML property!", property.getName(),
                variable.getName());
    }

    private void assertPropertyTypeWasMappedCorrectly(final Property property) {

        Variable variable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), property,
                Variable.class);
        assertTrue("No corresponding ASEM variable found for SysML property " + property.getName() + "!",
                variable != null);

        Type propertyType = property.getType();
        Classifier correspondingType = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                propertyType, Classifier.class);
        assertEquals("The ASEM variable has the wrong type!", correspondingType, variable.getType());
    }

    private void assertReadOnlyPropertyMappingWasSuccessful(final Property readOnlyProperty) {

        Variable variable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                readOnlyProperty, Variable.class);
        assertTrue("No corresponding ASEM variable found for SysML property " + readOnlyProperty.getName() + "!",
                variable != null);

        assertEquals("Invalid access paramters for ASEM variable " + variable.getName() + ".",
                readOnlyProperty.isReadOnly(), !variable.isWritable());

    }

}
