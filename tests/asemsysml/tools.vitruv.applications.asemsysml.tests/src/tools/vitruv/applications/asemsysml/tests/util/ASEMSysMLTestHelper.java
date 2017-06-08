package tools.vitruv.applications.asemsysml.tests.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.papyrus.sysml14.portsandflows.PortsandflowsPackage;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.ClassifiersFactory;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import edu.kit.ipd.sdq.ASEM.dataexchange.DataexchangeFactory;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;

/**
 * A helper class which contains useful methods for the ASEMSysML test cases.
 * 
 * @author Benjamin Rupp
 */
public final class ASEMSysMLTestHelper {

    /** Utility classes should not have a public or default constructor. */
    private ASEMSysMLTestHelper() {
    }

    /**
     * Available transformation types. The following transformations are available at the moment:
     * 
     * <ul>
     * <li>transformation using <b>reactions</b>
     * (tools.vitruv.applications.asemsysml.reactions.sysml2asem)</li>
     * <li>transformation using <b>java</b>
     * (tools.vitruv.applications.asemsysml.java.sysml2asem)</li>
     * </ul>
     * 
     * @author Benjamin Rupp
     *
     */
    public static enum TransformationType {
        REACTIONS, JAVA
    };

    /**
     * Get all available ports of the given SysML model resource.
     * 
     * @param sysmlModelResource
     *            The SysML model resource.
     * @return A set of available ports.
     */
    public static Collection<Port> getSysMLPorts(final Resource sysmlModelResource) {

        ASEMSysMLAssertionHelper.assertValidModelResource(sysmlModelResource, Model.class);

        Collection<Port> ports = new HashSet<Port>();
        Model rootModel = getSysMLRootModelElement(sysmlModelResource);
        EList<PackageableElement> modelElements = rootModel.getPackagedElements();

        for (PackageableElement modelElement : modelElements) {

            // Get ports of a block.
            if (modelElement instanceof Class) {
                Class classElement = (Class) modelElement;
                EList<Property> ownedAttributes = classElement.getOwnedAttributes();

                for (Property attribute : ownedAttributes) {
                    if (attribute instanceof Port) {
                        ports.add((Port) attribute);
                    }
                }
            }

            // TODO [BR] Check for nested ports and ports of parts, too.

        }

        return ports;

    }

    /**
     * Get the direction of the given port which is stored in the flow property of this port.
     * 
     * @param port
     *            The given port which must contain a {@link FlowProperty}.
     * @return The {@link FlowDirection} of the given Port.
     */
    public static FlowDirection getPortDirection(final Port port) {

        FlowProperty flowProperty = ASEMSysMLHelper.getFlowProperty(port);
        if (flowProperty == null) {
            fail("There is no flow property available for the current port: " + port);
        }

        FlowDirection flowDirection = flowProperty.getDirection();
        assertTrue("No flow direction for given flow was found.", flowDirection != null);

        return flowDirection;
    }

    /**
     * Create a SysML block and add it to the existing SysML model.
     * 
     * @param sysmlModelResource
     *            SysML model resource.
     * @param blockName
     *            Name of the SysML block to add.
     * @param isEncapsulated
     *            Encapsulated flag, see {@link Block#isEncapsulated()}
     * @param asemComponentType
     *            The ASEM component type the SysML block shall be mapped to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created {@link Block SysML Block}.
     * 
     * @see Block#isEncapsulated
     */
    public static Block createSysMLBlockAddToModelAndSync(Resource sysmlModelResource, final String blockName,
            final Boolean isEncapsulated, final java.lang.Class<? extends Component> asemComponentType,
            final ASEMSysMLTest testCaseClass) {

        ASEMSysMLAssertionHelper.assertValidModelResource(sysmlModelResource, Model.class);

        Model sysmlRootModel = getSysMLRootModelElement(sysmlModelResource);

        // Prepare user selection simulation for ASEM component type.
        testCaseClass.setNextUserInteractorSelection(asemComponentType);

        // Create a SysML block with its base class.
        Class baseClass = sysmlRootModel.createOwnedClass(blockName, false);
        Block sysmlBlock = (Block) StereotypeApplicationHelper.getInstance(null).applyStereotype(baseClass,
                BlocksPackage.eINSTANCE.getBlock());
        sysmlBlock.setIsEncapsulated(isEncapsulated);

        testCaseClass.saveAndSynchronizeChangesWrapper(sysmlRootModel);

        return sysmlBlock;

    }

