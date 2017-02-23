package tools.vitruv.applications.asemsysml.tests.asem2sysml.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.papyrus.sysml14.blocks.Block;
import org.junit.Test;
import org.junit.runner.Description;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * This test class creates an example ASEM model with an addition component.
 * 
 * The addition component has a method 'doAddition()' which has two input parameters (input1,
 * input2) and a return value. Both parameters and the return value are typed with the continuous
 * type.
 * 
 * @author Benjamin Rupp
 *
 */
public class ASEMAdditionExample extends ASEM2SysMLTest {

    private Class asemClass;
    private Method method;
    private Parameter input1;
    private Parameter input2;
    private ReturnType returnType;

    @Override
    public void beforeTest(Description description) throws Throwable {

        super.beforeTest(description);

        this.createASEMAdditionComponent();

    }

    /**
     * The example ASEM model must be mapped to a SysML block with two ports with direction 'in'
     * (for the ASEM parameters) and a port with direction 'out' (for the ASEM return type).
     */
    @Test
    public void checkSysMLModel() {

        final Block block = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), asemClass,
                Block.class);

        assertTrue("No corresponding SysML block for asem class " + asemClass.getName() + " was found!", block != null);
        assertEquals("SysML block has wrong name.", asemClass.getName(), block.getBase_Class().getName());

        ASEMSysMLAssertionHelper.assertPortWasCreated(input1, asemClass, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortWasCreated(input2, asemClass, this.getCorrespondenceModel());

        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(input1, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(input2, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(returnType, this.getCorrespondenceModel());

        ASEMSysMLAssertionHelper.assertPortHasCorrectType(input1, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(input2, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(returnType, this.getCorrespondenceModel());

    }

    private void createASEMAdditionComponent() {

        asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("Addition", Class.class, this);

        method = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("doAddition", asemClass, this);

        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, asemClass);
        input1 = ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("input1", pContinuous, method, this);
        input2 = ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("input2", pContinuous, method, this);
        returnType = ASEMSysMLTestHelper.createASEMReturnTypeAddToMethodAndSync("return", pContinuous, method, this);

        method.getParameters().add(input1);
        method.getParameters().add(input2);
        method.setReturnType(returnType);
        this.saveAndSynchronizeChanges(method);
    }

}
