package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Port;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all ASEM message mapping tests. An ASEM message must be transformed to an UML port.
 * 
 * @author Benjamin Rupp
 *
 */
public class MessageMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * After adding a ASEM message to an ASEM model, an UML Port with the same name (and a SysML
     * FlowProperty and BindingConnector, too) must be added to the SysML model.
     */
    @Test
    public void testIfAMessageIsMappedToASysMLPort() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        Class asemClassForMessageType = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMessageType",
                Class.class, this);

        Collection<Message> methods = this.prepareMessages(module, asemClassForMessageType);

        for (Message message : methods) {

            this.assertPortWasCreated(message, module);
            this.assertPortHasCorrectDirection(message);
            this.assertPortHasCorrectType(message);
        }

    }

    private Collection<Message> prepareMessages(final Module module, final Class moduleAsType) {

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);

        Collection<Message> messages = new HashSet<Message>();
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanIN", true, false, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanOUT", false, true, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanINOUT", true, true,
                pBoolean, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClassIN", true, false,
                moduleAsType, module, this));

        return messages;
    }

    private void assertPortWasCreated(final Message message, final Module module) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message,
                Port.class);

        assertTrue("No correspondence between the message element " + message.getName() + " and an UML port exists!",
                port != null);

        final Block block = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), module,
                Block.class);
        final Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);

        assertEquals("The port was not added to the expected SysML block!", block, portsBlock);
    }

    private void assertPortHasCorrectDirection(final Message message) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message,
                Port.class);

        final FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);

        FlowDirection expectedDirection = null;

        if (message.isReadable() && !message.isWritable()) {
            expectedDirection = FlowDirection.IN;
        } else if (!message.isReadable() && message.isWritable()) {
            expectedDirection = FlowDirection.OUT;
        } else if (message.isReadable() && message.isWritable()) {
            expectedDirection = FlowDirection.INOUT;
        } else {
            fail("Invalid message attributes! Messages were readable and writable are false cannot be transformed.");
        }

        assertTrue("No flow property for port " + port.getName() + " was found!", flowProperty != null);
        assertEquals("Port " + port.getName() + " has wrong direction!", expectedDirection,
                flowProperty.getDirection());
    }

    private void assertPortHasCorrectType(final Message message) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message,
                Port.class);

        assertTrue("Port type is not set!", port.getType() != null);

        if (message.getType() instanceof PrimitiveType) {

            final PrimitiveType asemType = (PrimitiveType) message.getType();
            final org.eclipse.uml2.uml.PrimitiveType portType = ASEMSysMLPrimitiveTypeHelper
                    .getSysMLTypeByASEMType(asemType.getClass());
            final org.eclipse.uml2.uml.PrimitiveType expectedPortType = ASEMSysMLPrimitiveTypeHelper
                    .getSysMLPrimitiveTypeFromSysMLModel(this.getCorrespondenceModel(), message, portType.eClass());

            assertEquals("Invalid port type!", expectedPortType, port.getType());

        } else if (message.getType() instanceof Component) {

            final Component messageType = (Component) message.getType();
            final org.eclipse.uml2.uml.Class expectedPortType = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), messageType, Block.class)
                    .getBase_Class();

            assertEquals("Invalid port type!", expectedPortType, port.getType());

        } else {
            fail("Unsupported message type.");
        }

    }
}