    /**
     * Create a new ASEM component as root element within a new ASEM model.
     * 
     * @param <T>
     *            The ASEM component type.
     * @param componentName
     *            The name of the component to create.
     * @param asemComponentType
     *            The {@link Component component type} of the component to create.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM component of the given type.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component> T createASEMComponentAsModelRootAndSync(final String componentName,
            final java.lang.Class<T> asemComponentType, final ASEMSysMLTest testCaseClass) {

        T asemComponent;

        if (asemComponentType.isAssignableFrom(edu.kit.ipd.sdq.ASEM.classifiers.Class.class)) {

            asemComponent = (T) ClassifiersFactory.eINSTANCE.createClass();

        } else if (asemComponentType.isAssignableFrom(Module.class)) {

            asemComponent = (T) ClassifiersFactory.eINSTANCE.createModule();

        } else {
            throw new IllegalArgumentException(
                    "ASEM component type " + asemComponentType + " can not be used for component creation!");
        }
        asemComponent.setName(componentName);

        // Create new ASEM model and add component as root element.
        final String asemProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(componentName);
        testCaseClass.createAndSynchronizeModelWrapper(asemProjectModelPath, asemComponent);

        return asemComponent;
    }

    /**
     * Create an ASEM method and add it to an existing ASEM class. The method will save and
     * synchronize the ASEM model, too.
     * 
     * @param methodName
     *            The name of the method,
     * @param component
     *            The ASEM component the method shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM method.
     */
    public static Method createASEMMethodAddToComponentAndSync(final String methodName, final Component component,
            final ASEMSysMLTest testCaseClass) {

        Method method = DataexchangeFactory.eINSTANCE.createMethod();
        method.setName(methodName);
        component.getMethods().add(method);
        testCaseClass.saveAndSynchronizeChangesWrapper(component);

        return method;
    }

    /**
     * Create an ASEM message and add it to an existing ASEM module. The method will save and
     * synchronize the ASEM model, too.
     * 
     * @param messageName
     *            The name of the message.
     * @param readable
     *            <code>True</code> if the message shall be readable, otherwise <code>false</code>.
     * @param writable
     *            <code>True</code> if the message shall be writable, otherwise <code>false</code>.
     * @param type
     *            The message type.
     * @param module
     *            The module the message shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM message element.
     */
    public static Message createASEMMessageAddToModuleAndSync(final String messageName, final boolean readable,
            final boolean writable, final Classifier type, final Module module, final ASEMSysMLTest testCaseClass) {

        Message message = DataexchangeFactory.eINSTANCE.createMessage();
        message.setName(messageName);
        message.setReadable(readable);
        message.setWritable(writable);
        message.setType(type);

        module.getTypedElements().add(message);
        testCaseClass.saveAndSynchronizeChangesWrapper(module);

        return message;
    }

    /**
     * Create an ASEM parameter and add it to an existing ASEM method. This method will save and
     * synchronize the ASEM model, too.
     * 
     * @param parameterName
     *            The name of the parameter.
     * @param type
     *            The parameter type.
     * @param method
     *            The method the parameter shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM parameter.
     */
    public static Parameter createASEMParameterAddToMethodAndSync(final String parameterName, final Classifier type,
            final Method method, final ASEMSysMLTest testCaseClass) {

        Parameter parameter = DataexchangeFactory.eINSTANCE.createParameter();
        parameter.setName(parameterName);
        parameter.setType(type);

        method.getParameters().add(parameter);
        testCaseClass.saveAndSynchronizeChangesWrapper(method);

        return parameter;
    }

