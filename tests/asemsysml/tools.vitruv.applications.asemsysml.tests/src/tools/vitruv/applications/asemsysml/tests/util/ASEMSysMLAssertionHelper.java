package tools.vitruv.applications.asemsysml.tests.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.BindingConnector;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.correspondence.CorrespondenceModel;

/**
 * A helper class which contains assert methods for the ASEMSysML test cases.
 * 
 * @author Benjamin Rupp
 *
 */
public class ASEMSysMLAssertionHelper {

    /** Utility classes should not have a public or default constructor. */
    private ASEMSysMLAssertionHelper() {
    }

    /**
     * The given model resource should exists.
     * 
     * @param modelResource
     *            The model resource.
     */
    public static void assertResourceExists(final Resource modelResource) {

        final Boolean modelResourceExists = (modelResource != null);
        assertTrue("Model resource doesn't exist.", modelResourceExists);
    }

    /**
     * The given model resource should have a root element.
     * 
     * @param modelResource
     *            The model resource.
     */
    public static void assertRootElementExists(final Resource modelResource) {
        if (modelResource.getContents() == null || modelResource.getContents().get(0) == null) {
            fail("Model " + modelResource.getURI() + " doesn't contain a root element.");
        }
    }

    /**
     * The root element of the given model should be an instance of the given type.
     * 
     * @param <T>
     *            Type of the root class.
     * @param modelResource
     *            The given model resource.
     * @param rootClass
     *            The type the root element should be an instance of.
     */
    public static <T> void assertRootElementIsTypeOf(final Resource modelResource, java.lang.Class<T> rootClass) {
        if (!(rootClass.isInstance(modelResource.getContents().get(0)))) {
            fail("SysML root element is not an instance of " + rootClass.getTypeName() + ".");
        }
    }

    /**
     * For a valid model resource the resource itself must exist and must contain a root element
     * which must be typed by the given class.
     * 
     * @param modelResource
     *            The model resource to check.
     * @param rootElementClass
     *            The class of the root element.
     */
    public static void assertValidModelResource(final Resource modelResource,
            final java.lang.Class<?> rootElementClass) {
        ASEMSysMLAssertionHelper.assertResourceExists(modelResource);
        ASEMSysMLAssertionHelper.assertRootElementExists(modelResource);
        ASEMSysMLAssertionHelper.assertRootElementIsTypeOf(modelResource, rootElementClass);
    }

