package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
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
        // TODO [BR] Add port mapping check for ASEM class, too.
    }

    private void assertPortMappingForASEMModuleExists() {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        // Add a block which owns all ports for this test.
        int componentSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(Module.class);
        this.testUserInteractor.addNextSelections(componentSelection);
        Block block = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockWithPort", true, this);

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

        for (Port port : portsToTest) {
            this.assertPortExists(port);

            // [Requirement 1.d)] [Requirement 1.d)i]
            this.assertMessageExistsWithSameName(port);

            // [Requirement 1.d)ii]
            this.assertPortDirectionIsMappedCorrectly(port);

            // [Requirement 1.d)iii] [Requirement 1.d)iv]
            this.assertPortTypeIsMappedCorrectly(port);
        }
    }

    private void assertMessageExistsWithSameName(final Port port) {

        Message asemMessage = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port,
                Message.class);

        assertTrue("The given ASEM module has no message element.", asemMessage != null);
        assertEquals("The names of port and message are not equal.", port.getName(), asemMessage.getName());
    }

    private void assertPortDirectionIsMappedCorrectly(final Port port) {
        // Check the read and write properties of the ASEM message based on the ports flow
        // direction.
        // ([Requirement 1.d)ii])

        FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);
        if (flowProperty == null) {
            fail("There is no flow property available for the current port: " + port);
            return;
        }

        FlowDirection flowDirection = flowProperty.getDirection();
        assertTrue("No flow direction for given flow was found.", flowDirection != null);

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

    private void assertPortTypeIsMappedCorrectly(final Port port) {

        final Type portType = port.getType();

        if (portType instanceof org.eclipse.uml2.uml.Class
                && portType.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {

            // [Requirement 1.d)iii]
            assertMessageTypeIsASEMComponent(port);

        } else if (portType instanceof PrimitiveType) {

            // [Requirement 1.d)iv]
            assertMessageTypeIsPrimitiveType(port);

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

    private void assertMessageTypeIsPrimitiveType(final Port port) {

        final PrimitiveType portType = (PrimitiveType) port.getType();

        CorrespondenceModel correspondenceModel = this.getCorrespondenceModel();

        final Message message = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
                Message.class);
        final Classifier messageType = message.getType();
        assertTrue("The message typ of message " + message.getName() + " isn't set!", messageType != null);

        final Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> expectedMessageType;
        expectedMessageType = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(portType);

        assertTrue(
                "Message has wrong type! Type is " + messageType.getClass().getSimpleName() + ". Expected type was:"
                        + expectedMessageType.getSimpleName(),
                expectedMessageType.isAssignableFrom(messageType.getClass()));
    }

    private void assertMessageTypeIsASEMComponent(final Port port) {
        // The port type is a block, therefore the message type has to be an 1) ASEM module, if the
        // block (which is the type of the port) corresponds to a module, or an 2) ASEM class if the
        // block corresponds to a class.

        CorrespondenceModel correspondenceModel = this.getCorrespondenceModel();

        final Message message = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
                Message.class);
        final Classifier messageType = message.getType();

        assertTrue("Message type is not set.", messageType != null);
        assertTrue("Message type is not a ASEM component.", messageType instanceof Component);

        final Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);
        final Component correspondingASEMComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(correspondenceModel, portsBlock, Component.class);

        Class<?> componentType = correspondingASEMComponent.getClass();
        Class<?> msgType = messageType.getClass();

        assertEquals("The message which corresponds to the given port has the wrong type.", componentType, msgType);
    }
}
