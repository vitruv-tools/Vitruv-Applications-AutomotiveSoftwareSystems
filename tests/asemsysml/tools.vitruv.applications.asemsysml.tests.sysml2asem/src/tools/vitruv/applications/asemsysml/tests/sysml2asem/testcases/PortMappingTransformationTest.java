package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper;
import tools.vitruv.framework.correspondence.CorrespondenceModel;

/**
 * Class for all test cases checking the port mapping of ports of a SysML block.
 * 
 * @author Benjamin Rupp
 *
 */
public class PortMappingTransformationTest extends SysML2ASEMTest {

    /**
     * The owned ports of a SysML block shall be mapped to a ASEM message which is part of a ASEM
     * module or to an argument of a ASEM class method. <br>
     * <br>
     * 
     * [Requirement 1.d)] [Requirement 2.d)]
     */
    @Test
    public void testIfPortsMappedCorrectly() {

        this.assertPortMappingForASEMModuleExists();
        this.assertPortMappingForASEMClassExists();
    }

    private void assertPortMappingForASEMModuleExists() {

        Collection<Port> portsToTest = preparePorts(Module.class);

        for (Port port : portsToTest) {
            this.assertPortExists(port);

            // [Requirement 1.d)] [Requirement 1.d)i]
            this.assertVariableExistsWithSameName(port);

            // [Requirement 1.d)ii]
            this.assertPortDirectionMappingForASEMModule(port);

            // [Requirement 1.d)iii] [Requirement 1.d)iv]
            this.assertPortTypeIsMappedCorrectly(port);
        }
    }

    private void assertPortMappingForASEMClassExists() {

        testUserInteractor.addNextSelections("MethodName1", "MethodName2", "MethodName3", "MethodName4", "MethodName5",
                "MethodName6");

        Collection<Port> portsToTest = preparePorts(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

        for (Port port : portsToTest) {

            final FlowDirection flowDirection = ASEMSysMLTestHelper.getPortDirection(port);

            this.assertPortExists(port);

            if (flowDirection.equals(FlowDirection.IN)) {

                // [Requirement 2.d)] [Requirement 2.d)i]
                this.assertVariableExistsWithSameName(port);

            } else if (flowDirection.equals(FlowDirection.INOUT)) {
                // TODO [BR] INOUT ports are not mapped to ASEM models at the moment.
                continue;
            }

            // [Requirement 2.d)ii] [Requirement 2.d)iii] [Requirement 2.e)i] [Requirement 2.e)ii]
            this.assertPortDirectionMappingForASEMClass(port);

            // [Requirement 2.d)iv] [Requirement 2.d)v] [Requirement 2.d)vi] [Requirement 2.e)iii]
            // [Requirement 2.e)iv] [Requirement 2.e)v]
            this.assertPortTypeIsMappedCorrectly(port);
        }
    }

    private Collection<Port> preparePorts(final Class<? extends Component> asemComponentType) {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        // Add a block which owns all ports for this test.
        final String blockName = "BlockWithPortFor" + asemComponentType.getSimpleName();
        Block block = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, blockName, true, asemComponentType,
                this);

        // The different port types to test.
        // TODO [BR] String and unlimited natural are ignored at the moment.
        Type blockType = block.getBase_Class();
        PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_BOOLEAN;
        PrimitiveType pInteger = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER;
        PrimitiveType pReal = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_REAL;

        // Add ports and test if their transformation was successfully.
        Collection<Port> portsToTest = new HashSet<Port>();
        portsToTest.add(
                ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SamplePortIN", FlowDirection.IN, blockType, this));
        portsToTest.add(
                ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SamplePortOUT", FlowDirection.OUT, blockType, this));
        portsToTest.add(ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SamplePortINOUT", FlowDirection.INOUT,
                blockType, this));
        portsToTest.add(ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SampleBooleanPortIN", FlowDirection.IN,
                pBoolean, this));
        portsToTest.add(ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SampleIntegerPortIN", FlowDirection.IN,
                pInteger, this));
        portsToTest.add(
                ASEMSysMLTestHelper.addPortToBlockAndSync(block, "SampleRealPortIN", FlowDirection.IN, pReal, this));

