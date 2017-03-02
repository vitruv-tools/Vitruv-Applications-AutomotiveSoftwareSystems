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
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
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
public final class ASEMSysMLAssertionHelper {

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
     *            The {@link Component} which contains the port which corresponds to the Named
     *            element.
     * @param correspondenceModel
     *            The test case correspondence model.
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

    /**
     * Check if an ASEM constant was transformed as expected.
     * 
     * @param constant
     *            The constant which shall be mapped correctly.
     * @param referencedClass
     *            The ASEM class to which the constant is referencing.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertConstantWasTransformedAsExpected(final Constant constant,
            final edu.kit.ipd.sdq.ASEM.classifiers.Class referencedClass,
            final CorrespondenceModel correspondenceModel) {

        final Property property = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(correspondenceModel, constant,
                Property.class);

        assertTrue("No corresponding part property for constant " + constant.getName() + " found!", property != null);

        assertEquals("Wrong name of part reference " + property.getName() + "!", constant.getName(),
                property.getName());

        assertEquals("Wrong aggregation kind of part reference " + property.getName() + "!",
                AggregationKind.COMPOSITE_LITERAL, property.getAggregation());

        final org.eclipse.uml2.uml.Class expectedType = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(correspondenceModel, referencedClass, Block.class).getBase_Class();

        assertEquals("Wrong type of part reference " + property.getName() + "!", expectedType, property.getType());
    }

    /**
     * Check if a SysML port was transformed as expected.
     * 
     * @param port
     *            The port which shall be transformed correctly.
     * @param sysmlResource
     *            The SysML model resource.
     * @param correspondenceModel
     *            The test case correspondence model.
     */
    public static void assertPortWasTransformedAsExpected(final Port port, final Resource sysmlResource,
            final CorrespondenceModel correspondenceModel) {

        assertPortExists(port, sysmlResource);

        Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);
        final Component component = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, portsBlock,
                Component.class);

        if (edu.kit.ipd.sdq.ASEM.classifiers.Class.class.isAssignableFrom(component.getClass())) {

            final FlowDirection flowDirection = ASEMSysMLTestHelper.getPortDirection(port);

            if (flowDirection.equals(FlowDirection.IN)) {

                // [Requirement 2.d)] [Requirement 2.d)i]
                assertVariableExistsWithSameName(port, correspondenceModel);

            } else if (flowDirection.equals(FlowDirection.OUT)) {

                // TODO [BR] Check if return type exists!?
            }

            // Flow direction INOUT can not be mapped to the method of a ASEM class since the port
            // can only be mapped to a parameter (IN) or a return type (OUT).
            if (!flowDirection.equals(FlowDirection.INOUT)) {

                // [Requirement 2.d)ii] [Requirement 2.d)iii] [Requirement 2.e)i]
                // [Requirement 2.e)ii]
                assertPortDirectionMappingForASEMClass(port, correspondenceModel);

                // [Requirement 2.d)iv] [Requirement 2.d)v] [Requirement 2.d)vi]
                // [Requirement 2.e)iii] [Requirement 2.e)iv] [Requirement 2.e)v]
                assertPortTypeIsMappedCorrectly(port, correspondenceModel);

            }

        } else {

            // [Requirement 1.d)] [Requirement 1.d)i]
            assertVariableExistsWithSameName(port, correspondenceModel);

            // [Requirement 1.d)ii]
            assertPortDirectionMappingForASEMModule(port, correspondenceModel);

            // [Requirement 1.d)iii] [Requirement 1.d)iv]
            assertPortTypeIsMappedCorrectly(port, correspondenceModel);

        }

    }

    private static void assertPortExists(final Port portThatShallExists, final Resource sysmlResource) {

        Collection<Port> ports = ASEMSysMLTestHelper.getSysMLPorts(sysmlResource);
        boolean portExists = false;

        for (Port port : ports) {
            if (port == portThatShallExists) {
                portExists = true;
            }
        }

        assertTrue("The SysML port " + portThatShallExists.getName() + " doesn't exist in SysML model resource.",
                portExists);
    }

    private static void assertVariableExistsWithSameName(final Port port,
            final CorrespondenceModel correspondenceModel) {

        Variable asemVariable = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
                Variable.class);

        assertTrue("The SysML port " + port.getName() + " has no corresponding ASEM variable.", asemVariable != null);
        assertEquals("The names of the SysML port and the corresponding ASEM variable are not equal.", port.getName(),
                asemVariable.getName());
    }

    private static void assertPortDirectionMappingForASEMModule(final Port port,
            final CorrespondenceModel correspondenceModel) {
        // Check the read and write properties of the ASEM message based on the ports flow
        // direction.
        // ([Requirement 1.d)ii])

        FlowDirection flowDirection = ASEMSysMLTestHelper.getPortDirection(port);

        Message asemMessage = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
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

    private static void assertPortDirectionMappingForASEMClass(final Port port,
            final CorrespondenceModel correspondenceModel) {
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
                .getFirstCorrespondingASEMElement(correspondenceModel, port, TypedElement.class);
        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final Component component = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, block,
                Component.class);

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

    private static void assertPortTypeIsMappedCorrectly(final Port port,
            final CorrespondenceModel correspondenceModel) {

        final Type portType = port.getType();
        final Classifier asemVariableType = assertVariableTypeExists(port, correspondenceModel);

        if (portType instanceof org.eclipse.uml2.uml.Class
                && portType.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {

            // [Requirement 1.d)iii] [Requirement 2.d)iv] [Requirement 2.d)v] [Requirement 2.e)iii]
            // [Requirement 2.e)iv]
            assertVariableTypeIsASEMComponent(port, asemVariableType, correspondenceModel);

        } else if (portType instanceof org.eclipse.uml2.uml.PrimitiveType) {

            // [Requirement 1.d)iv] [Requirement 2.d)vi] [Requirement 2.e)v]
            org.eclipse.uml2.uml.PrimitiveType primitivePortType = (org.eclipse.uml2.uml.PrimitiveType) port.getType();
            assertVariableTypeIsPrimitiveType(primitivePortType, asemVariableType);

        } else {
            fail("Invalid port type is used: " + portType);
        }
    }

    private static Classifier assertVariableTypeExists(final Port port, final CorrespondenceModel correspondenceModel) {

        final TypedElement typedElement = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel, port,
                TypedElement.class);

        assertTrue("No corresponding typed element for port " + port.getName() + " exists!", typedElement != null);

        final Classifier asemType = typedElement.getType();

        assertTrue("Typed element " + typedElement.getName() + " has no type!", asemType != null);

        return asemType;
    }

    private static void assertVariableTypeIsASEMComponent(final Port port, final Classifier asemVariableType,
            final CorrespondenceModel correspondenceModel) {
        // The port type is a block, therefore the message type has to be an 1) ASEM module, if the
        // block (which is the type of the port) corresponds to a module, or an 2) ASEM class if the
        // block corresponds to a class.

        assertTrue("Variable type is not a ASEM component.", asemVariableType instanceof Component);

        final Block portsBlock = ASEMSysMLHelper.getPortsBlock(port);
        final Component correspondingASEMComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(correspondenceModel, portsBlock, Component.class);

        java.lang.Class<?> componentType = correspondingASEMComponent.getClass();
        java.lang.Class<?> variableType = asemVariableType.getClass();

        assertEquals("The ASEM variable which corresponds to the given SysML port has the wrong type.", componentType,
                variableType);
    }

    private static void assertVariableTypeIsPrimitiveType(final org.eclipse.uml2.uml.PrimitiveType portType,
            final Classifier variableType) {

        final java.lang.Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> expectedVariableType;
        expectedVariableType = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(portType);

        assertTrue(
                "ASEM variable has wrong type! Type is " + variableType.getClass().getSimpleName()
                        + ". Expected type was:" + expectedVariableType.getSimpleName(),
                expectedVariableType.isAssignableFrom(variableType.getClass()));
    }
}
