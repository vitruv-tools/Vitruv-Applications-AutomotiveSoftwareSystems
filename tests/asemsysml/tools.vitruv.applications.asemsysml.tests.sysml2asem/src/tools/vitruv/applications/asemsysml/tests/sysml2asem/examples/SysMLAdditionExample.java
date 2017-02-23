package tools.vitruv.applications.asemsysml.tests.sysml2asem.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.junit.Test;
import org.junit.runner.Description;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper.ASEMMethodMode;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * In this test case class the ASEM addition example model is recreated from a SysML model. The
 * SysML model which will be initialized in this test is based on the transformation result of the
 * ASEM to SysML transformation of the addition example (see <i>*.tests.asem2sysml</i> package).
 * 
 * @author Benjamin Rupp
 *
 */
public class SysMLAdditionExample extends SysML2ASEMTest {

    private Block block;
    private Port input1;
    private Port input2;
    private Port returnType;

    @Override
    public void beforeTest(Description description) throws Throwable {

        Logger.getRootLogger().setLevel(Level.INFO);

        super.beforeTest(description);

        this.createSysMLAdditionBlock();
    }

    /**
     * The SysML example model must be transformed to an ASEM class with a method ("doAddition")
     * which has two arguments ("input1" and "input2") and a return type.
     */
    @Test
    public void checkASEMModel() {

        final Class asemClass = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), block,
                Class.class);

        assertTrue("No correspondence for block " + block.getBase_Class().getName() + " found!", asemClass != null);

        Method asemMethod = null;
        for (Method method : asemClass.getMethods()) {
            if (method.getName().equals("doAddition") && method.getParameters().size() == 2
                    && method.getReturnType() != null) {
                asemMethod = method;
            }
        }

        assertTrue("ASEM class " + asemClass.getName() + " does not contain the method doAddition()!",
                asemClass.getMethods().contains(asemMethod));

        final Parameter input1Correspondence = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), input1, Parameter.class);
        final Parameter input2Correspondence = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), input2, Parameter.class);
        final ReturnType returnTypeCorrespondence = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), returnType, ReturnType.class);

        assertTrue("No corresponding ASEM parameter found for port " + input1.getName(), input1Correspondence != null);
        assertTrue("No corresponding ASEM parameter found for port " + input2.getName(), input2Correspondence != null);
        assertTrue("No corresponding ASEM return type found for port " + returnType.getName(),
                returnTypeCorrespondence != null);

        assertEquals("ASEM parameter " + input1Correspondence.getName() + " is not part of the method "
                + asemMethod.getName() + "!", input1Correspondence.getMethod(), asemMethod);
        assertEquals("ASEM parameter " + input2Correspondence.getName() + " is not part of the method "
                + asemMethod.getName() + "!", input2Correspondence.getMethod(), asemMethod);
        assertEquals("ASEM return type " + returnTypeCorrespondence.getName() + " is not part of the method "
                + asemMethod.getName() + "!", returnTypeCorrespondence.getMethod(), asemMethod);

    }

    private void createSysMLAdditionBlock() {

        Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);

        block = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "Addition", true, Class.class, this);

        final PrimitiveType pType = ASEMSysMLPrimitiveTypeHelper.getSysMLTypeByASEMType(ContinuousType.class);
        final PrimitiveType pTypeInstance = ASEMSysMLPrimitiveTypeHelper
                .getSysMLPrimitiveTypeFromSysMLModel(block.eResource(), pType);

        // Add input parameter 1 corresponding to a new ASEM method.
        final int methodModeSelectionA = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.CREATE_NEW);
        this.testUserInteractor.addNextSelections(methodModeSelectionA);
        this.testUserInteractor.addNextSelections("doAddition");

        input1 = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "input1", FlowDirection.IN, pTypeInstance, this);

        // Add input parameter 2 corresponding to the existing ASEM method of input parameter 1.
        final Parameter parameterInput1 = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), input1, Parameter.class);
        final Method method = parameterInput1.getMethod();
        assertTrue("No method found!", method != null);

        final int methodModeSelectionB = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.USE_EXISTING);
        final int methodSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodSelection(method, FlowDirection.IN,
                        this.getCorrespondenceModel());
        this.testUserInteractor.addNextSelections(methodModeSelectionB, methodSelection);
        input2 = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "input2", FlowDirection.IN, pTypeInstance, this);

        // Add return type corresponding to the existing ASEM method of input parameter 1.
        this.testUserInteractor.addNextSelections(methodModeSelectionB, methodSelection);
        returnType = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "return", FlowDirection.OUT, pTypeInstance, this);

    }

}