    /**
     * Create an ASEM return type and add it to an existing ASEM method. This method will save and
     * synchronize the ASEM model, too.
     * 
     * @param returnTypeName
     *            The name of the return type.
     * @param type
     *            The type of the return type.
     * @param method
     *            The method the return type shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM return type.
     */
    public static ReturnType createASEMReturnTypeAddToMethodAndSync(final String returnTypeName, final Classifier type,
            final Method method, final ASEMSysMLTest testCaseClass) {

        ReturnType returnType = DataexchangeFactory.eINSTANCE.createReturnType();
        returnType.setName(returnTypeName);
        returnType.setType(type);

        method.setReturnType(returnType);
        testCaseClass.saveAndSynchronizeChangesWrapper(method);

        return returnType;
    }

    /**
     * Create an ASEM constant and add it to an existing ASEM component. This method will save and
     * synchronize the ASEM model, too.
     * 
     * @param constantName
     *            The name of the constant.
     * @param type
     *            The ASEM class which shall be referenced.
     * @param component
     *            The component the constant shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM constant.
     */
    public static Constant createASEMConstantAddToComponentAndSync(final String constantName,
            final edu.kit.ipd.sdq.ASEM.classifiers.Class type, final Component component,
            final ASEMSysMLTest testCaseClass) {

        Constant constant = DataexchangeFactory.eINSTANCE.createConstant();
        constant.setName(constantName);
        constant.setType(type);

        component.getTypedElements().add(constant);
        testCaseClass.saveAndSynchronizeChangesWrapper(component);

        return constant;
    }

    /**
     * Create an ASEM variable and add it to an existing ASEM component. This method will save and
     * synchronize the ASEM model, too.
     * 
     * @param variableName
     *            The name of the variable.
     * @param isReadable
     *            <code>True</code> if the variable shall be readable.
     * @param isWritable
     *            <code>True</code> if the variable shall be writable.
     * @param variableType
     *            The variable type.
     * @param component
     *            The component the variable shall be added to.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created ASEM variable.
     */
    public static Variable createASEMVariableAddToComponentAndSync(final String variableName, final boolean isReadable,
            final boolean isWritable, final Classifier variableType, final Component component,
            final ASEMSysMLTest testCaseClass) {

        Variable variable = DataexchangeFactory.eINSTANCE.createVariable();
        variable.setName(variableName);
        variable.setReadable(isReadable);
        variable.setWritable(isWritable);
        variable.setType(variableType);

        component.getTypedElements().add(variable);
        testCaseClass.saveAndSynchronizeChangesWrapper(component);

        return variable;

    }

    /**
     * Add an UML port with the given name and the given flow direction to an existing block. The
     * method will save and synchronize the model, too.
     * 
     * @param block
     *            The SysML block to which the port shall be added to.
     * @param portName
     *            The name of the port.
     * @param flowDirection
     *            The flow direction of the port.
     * @param portType
     *            The type of the port and its property.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The added port.
     * 
     * @see FlowDirection
     */
    public static Port createUMLPortAddToBlockAndSync(final Block block, final String portName,
            final FlowDirection flowDirection, final Type portType, final ASEMSysMLTest testCaseClass) {

        Port port = addPortToBlock(block, portName, flowDirection, portType);
        testCaseClass.saveAndSynchronizeChangesWrapper(port);

        return port;

    }

    /**
     * Add an UML property with the given name and the given type to an existing block. The method
     * will save and synchronize the model, too.
     * 
     * @param block
     *            The SysML block to which the port shall be added to.
     * @param propertyName
     *            The name of the property.
     * @param propertyType
     *            The type of the property.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The added property.
     */
    public static Property createUMLPropertyAddToBlockAndSync(final Block block, final String propertyName,
            final Type propertyType, final ASEMSysMLTest testCaseClass) {

        Property property = addPropertyToBlock(block, propertyName, propertyType, false);
        testCaseClass.saveAndSynchronizeChangesWrapper(property);

        return property;
    }

