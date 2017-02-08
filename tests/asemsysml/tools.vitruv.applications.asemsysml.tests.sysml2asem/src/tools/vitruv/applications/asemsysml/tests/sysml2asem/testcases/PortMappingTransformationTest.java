package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.BindingConnector;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.Named;
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
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper.ASEMMethodMode;
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

    private static Logger logger = Logger.getLogger(PortMappingTransformationTest.class);

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

    /**
     * If a SysML port is deleted, the corresponding ASEM element must be deleted, too.
     */
    @Test
    public void testIfPortMappingIsRemovedAfterPortDeletion() {

        this.assertPortDeletionForASEMModule();
        this.assertPortDeletionForASEMClass();
    }

    /**
     * If a SysML port has direction 'in' the port will be mapped to an ASEM method parameter.
     * Therefore a user interaction dialog must be appear. In this dialog the user can decide
     * whether to create a new method (and enter a method name) or to add the parameter to an
     * existing method (and select this method from the list of available methods). This test case
     * checks, whether the adding to an existing method will be handled correctly.
     */
    @Test
    public void testIfASEMMethodArgumentWasAddedToExistingMethod() {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        final Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;
        final PrimitiveType pInteger = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER;
        final String methodName = "MethodWithSecondParameter";

        // Add a block which will be contain all the ports.
        Block blockA = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockA", true, asemComponentType,
                this);

        // Add a port PortX to the block, to test if the correct method is selected by the user
        // interacting.
        final int parameterModeSelection = this.getNextUserInteractorSelection(ASEMMethodMode.CREATE_NEW);
        this.testUserInteractor.addNextSelections(parameterModeSelection);
        this.testUserInteractor.addNextSelections("MethodWithoutSecondParameter");
        ASEMSysMLTestHelper.addPortToBlockAndSync(blockA, "PortX", FlowDirection.IN, pInteger, this);

        // Add a port PortA which will be mapped to an ASEM parameter in a new ASEM method.
        final int parameterModeSelectionA = this.getNextUserInteractorSelection(ASEMMethodMode.CREATE_NEW);
        this.testUserInteractor.addNextSelections(parameterModeSelectionA);
        this.testUserInteractor.addNextSelections(methodName);
        final Port portA = ASEMSysMLTestHelper.addPortToBlockAndSync(blockA, "PortA", FlowDirection.IN, pInteger, this);

        // Add a port PortB which will be mapped to an ASEM parameter of the ASEM method of PortA.
        final int parameterModeSelectionB = this.getNextUserInteractorSelection(ASEMMethodMode.USE_EXISTING);
        final int methodSelection = this.getNextUserInteractorSelection(this.getMethodOfPortCorrespondence(portA),
                FlowDirection.IN);
        this.testUserInteractor.addNextSelections(parameterModeSelectionB, methodSelection);
        final Port portB = ASEMSysMLTestHelper.addPortToBlockAndSync(blockA, "PortB", FlowDirection.IN, pInteger, this);

        // Check transformation results.
        this.assertMethodsForParameterCheckWereAddedCorrectly(blockA, portA, methodName, 2);
        this.assertParameterWasAddedCorrectly(portA, portB);

    }

    /**
     * If a SysML port has direction 'out' the port will be mapped to an ASEM return value.
     * Therefore a user interaction dialog must be appear. In this dialog the user can decide
     * whether to create a new method (and enter a method name) or to add the return value to an
     * existing method (and select this method from the list of available methods) if this method
     * doesn't have a return value so far. This test case checks, whether the adding to an existing
     * method will be handled correctly.
     */
    @Test
    public void testIfASEMReturnValueWasAddedToExistingMethod() {

        Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);
        final Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;
        final PrimitiveType pInteger = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER;

        // Add a block which will be contain all the ports.
        Block block = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "SampleBlock", true, asemComponentType,
                this);

        // Add a port PortA with direction 'in' which will be mapped to an ASEM parameter in a new
        // ASEM method. The created method will NOT have a return type.
        final int parameterModeSelectionA = this.getNextUserInteractorSelection(ASEMMethodMode.CREATE_NEW);
        this.testUserInteractor.addNextSelections(parameterModeSelectionA);
        this.testUserInteractor.addNextSelections("MethodWithoutReturnValue");
        Port portA = ASEMSysMLTestHelper.addPortToBlockAndSync(block, "PortA", FlowDirection.IN, pInteger, this);

        // Add a port PortB with direction 'out' which will be mapped to an ASEM return type in a
        // new ASEM method. The created method will have a return type.
        final int parameterModeSelectionB = this.getNextUserInteractorSelection(ASEMMethodMode.CREATE_NEW);
        this.testUserInteractor.addNextSelections(parameterModeSelectionB);
        this.testUserInteractor.addNextSelections("MethodWithReturnValue");
        Port portB = ASEMSysMLTestHelper.addPortToBlockAndSync(block, "PortB", FlowDirection.OUT, pInteger, this);

        // Add a port PortC with direction 'out' which shall be mapped to an ASEM return type which
        // will be added to the existing method of PortA.
        final int parameterModeSelectionC = this.getNextUserInteractorSelection(ASEMMethodMode.USE_EXISTING);
        final int methodSelectionC = this.getNextUserInteractorSelection(this.getMethodOfPortCorrespondence(portA),
                FlowDirection.OUT);
        this.testUserInteractor.addNextSelections(parameterModeSelectionC, methodSelectionC);
        Port portC = ASEMSysMLTestHelper.addPortToBlockAndSync(block, "PortC", FlowDirection.OUT, pInteger, this);

        // Add a port PortD with direction 'out' and try to add its return type to the existing
        // method of PortB. This attempt must be fail and a new method for the return type of PortD
        // must be created.
        final int parameterModeSelectionD = this.getNextUserInteractorSelection(ASEMMethodMode.USE_EXISTING);

        try {
            // Check user interacting.
            // The message of PortB must NOT be part of the list of available methods in the user
            // interacting dialog.
            Method methodB = this.getMethodOfPortCorrespondence(portB);
            this.getNextUserInteractorSelection(methodB, FlowDirection.OUT);

            fail("The method " + methodB.getName()
                    + " must not be part of the allowed methods because it already has a return type!");

        } catch (IllegalArgumentException iae) {
        }

        this.testUserInteractor.addNextSelections(parameterModeSelectionD);
        Port portD = ASEMSysMLTestHelper.addPortToBlockAndSync(block, "PortD", FlowDirection.OUT, pInteger, this);

        // Check transformation result.
        this.assertMethodsForReturnTypeCheckWereAddedCorrectly(block, portA, portB);
        this.assertReturnTypeWasAddedCorrectly(portA, portB, portC, portD);

    }

    private void assertMethodsForReturnTypeCheckWereAddedCorrectly(final Block block, final Port portA,
            final Port portB) {

        final Component correspondingComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), block, Component.class);

        EList<Method> methods = correspondingComponent.getMethods();
        Method methodA = this.getMethodOfPortCorrespondence(portA);
        Method methodB = this.getMethodOfPortCorrespondence(portB);

        assertTrue(
                "Corresponding ASEM component of block " + block.getBase_Class().getName()
                        + " does not contain all relevant methods.",
                methods.contains(methodA) && methods.contains(methodB));
    }

    private void assertReturnTypeWasAddedCorrectly(final Port portA, final Port portB, final Port portC,
            final Port portD) {

        Method methodA = this.getMethodOfPortCorrespondence(portA);
        Method methodB = this.getMethodOfPortCorrespondence(portB);
        Method methodC = this.getMethodOfPortCorrespondence(portC);
        Method methodD = this.getMethodOfPortCorrespondence(portD);

        assertEquals("Return type corresponding to port " + portC.getName() + " was not added to the method "
                + methodA.getName(), methodA, methodC);

        assertTrue("No method for the corresponding return value of port " + portD.getName() + " was created.",
                methodD != null);
        assertNotEquals("The return type corresponding to port " + portD.getName() + " was added to method "
                + methodB.getName() + ". This method already had a return type.", methodB, methodD);

    }

    private void assertMethodsForParameterCheckWereAddedCorrectly(final Block block, final Port port,
            final String methodName, final int numberOfMethods) {

        Component correspondingComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), block, Component.class);

        assertTrue("Invalid corresponding element for SysML block " + block.getBase_Class().getName() + ".",
                correspondingComponent != null
                        && correspondingComponent instanceof edu.kit.ipd.sdq.ASEM.classifiers.Class);

        EList<Method> methods = correspondingComponent.getMethods();
        Method method = this.getMethodOfPortCorrespondence(port);

        assertEquals("Invalid number of methods in ASEM component " + correspondingComponent.getName(), numberOfMethods,
                methods.size());

        assertTrue("Invalid method.", method != null);
        assertTrue("Method is not contained in method list of " + correspondingComponent.getName(),
                methods.contains(method));
        assertEquals("Invalid method name in ASEM component " + correspondingComponent.getName(), methodName,
                method.getName());

    }

    private void assertParameterWasAddedCorrectly(final Port portA, final Port portB) {

        Method methodWithParameter = this.getMethodOfPortCorrespondence(portA);

        Named portACorrespondence = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                portA, Named.class);
        Named portBCorrespondence = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                portB, Named.class);

        assertTrue("Invalid port correspondences!", portACorrespondence != null && portBCorrespondence != null);

        assertTrue(
                "Corresponding ASEM parameter for port " + portA.getName() + " was not added to method "
                        + methodWithParameter.getName(),
                methodWithParameter.getParameters().contains(portACorrespondence));
        assertTrue(
                "Corresponding ASEM parameter for port " + portB.getName() + " was not added to method "
                        + methodWithParameter.getName(),
                methodWithParameter.getParameters().contains(portBCorrespondence));
    }

    private void assertPortDeletionForASEMModule() {

        Collection<Port> portsToDelete = preparePorts(Module.class);

        for (Port port : portsToDelete) {
            this.doDeletionAndCheck(port);
        }
    }

    private void assertPortDeletionForASEMClass() {

        Collection<Port> portsToDelete = preparePorts(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

        for (Port port : portsToDelete) {
            this.doDeletionAndCheck(port);
        }
    }

    private void assertPortCorrespondenceDoesNotExist(final Port port) {

        final String msg = "Port correspondence for port " + port.getName() + " was not deleted!";

        try {

            TypedElement correspondence = ASEMSysMLHelper
                    .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port, TypedElement.class);
            assertEquals(msg, correspondence, null);

        } catch (Exception e) {
            fail(msg);
        }
    }

    private void doDeletionAndCheck(final Port port) {

        // Backup data for deletion check.
        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final TypedElement correspondingElement = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port, TypedElement.class);

        this.deletePortAndSync(port);
        this.assertPortMappingIsDeleted(port, block, correspondingElement);
    }

    private void assertPortMappingIsDeleted(final Port port, final Block block,
            final TypedElement correspondingElement) {

        // Check if the corresponding element was deleted.
        final Component component = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                block, Component.class);

        if (component instanceof Module) {

            Module module = (Module) component;
            boolean mappingExists = module.getTypedElements().contains(correspondingElement);

            assertTrue("Module " + module.getName() + " contains a port reference which shall be deleted!",
                    !mappingExists);

        } else if (component instanceof edu.kit.ipd.sdq.ASEM.classifiers.Class) {

            boolean mappingExists = false;
            edu.kit.ipd.sdq.ASEM.classifiers.Class asemClass = (edu.kit.ipd.sdq.ASEM.classifiers.Class) component;

            for (Method method : asemClass.getMethods()) {
                if ((method.getReturnType() != null && method.getReturnType().equals(correspondingElement))
                        || method.getParameters().contains(correspondingElement)) {
                    mappingExists = true;
                    break;
                }
            }

            assertTrue("Class " + asemClass.getName() + " contains a port reference which shall be deleted!",
                    !mappingExists);

        } else {
            logger.warn("Unsupported ASEM component type: " + component);
        }

        // Check if correspondence was deleted, too.
        assertPortCorrespondenceDoesNotExist(port);
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
                logger.info("INOUT ports are not mapped to ASEM models at the moment.");
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

    private void deletePortAndSync(final Port port) {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        EObject rootElementToSave = EcoreUtil.getRootContainer(port);
        Block block = ASEMSysMLHelper.getPortsBlock(port);

        ConnectorEnd connectorEnd = ASEMSysMLHelper.getConnectorEnd(port);
        Connector connector = ASEMSysMLHelper.getConnector(connectorEnd);
        BindingConnector bindingConnector = UMLUtil.getStereotypeApplication(connector, BindingConnector.class);
        Property property = ASEMSysMLTestHelper.getPortProperty(port);
        FlowProperty flowProperty = UMLUtil.getStereotypeApplication(property, FlowProperty.class);

        EcoreUtil.remove(connector);
        EcoreUtil.remove(bindingConnector);
        EcoreUtil.remove(property);
        EcoreUtil.remove(flowProperty);
        EcoreUtil.remove(port);
        saveAndSynchronizeChanges(rootElementToSave);

        assertTrue("Port was not deleted successfully!",
                (!sysmlModelResource.getContents().contains(port)
                        && !sysmlModelResource.getContents().contains(property)
                        && !sysmlModelResource.getContents().contains(connector)
                        && !block.getBase_Class().getOwnedPorts().contains(port)));

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

    private Method getMethodOfPortCorrespondence(final Port port) {

        Named portCorrespondence = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port,
                Named.class);

        assertTrue("Invalid port correspondence! Parent element of port correspondence is not a method.",
                portCorrespondence.eContainer() instanceof Method);

        return (Method) portCorrespondence.eContainer();
    }

    private int getNextUserInteractorSelection(final ASEMMethodMode mode) {

        return mode.ordinal();
    }

    private int getNextUserInteractorSelection(final Method method, final FlowDirection directionOfPortToAdd) {

        if (!(EcoreUtil.getRootContainer(method) instanceof Component)) {
            throw new IllegalArgumentException("Invalid root element of ASEM method " + method.getName());
        }

        final Component rootComponent = (Component) EcoreUtil.getRootContainer(method);
        final String asemProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(rootComponent.getName());
        final Resource asemResource = this.getModelResource(asemProjectModelPath);

        if (directionOfPortToAdd.equals(FlowDirection.OUT)) {
            return ASEMSysMLUserInteractionHelper
                    .getNextUserInteractorSelectionForASEMMethodSelectionForReturnTypes(method, asemResource);
        } else {
            return ASEMSysMLUserInteractionHelper
                    .getNextUserInteractorSelectionForASEMMethodSelectionForParameter(method, asemResource);
        }

    }
}