    /**
     * For a given ASEM Named element a port must exist. The port itself must be contained in the
     * given ASEM component.
     * 
     * @param named
     *            The {@link Named} element which shall correspond to a port.
     * @param component
     *            The {@link Component} which shall contain the port which corresponds to the Named
     *            element.
     * @param correspondenceModel
     */
    public static void assertPortWasCreated(final Named named, final Component component,
            final CorrespondenceModel correspondenceModel) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, named, Port.class);

        assertTrue("No correspondence between the named element " + named.getName() + " and an UML port exists!",
                port != null);
        assertEquals("Wrong aggregation kind for port " + port.getName(), AggregationKind.COMPOSITE_LITERAL,
                port.getAggregation());

        final Block block = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, component,
                Block.class);
        final Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);

        assertEquals("The port was not added to the expected SysML block!", block, portsBlock);
    }

    /**
     * Check if the port direction was set correctly.
     * 
     * @param message
     *            The {@link Message} which corresponds with the port.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertPortHasCorrectDirection(final Message message,
            final CorrespondenceModel correspondenceModel) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, message, Port.class);

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

    /**
     * Check if the port direction was set correctly.
     * 
     * @param parameter
     *            The {@link Parameter} which corresponds with the port.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertPortHasCorrectDirection(final Parameter parameter,
            final CorrespondenceModel correspondenceModel) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, parameter, Port.class);

        final FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);

        FlowDirection expectedDirection = FlowDirection.IN;

        assertTrue("No flow property for port " + port.getName() + " was found!", flowProperty != null);
        assertEquals("Port " + port.getName() + " has wrong direction!", expectedDirection,
                flowProperty.getDirection());
    }

    /**
     * Check if the port direction was set correctly.
     * 
     * @param returnType
     *            The {@link ReturnType} which corresponds with the port.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertPortHasCorrectDirection(final ReturnType returnType,
            final CorrespondenceModel correspondenceModel) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, returnType,
                Port.class);

        final FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);

        FlowDirection expectedDirection = FlowDirection.OUT;

        assertTrue("No flow property for port " + port.getName() + " was found!", flowProperty != null);
        assertEquals("Port " + port.getName() + " has wrong direction!", expectedDirection,
                flowProperty.getDirection());
    }

    /**
     * Check if the port type was set correctly.
     * 
     * @param typedElement
     *            The {@link TypedElement} which corresponds with the port.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertPortHasCorrectType(final TypedElement typedElement,
            final CorrespondenceModel correspondenceModel) {

        final Port port = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, typedElement,
                Port.class);

        assertTrue("Port type is not set!", port.getType() != null);

        if (typedElement.getType() instanceof PrimitiveType) {

            final PrimitiveType asemType = (PrimitiveType) typedElement.getType();
            final org.eclipse.uml2.uml.PrimitiveType portType = ASEMSysMLPrimitiveTypeHelper
                    .getSysMLTypeByASEMType(asemType.getClass());
            final org.eclipse.uml2.uml.PrimitiveType expectedPortType = ASEMSysMLPrimitiveTypeHelper
                    .getSysMLPrimitiveTypeFromSysMLModel(correspondenceModel, typedElement, portType);

            assertEquals("Invalid port type!", expectedPortType, port.getType());

        } else if (typedElement.getType() instanceof Component) {

            final Component messageType = (Component) typedElement.getType();
            final org.eclipse.uml2.uml.Class expectedPortType = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(correspondenceModel, messageType, Block.class).getBase_Class();

            assertEquals("Invalid port type!", expectedPortType, port.getType());

        } else {
            fail("Unsupported message type.");
        }
    }

    /**
     * Check if the port was deleted successfully.
     * 
     * @param typedElement
     *            The {@link TypedElement} which corresponds with the port.
     * @param portBckp
     *            The port which shall be deleted.
     * @param portContainerBckp
     *            The {@link Class} which contains the port.
     * @param propertyBckp
     *            The port {@link Property} which shall be deleted, too.
     * @param flowPropertyBckp
     *            The {@link FlowProperty} which shall be deleted, too.
     * @param correspondenceModel
     *            The test case correspondence model.
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     */
    public static void assertPortWasDeleted(final TypedElement typedElement, final Port portBckp,
            final org.eclipse.uml2.uml.Class portContainerBckp, final Property propertyBckp,
            final FlowProperty flowPropertyBckp, final CorrespondenceModel correspondenceModel,
            final EObject alreadyPersistedObject) {

        // Correspondence.
        final Port correspondence = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, typedElement,
                Port.class);

        assertTrue("Correspondence between typed element " + typedElement.getName() + " and port " + portBckp.getName()
                + " was not deleted!", correspondence == null);

        // SysML elements.
        final String sysmlProjectModelPath = ASEMSysMLHelper
                .getProjectModelPath(ASEMSysMLConstants.TEST_SYSML_MODEL_NAME, SysMlNamspace.FILE_EXTENSION);
        final Resource sysmlResource = ASEMSysMLHelper.getModelResource(correspondenceModel, alreadyPersistedObject,
                sysmlProjectModelPath);
        final Model sysmlModel = (Model) EcoreUtil.getObjectByType(sysmlResource.getContents(),
                UMLPackage.eINSTANCE.getModel());

        assertTrue("No SysML model element found!", sysmlModel != null);

        // Port.
        final Collection<Object> modelPorts = EcoreUtil.getObjectsByType(sysmlModel.getPackagedElements(),
                UMLPackage.eINSTANCE.getPort());

        assertTrue("Port element was not deleted from SysML model!", !modelPorts.contains(portBckp));

        // Port property.
        assertTrue("FlowProperty for port " + portBckp.getName() + " was not deleted!",
                !sysmlModel.getPackagedElements().contains(flowPropertyBckp));
        assertTrue("Port property for port " + portBckp.getName() + " was not deleted!",
                !sysmlModel.getPackagedElements().contains(propertyBckp));

        // Connector.
        final ConnectorEnd connectorEnd = ASEMSysMLHelper.getConnectorEnd(portBckp);
        final Connector connector = ASEMSysMLHelper.getConnector(connectorEnd);
        final BindingConnector bindingConnector = UMLUtil.getStereotypeApplication(connector, BindingConnector.class);

        assertTrue("Connector for port " + portBckp.getName() + " was not deleted!",
                !portContainerBckp.getOwnedConnectors().contains(connector));
        assertTrue("BindingConnector stereoptype for port " + portBckp.getName() + " was not deleted!",
                !sysmlResource.getContents().contains(bindingConnector));

    }
}
