package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Port;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
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

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMethod", Module.class,
                this);

        PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper.getASEMPrimitiveTypeFromRepository(BooleanType.class,
                module);

        Message message = DataexchangeFactory.eINSTANCE.createMessage();
        message.setName("SampleMessage");
        message.setReadable(true);
        message.setWritable(false);
        message.setType(pBoolean);

        module.getTypedElements().add(message);
        this.saveAndSynchronizeChanges(module);

        // Check if an UML port with direction IN was created.
        Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), message,
                Port.class);

        assertTrue("No correspondence between the message element " + message.getName() + " and an UML port exists!",
                port != null);

        Block block = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), module,
                Block.class);
        Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);

        assertEquals("The port was not added to the expected SysML block!", block, portsBlock);

        // Check direction.
        FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);

        assertTrue("No flow property for port " + port.getName() + " was found!", flowProperty != null);
        assertEquals("Port " + port.getName() + " has wrong direction!", FlowDirection.IN, flowProperty.getDirection());

        // Check type.
        final PrimitiveType asemType = (PrimitiveType) message.getType();
        final org.eclipse.uml2.uml.PrimitiveType portType = ASEMSysMLPrimitiveTypeHelper
                .getSysMLTypeByASEMType(asemType.getClass());
        final org.eclipse.uml2.uml.PrimitiveType expectedPortType = ASEMSysMLPrimitiveTypeHelper
                .getSysMLPrimitiveTypeFromSysMLModel(this.getCorrespondenceModel(), message, portType.eClass());

        assertTrue("Port type is not set!", port.getType() != null);
        assertEquals("Invalid port type!", expectedPortType, port.getType());

    }

}