        return portsToTest;
    }

    private void assertVariableExistsWithSameName(final Port port) {

        Variable asemVariable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port,
                Variable.class);

        assertTrue("The SysML port " + port.getName() + " has no corresponding ASEM variable.", asemVariable != null);
        assertEquals("The names of the SysML port and the corresponding ASEM variable are not equal.", port.getName(),
                asemVariable.getName());
    }

    private void assertPortDirectionMappingForASEMModule(final Port port) {
        // Check the read and write properties of the ASEM message based on the ports flow
        // direction.
        // ([Requirement 1.d)ii])

        FlowDirection flowDirection = ASEMSysMLTestHelper.getPortDirection(port);

        Message asemMessage = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port,
                Message.class);

        assertTrue("There was no corresponding ASEM message found for the given port with name " + port.getName(),
                asemMessage != null);

        switch (flowDirection) {
        case IN:
            assertTrue("Flow direction (IN) wasn't mapped properly: The message should be readable.",
                    asemMessage.isReadable());
            assertTrue("Flow direction (IN) wasn't mapped properly: The message should not be writeable.",
                    !asemMessage.isWritable());
            break;
        case OUT:
            assertTrue("Flow direction (OUT) wasn't mapped properly: The message should not be readable.",
                    !asemMessage.isReadable());
            assertTrue("Flow direction (OUT) wasn't mapped properly: The message should be writable.",
                    asemMessage.isWritable());
            break;
        case INOUT:
            assertTrue("Flow direction (INOUT) wasn't mapped properly: The message should be readable.",
                    asemMessage.isReadable());
            assertTrue("Flow direction (INOUT) wasn't mapped properly: The message should be writeable.",
                    asemMessage.isWritable());
            break;

        default:
            break;
        }

    }

    private void assertPortDirectionMappingForASEMClass(final Port port) {
        /*
         * If the port direction is "in", an ASEM method shall be exists with an ASEM parameter
         * which is named and typed like the port. [Requirement 2.d)] [Requirement 2.d)i]
         * [Requirement 2.d)ii]
         * 
         * If the port direction is "out", an ASEM method shall be exists with an ASEM return type
         * which is typed like the SysML port. [Requirement 2.e)] [Requirement 2.e)i] [Requirement
         * 2.e)ii]
         */
        final FlowDirection direction = ASEMSysMLTestHelper.getPortDirection(port);
        final TypedElement correspondingTypedElement = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port, TypedElement.class);
        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final Component component = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                block, Component.class);

        switch (direction) {
        case IN:

            assertTrue("The SysML port with direction 'in' must be mapped to an ASEM parameter!",
                    correspondingTypedElement instanceof Parameter);
            assertTrue("The name of the parameter must be equal to the name of the port!",
                    correspondingTypedElement.getName().equals(port.getName()));

            assertTrue("The parameter must be part of a method!",
                    correspondingTypedElement.eContainer() instanceof Method);
            assertTrue(
                    "The method of the parameter must be part of a component which corresponds to the block of the port!",
                    component.getMethods().contains((Method) correspondingTypedElement.eContainer()));

            break;

        case OUT:

            assertTrue("The SysML port with direction 'out' must be mapped to an ASEM return type!",
                    correspondingTypedElement instanceof ReturnType);

            assertTrue("The return type must be part of a method!",
                    correspondingTypedElement.eContainer() instanceof Method);
            assertTrue(
                    "The method of the return type must be part of a component which corresponds to the block of the port!",
                    component.getMethods().contains((Method) correspondingTypedElement.eContainer()));

            break;

        default:
            break;
        }

    }

    private void assertPortTypeIsMappedCorrectly(final Port port) {

        final Type portType = port.getType();
        final Classifier asemVariableType = assertVariableTypeExists(port);

        if (portType instanceof org.eclipse.uml2.uml.Class
                && portType.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {

            // [Requirement 1.d)iii] [Requirement 2.d)iv] [Requirement 2.d)v] [Requirement 2.e)iii]
            // [Requirement 2.e)iv]
            assertVariableTypeIsASEMComponent(port, asemVariableType);

        } else if (portType instanceof PrimitiveType) {

            // [Requirement 1.d)iv] [Requirement 2.d)vi] [Requirement 2.e)v]
            PrimitiveType primitivePortType = (PrimitiveType) port.getType();
            assertVariableTypeIsPrimitiveType(primitivePortType, asemVariableType);

        } else {
            fail("Invalid port type is used.");
        }
    }

    private void assertPortExists(final Port portThatShallExists) {
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Collection<Port> ports = ASEMSysMLTestHelper.getSysMLPorts(sysmlModelResource);
        boolean portExists = false;

        for (Port port : ports) {
            if (port == portThatShallExists) {
                portExists = true;
            }
        }

        assertTrue("The SysML port " + portThatShallExists.getName() + " doesn't exist in SysML model resource.",
                portExists);
    }

    private Classifier assertVariableTypeExists(final Port port) {

        CorrespondenceModel correspondenceModel = this.getCorrespondenceModel();

        final TypedElement typedElement = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
                TypedElement.class);

        assertTrue("No corresponding typed element for port " + port.getName() + " exists!", typedElement != null);

        final Classifier asemType = typedElement.getType();

        assertTrue("Typed element " + typedElement.getName() + " has no type!", asemType != null);

        return asemType;
    }

    private void assertVariableTypeIsPrimitiveType(final PrimitiveType portType, final Classifier variableType) {

        final Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> expectedVariableType;
        expectedVariableType = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(portType);

        assertTrue(
                "ASEM variable has wrong type! Type is " + variableType.getClass().getSimpleName()
                        + ". Expected type was:" + expectedVariableType.getSimpleName(),
                expectedVariableType.isAssignableFrom(variableType.getClass()));
    }

    private void assertVariableTypeIsASEMComponent(final Port port, final Classifier asemVariableType) {
        // The port type is a block, therefore the message type has to be an 1) ASEM module, if the
        // block (which is the type of the port) corresponds to a module, or an 2) ASEM class if the
        // block corresponds to a class.

        assertTrue("Variable type is not a ASEM component.", asemVariableType instanceof Component);

        final Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);
        final Component correspondingASEMComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), portsBlock, Component.class);

        Class<?> componentType = correspondingASEMComponent.getClass();
        Class<?> variableType = asemVariableType.getClass();

        assertEquals("The ASEM variable which corresponds to the given SysML port has the wrong type.", componentType,
                variableType);
    }
}
