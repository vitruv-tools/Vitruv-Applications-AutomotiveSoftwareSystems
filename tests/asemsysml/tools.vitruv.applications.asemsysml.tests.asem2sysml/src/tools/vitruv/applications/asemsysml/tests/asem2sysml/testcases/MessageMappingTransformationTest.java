package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.BindingConnector;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.junit.Ignore;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.SignedDiscreteType;
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
     * After adding an ASEM message to an ASEM model, an UML Port with the same name (and a SysML
     * FlowProperty and BindingConnector, too) must be added to the SysML model.
     */
    @Test
    public void testIfAMessageIsMappedToASysMLPort() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<Message> messages = this.prepareMessages(module, asemClassForMessageType);

        for (Message message : messages) {

            this.assertPortWasCreated(message, module);
            this.assertPortHasCorrectDirection(message);
            this.assertPortHasCorrectType(message);
        }

    }

    /**
     * After deleting an ASEM message, the corresponding SysML port and the correspondence between
     * both must be deleted, too.
     */
    @Test
    @Ignore
    public void testIfAPortWillBeDeleted() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessageToDelete",
                Module.class, this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<Message> messages = this.prepareMessages(module, asemClassForMessageType);

        for (Message message : messages) {

            final Port portBckp = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                    message, Port.class);
            final org.eclipse.uml2.uml.Class portContainerBckp = (org.eclipse.uml2.uml.Class) portBckp.eContainer();
            final EObject rootElement = EcoreUtil.getRootContainer(message);
            final FlowProperty flowPropertyBckp = ASEMSysMLHelper.getFlowProperty(portBckp);
            final Property propertyBckp = flowPropertyBckp.getBase_Property();

            EcoreUtil.delete(message);
            this.saveAndSynchronizeChanges(rootElement);

            this.assertPortWasDeleted(message, portBckp, portContainerBckp, propertyBckp, flowPropertyBckp);
        }

    }

    /**
     * After renaming an ASEM message, the port must be renamed, too.
     */
    @Test
    public void testIfPortWillBeRenamed() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessagesToRename",
                Module.class, this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<Message> messages = this.prepareMessages(module, asemClassForMessageType);

        for (Message message : messages) {

            final Port portBeforeRenaming = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message, Port.class);
            assertEquals("Port " + portBeforeRenaming.getName() + " has wrong name!", message.getName(),
                    portBeforeRenaming.getName());

            final String newName = message.getName() + "Renamed";
            message.setName(newName);
            this.saveAndSynchronizeChanges(message);

            final Port portAfterRenaming = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message, Port.class);
            assertEquals("Port was renamed successfully!", newName, portAfterRenaming.getName());

        }

    }

    /**
     * After changing the access parameters of an ASEM message (readable, writable), the port
     * direction must be adapted.
     */
    @Test
    public void testIfPortDirectionWillBeUpdated() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Message messagePrimitiveType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBoolean", true,
                false, pBoolean, module, this);
        Message messageComponentType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClass", true,
                false, asemClassForMessageType, module, this);

        this.assertPortHasCorrectDirection(messagePrimitiveType);
        this.assertPortHasCorrectDirection(messageComponentType);

        // INOUT
        messagePrimitiveType.setWritable(true);
        messageComponentType.setWritable(true);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        this.assertPortHasCorrectDirection(messagePrimitiveType);
        this.saveAndSynchronizeChanges(messageComponentType);
        this.assertPortHasCorrectDirection(messageComponentType);

        // OUT
        messagePrimitiveType.setReadable(false);
        messageComponentType.setReadable(false);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        this.assertPortHasCorrectDirection(messagePrimitiveType);
        this.saveAndSynchronizeChanges(messageComponentType);
        this.assertPortHasCorrectDirection(messageComponentType);

    }

    /**
     * After changing the type of an ASEM message, the port type must be adapted.
     */
    @Test
    public void testIfPortTypeWillBeUpdated() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, module);
        final Class asemClassForMessageTypeA = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageTypeA", Class.class, this);
        final Class asemClassForMessageTypeB = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageTypeB", Class.class, this);

        Message messagePrimitiveType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBoolean", true,
                false, pBoolean, module, this);
        Message messageComponentType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClass", true,
                false, asemClassForMessageTypeA, module, this);

        this.assertPortHasCorrectType(messagePrimitiveType);
        this.assertPortHasCorrectType(messageComponentType);

        messagePrimitiveType.setType(pContinuous);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        this.assertPortHasCorrectType(messagePrimitiveType);

        messageComponentType.setType(asemClassForMessageTypeB);
        this.saveAndSynchronizeChanges(messageComponentType);
        this.assertPortHasCorrectType(messageComponentType);

    }

    private Collection<Message> prepareMessages(final Module module, final Class moduleAsType) {

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, module);
        final PrimitiveType pSignedDiscreteType = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(SignedDiscreteType.class, module);

        Collection<Message> messages = new HashSet<Message>();
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanIN", true, false, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanOUT", false, true, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanINOUT", true, true,
                pBoolean, module, this));

        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousIN", true, false,
                pContinuous, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousOUT", false, true,
                pContinuous, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousINOUT", true, true,
                pContinuous, module, this));

        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteIN", true, false,
                pSignedDiscreteType, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteOUT", false, true,
                pSignedDiscreteType, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteINOUT", true, true,
                pSignedDiscreteType, module, this));

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
                    .getSysMLPrimitiveTypeFromSysMLModel(this.getCorrespondenceModel(), message, portType);

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

    private void assertPortWasDeleted(final Message message, final Port port,
            final org.eclipse.uml2.uml.Class portContainer, final Property property, final FlowProperty flowProperty) {

        // Correspondence.
        final Port correspondence = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                message, Port.class);

        assertTrue("Correspondence between message " + message.getName() + " and port " + port.getName()
                + " was not deleted!", correspondence == null);

        // SysML elements.
        final Resource sysmlResource = this.getModelResource(this.sysmlProjectModelPath);
        final Model sysmlModel = (Model) EcoreUtil.getObjectByType(sysmlResource.getContents(),
                UMLPackage.eINSTANCE.getModel());

        assertTrue("No SysML model element found!", sysmlModel != null);

        // Port.
        final Collection<Object> modelPorts = EcoreUtil.getObjectsByType(sysmlModel.getPackagedElements(),
                UMLPackage.eINSTANCE.getPort());

        assertTrue("Port element was not deleted from SysML model!", !modelPorts.contains(port));

        // Port property.
        assertTrue("FlowProperty for port " + port.getName() + " was not deleted!",
                !sysmlModel.getPackagedElements().contains(flowProperty));
        assertTrue("Port property for port " + port.getName() + " was not deleted!",
                !sysmlModel.getPackagedElements().contains(property));

        // Connector.
        final ConnectorEnd connectorEnd = ASEMSysMLHelper.getConnectorEnd(port);
        final Connector connector = ASEMSysMLHelper.getConnector(connectorEnd);
        final BindingConnector bindingConnector = UMLUtil.getStereotypeApplication(connector, BindingConnector.class);

        assertTrue("Connector for port " + port.getName() + " was not deleted!",
                !portContainer.getOwnedConnectors().contains(connector));
        assertTrue("BindingConnector stereoptype for port " + port.getName() + " was not deleted!",
                !sysmlResource.getContents().contains(bindingConnector));

    }
}
