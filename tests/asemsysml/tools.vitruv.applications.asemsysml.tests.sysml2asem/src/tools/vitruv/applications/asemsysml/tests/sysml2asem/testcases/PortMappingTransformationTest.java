package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper.ASEMMethodMode;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

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

        getUserInteractor().addNextSelections("MethodName1", "MethodName2", "MethodName3", "MethodName4", "MethodName5",
                "MethodName6");

        this.assertPortMappingForASEMModuleExists();
        this.assertPortMappingForASEMClassExists();
    }

    /**
     * If a SysML port is deleted, the corresponding ASEM element must be deleted, too.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfPortMappingIsRemovedAfterPortDeletion() throws IOException {

        getUserInteractor().addNextSelections("MethodName1", "MethodName2", "MethodName3", "MethodName4", "MethodName5",
                "MethodName6");

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
        Block blockA = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockA", true,
                asemComponentType, this);

        // Add a port PortX to the block, to test if the correct method is selected by the user
        // interacting.
        final int parameterModeSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.CREATE_NEW);
        getUserInteractor().addNextSelections(parameterModeSelection);
        getUserInteractor().addNextSelections("MethodWithoutSecondParameter");
        ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(blockA, "PortX", FlowDirection.IN, pInteger, this);

        // Add a port PortA which will be mapped to an ASEM parameter in a new ASEM method.
        final int parameterModeSelectionA = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.CREATE_NEW);
        getUserInteractor().addNextSelections(parameterModeSelectionA);
        getUserInteractor().addNextSelections(methodName);
        final Port portA = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(blockA, "PortA", FlowDirection.IN,
                pInteger, this);

        // Add a port PortB which will be mapped to an ASEM parameter of the ASEM method of PortA.
        final int parameterModeSelectionB = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.USE_EXISTING);
        final int methodSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodSelection(this.getMethodOfPortCorrespondence(portA),
                        FlowDirection.IN, this.getCorrespondenceModel());
        getUserInteractor().addNextSelections(parameterModeSelectionB, methodSelection);
        final Port portB = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(blockA, "PortB", FlowDirection.IN,
                pInteger, this);

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
        Block block = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "SampleBlock", true,
                asemComponentType, this);

        // Add a port PortA with direction 'in' which will be mapped to an ASEM parameter in a new
        // ASEM method. The created method will NOT have a return type.
        final int parameterModeSelectionA = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.CREATE_NEW);
        getUserInteractor().addNextSelections(parameterModeSelectionA);
        getUserInteractor().addNextSelections("MethodWithoutReturnValue");
        Port portA = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "PortA", FlowDirection.IN, pInteger,
                this);

        // Add a port PortB with direction 'out' which will be mapped to an ASEM return type in a
        // new ASEM method. The created method will have a return type.
        final int parameterModeSelectionB = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.CREATE_NEW);
        getUserInteractor().addNextSelections(parameterModeSelectionB);
        getUserInteractor().addNextSelections("MethodWithReturnValue");
        Port portB = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "PortB", FlowDirection.OUT, pInteger,
                this);

        // Add a port PortC with direction 'out' which shall be mapped to an ASEM return type which
        // will be added to the existing method of PortA.
        final int parameterModeSelectionC = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodMode(ASEMMethodMode.USE_EXISTING);
        final int methodSelectionC = ASEMSysMLUserInteractionHelper
                .getNextUserInteractionSelectionForASEMMethodSelection(this.getMethodOfPortCorrespondence(portA),
                        FlowDirection.OUT, this.getCorrespondenceModel());
        getUserInteractor().addNextSelections(parameterModeSelectionC, methodSelectionC);
        Port portC = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "PortC", FlowDirection.OUT, pInteger,
                this);

        // Try to add a return type to an existing method of PortB. This attempt must be fail.
        try {
            // Check user interacting.
            // The message of PortB must NOT be part of the list of available methods in the user
            // interacting dialog.
            Method methodB = this.getMethodOfPortCorrespondence(portB);
            ASEMSysMLUserInteractionHelper.getNextUserInteractionSelectionForASEMMethodSelection(methodB,
                    FlowDirection.OUT, this.getCorrespondenceModel());

            fail("The method " + methodB.getName()
                    + " must not be part of the allowed methods because it already has a return type!");

        } catch (IllegalArgumentException iae) {
        }

        // Check transformation result.
        this.assertMethodsForReturnTypeCheckWereAddedCorrectly(block, portA, portB);
        this.assertReturnTypeWasAddedCorrectly(portA, portC);

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

    private void assertReturnTypeWasAddedCorrectly(final Port portA, final Port portC) {

        Method methodA = this.getMethodOfPortCorrespondence(portA);
        Method methodC = this.getMethodOfPortCorrespondence(portC);

        assertEquals("Return type corresponding to port " + portC.getName() + " was not added to the method "
                + methodA.getName(), methodA, methodC);

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

    private void assertPortDeletionForASEMModule() throws IOException {

        Collection<Port> portsToDelete = preparePorts(Module.class);

        for (Port port : portsToDelete) {
            this.doDeletionAndCheck(port);
        }
    }

    private void assertPortDeletionForASEMClass() throws IOException {

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

    private void doDeletionAndCheck(final Port port) throws IOException {

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

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Collection<Port> portsToTest = preparePorts(Module.class);

        for (Port port : portsToTest) {

            ASEMSysMLAssertionHelper.assertPortWasTransformedAsExpected(port, sysmlModelResource,
                    this.getCorrespondenceModel());

        }
    }

    private void assertPortMappingForASEMClassExists() {

        getUserInteractor().addNextSelections("MethodName1", "MethodName2", "MethodName3", "MethodName4", "MethodName5",
                "MethodName6");

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Collection<Port> portsToTest = preparePorts(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

        for (Port port : portsToTest) {

            ASEMSysMLAssertionHelper.assertPortWasTransformedAsExpected(port, sysmlModelResource,
                    this.getCorrespondenceModel());

        }
    }

    private Collection<Port> preparePorts(final Class<? extends Component> asemComponentType) {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        // Add a block which owns all ports for this test.
        final String blockName = "BlockWithPortFor" + asemComponentType.getSimpleName();
        Block block = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, blockName, true,
                asemComponentType, this);

        // The different port types to test.
        // TODO [BR] String and unlimited natural are ignored at the moment.
        Type blockType = block.getBase_Class();
        PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_BOOLEAN;
        PrimitiveType pInteger = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_INTEGER;
        PrimitiveType pReal = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_REAL;

        // Add ports and test if their transformation was successfully.
        Collection<Port> portsToTest = new HashSet<Port>();
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SamplePortIN", FlowDirection.IN,
                blockType, this));
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SamplePortOUT", FlowDirection.OUT,
                blockType, this));
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SamplePortINOUT",
                FlowDirection.INOUT, blockType, this));
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SampleBooleanPortIN",
                FlowDirection.IN, pBoolean, this));
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SampleIntegerPortIN",
                FlowDirection.IN, pInteger, this));
        portsToTest.add(ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(block, "SampleRealPortIN", FlowDirection.IN,
                pReal, this));

        return portsToTest;
    }

    private void deletePortAndSync(final Port port) throws IOException {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        EObject rootElementToSave = EcoreUtil.getRootContainer(port);
        Block block = ASEMSysMLHelper.getPortsBlock(port);

        FlowProperty flowProperty = UMLUtil.getStereotypeApplication(port, FlowProperty.class);

        EcoreUtil.delete(flowProperty);
        EcoreUtil.delete(port);
        saveAndSynchronizeChanges(rootElementToSave);

        assertTrue("Port was not deleted successfully!", (!sysmlModelResource.getContents().contains(port)
                && !block.getBase_Class().getOwnedPorts().contains(port)));

    }

    private Method getMethodOfPortCorrespondence(final Port port) {

        Named portCorrespondence = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), port,
                Named.class);

        assertTrue("Invalid port correspondence! Parent element of port correspondence is not a method.",
                portCorrespondence.eContainer() instanceof Method);

        return (Method) portCorrespondence.eContainer();
    }
}
