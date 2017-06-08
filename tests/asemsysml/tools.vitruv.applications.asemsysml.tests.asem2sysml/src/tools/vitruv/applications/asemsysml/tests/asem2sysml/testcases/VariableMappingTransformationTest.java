package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all ASEM variable mapping tests. An ASEM variable must be transformed to a SysML
 * property.
 * 
 * @author Benjamin Rupp
 *
 */
public class VariableMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * An ASEM variable must be mapped to an SysML property. The name of the property must be the
     * same as the variable name. The type the SysML property must be the corresponding SysML
     * element of the ASEM variable type. If a variables writable attribute is set to false, the
     * read-only attribute of the SysML property must be set to true.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfVariableMappingExists() throws IOException {

        Logger.getRootLogger().setLevel(Level.DEBUG);

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleClass", Class.class, this);
        Class asemClassAsType = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("SampleClassAsType",
                Class.class, this);

        Variable variable = DataexchangeFactory.eINSTANCE.createVariable();
        variable.setName("SampleVariable");
        variable.setType(asemClassAsType);
        asemClass.getTypedElements().add(variable);
        saveAndSynchronizeChanges(asemClass);

        assertVariableMappingWasSuccessful(variable);
        assertVariableTypeWasMappedCorrectly(variable);

    }

    private void assertVariableMappingWasSuccessful(final Variable variable) {

        Property property = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), variable,
                Property.class);
        assertTrue("No corresponding SysML property found for ASEM variable " + variable.getName() + "!",
                property != null);

        assertEquals("The SysML property must have the same name as the ASEM variable!", variable.getName(),
                property.getName());
    }

    private void assertVariableTypeWasMappedCorrectly(final Variable variable) {

        Property property = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), variable,
                Property.class);
        assertTrue("No corresponding SysML property found for ASEM variable " + variable.getName() + "!",
                property != null);

        Classifier variableType = variable.getType();
        Type correspondingType = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                variableType, Type.class);
        assertEquals("The SysML property has the wrong type!", correspondingType, property.getType());
    }
}