    /**
     * Add an UML property with the given name and the given type to an existing block. The method
     * will save and synchronize the model, too.
     * 
     * @param block
     *            The SysML block to which the port shall be added to.
     * @param propertyName
     *            The name of the property.
     * @param propertyType
     *            The type of the property.
     * @param readOnly
     *            Set to <code>true</code> if the property shall be read only.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The added property.
     */
    public static Property createUMLPropertyAddToBlockAndSync(final Block block, final String propertyName,
            final Type propertyType, final boolean readOnly, final ASEMSysMLTest testCaseClass) {

        Property property = addPropertyToBlock(block, propertyName, propertyType, readOnly);
        testCaseClass.saveAndSynchronizeChangesWrapper(property);

        return property;
    }

    /**
     * Create a part reference and it to an existing SysML block. This method will save and
     * synchronize the SysML model, too.
     * 
     * @param partReferenceName
     *            Name of the part reference.
     * @param blockWithPart
     *            The block which shall contain the part reference.
     * @param referencedBlock
     *            The block which shall be referenced as part of <code>blockWithPart</code>.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created part property.
     */
    public static Property createPartReferenceForBlockAndSync(final String partReferenceName, final Block blockWithPart,
            final Block referencedBlock, final ASEMSysMLTest testCaseClass) {

        Property partProperty = blockWithPart.getBase_Class().createOwnedAttribute(partReferenceName,
                referencedBlock.getBase_Class());
        // The aggregation kind is needed for the getParts() method.
        partProperty.setAggregation(AggregationKind.COMPOSITE_LITERAL);

        testCaseClass.saveAndSynchronizeChangesWrapper(blockWithPart);

        return partProperty;
    }

    /**
     * Check whether the given parent component references to the given child component or not.
     * 
     * @param parentComponent
     *            ASEM component which contains a reference to another component.
     * @param childComponent
     *            ASEm component which is referenced by another component.
     * 
     * @return <code>True</code> if the parent component contains a reference to the child
     *         component, <code>false</code> otherwise.
     */
    public static boolean doesPartReferenceExists(final Component parentComponent, final Component childComponent) {

        boolean correctPartReferenceMapping = false;

        for (TypedElement typedElement : parentComponent.getTypedElements()) {
            if (typedElement.getType().equals(childComponent)) {
                correctPartReferenceMapping = true;
            }
        }

        return correctPartReferenceMapping;
    }

    private static Model getSysMLRootModelElement(final Resource sysmlModelResource) {
        Model sysmlRootModel = (Model) sysmlModelResource.getContents().get(0);
        assertFalse("The SysML profil must be applied.", sysmlRootModel.getAppliedProfiles().isEmpty());

        return sysmlRootModel;
    }

    private static Port addPortToBlock(final Block block, final String portName, final FlowDirection flowDirection,
            final Type portType) {

        // A) Property with and SysML FlowProperty stereotype.
        Property prop = block.getBase_Class().createOwnedAttribute(portName + "-Property", portType);
        prop.setAggregation(AggregationKind.COMPOSITE_LITERAL);
        // FlowProperty flowProp = (FlowProperty)
        // StereotypeApplicationHelper.getInstance(null).applyStereotype(prop,
        // PortsandflowsPackage.eINSTANCE.getFlowProperty());
        // flowProp.setDirection(FlowDirection.IN);

        // B) Port
        Port port = block.getBase_Class().createOwnedPort(portName, portType);
        port.setAggregation(AggregationKind.COMPOSITE_LITERAL);
        FlowProperty flowProp = (FlowProperty) StereotypeApplicationHelper.getInstance(null).applyStereotype(port,
                PortsandflowsPackage.eINSTANCE.getFlowProperty());
        flowProp.setDirection(flowDirection);

        // C) Connector with SysML BindingConnector stereotype connecting port and property.
        Connector conn = block.getBase_Class().createOwnedConnector("ConnectorForPortAndProperty");
        ConnectorEnd propertyEnd = conn.createEnd();
        propertyEnd.setRole(prop);
        ConnectorEnd portEnd = conn.createEnd();
        portEnd.setRole(port);

        return port;
    }

    private static Property addPropertyToBlock(final Block block, final String propertyName, final Type propertyType,
            final boolean readOnly) {

        Property prop = block.getBase_Class().createOwnedAttribute(propertyName, propertyType);
        prop.setIsReadOnly(readOnly);

        return prop;
    }
}
