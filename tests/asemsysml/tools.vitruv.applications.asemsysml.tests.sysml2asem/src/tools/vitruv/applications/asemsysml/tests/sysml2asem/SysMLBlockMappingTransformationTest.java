package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static tools.vitruv.applications.asemsysml.ASEMSysMLConstants.TEST_SYSML_MODEL_NAME;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.junit.Before;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLUserInteractionHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.correspondence.CorrespondenceModel;

/**
 * Class for all SysML block mapping tests. A SysML block will be mapped to an ASEM model containing
 * an ASEM component as root element.
 * 
 * @author Benjamin Rupp
 */
public class SysMLBlockMappingTransformationTest extends ASEMSysMLTest {

    private Block sysmlBlock;

    private final String sysmlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(TEST_SYSML_MODEL_NAME,
            SysMlNamspace.FILE_EXTENSION);

    @Override
    protected void init() {

        initializeSysMLAsSourceModel();

    }

    /**
     * Create a SysML block which is needed in all the test cases.
     */
    @Before
    public void setUp() {

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        int selection = ASEMSysMLUserInteractionHelper.getNextUserInteractorSelectionForASEMComponent(Module.class);
        this.testUserInteractor.addNextSelections(selection);

        this.sysmlBlock = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "SampleBlock", true, this);

    }

    /**
     * After adding a SysML block to a SysML model, a ASEM model should be created with a ASEM
     * component (e.g. a class or module) as root element. The user decides if a SysML block shall
     * be mapped to an ASEM class or an ASEM module.<br>
     * <br>
     * 
     * [Requirement 1.a)] [Requirement 2.a)]
     */
    @Test
    public void testIfASysMLBlockIsMappedToAnASEMComponent() {

        this.assertASEMComponentForSysMLBlockExists(sysmlBlock);

        this.assertExpectedASEMComponentType(Module.class);
        this.assertExpectedASEMComponentType(edu.kit.ipd.sdq.ASEM.classifiers.Class.class);

    }

    /**
     * A SysML block should only be transformed to an ASEM component if the
     * <code>isEncapsulated</code> attribute is set to <code>true</code>.<br>
     * <br>
     * 
     * [Requirement 1.b)]
     */
    @Test
    public void testEncapsulatedRestriction() {

        final Boolean isEncapsulated = false;

        Resource sysmlModelResource = this.getModelResource(this.sysmlProjectModelPath);
        Block blockWhichShouldNotBeTransformed = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource,
                "BlockWhichShouldNotBeTransformed", isEncapsulated, this);
        assertASEMModelDoesNotExistForSysMLBlock(blockWhichShouldNotBeTransformed);

    }

    /**
     * The name of the ASEM module should be equal to the name of the SysML block (the name of its
     * base class).<br>
     * <br>
     * 
     * [Requirement 1.c)]
     */
    @Test
    public void testIfNamesAreEqual() {

        this.assertSysMLBlockAndASEMModuleNamesAreEqual(sysmlBlock);

        final String newName = "NewBlockName";

        this.sysmlBlock.getBase_Class().setName(newName);
        this.saveAndSynchronizeChanges(this.sysmlBlock);

        // TODO [BR] Use assertSysMLBlockAndASEMModuleNamesAreEqual() method instead?!
        this.assertASEMComponentNameHasChangedAfterBlockNameChanged(sysmlBlock);

    }

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

    /**
     * If a SysML <code>block</code> corresponds to a ASEM <code>Module</code> its parts shall be
     * mapped to an <i>ASEM Module -> ASEM Module</i> reference (if the part of the SysML block
     * corresponds to a ASEM Module), or to an <i>ASEM Module -> ASEM Class</i> reference (if the
     * part of the SysML block corresponds to a ASEM Class). <br>
     * If the SysML <code>block</code> corresponds to a ASEM <code>Class</code> the references are
     * <i>ASEM Class -> ASEM Module</i> or <i>ASEM Class -> ASEM Class</i>. <br>
     * <br>
     * 
     * [Requirement 1.e)] [Requirement 1.f)] [Requirement 2.f)] [Requirement 2.g)]
     */
    @Test
    public void testIfPartsMappedCorrectly() {

        this.assertPartMappingExists();
        this.assertNestedPartMappingExists();
        this.assertNoPartMappingToModules();

        // TODO [BR] Check if part reference will be deleted if the part of block was deleted.

    }

    private void assertASEMComponentForSysMLBlockExists(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

    }

    private void assertSysMLBlockAndASEMModuleNamesAreEqual(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

        Component asemRootComponent = (Component) asemModelResource.getContents().get(0);

        assertTrue("Root component of ASEM module is not of the type 'ASEMModule'!",
                asemRootComponent instanceof Module);

        final String sysmlBlockName = this.sysmlBlock.getBase_Class().getName();
        final String asemModuleName = asemRootComponent.getName();

        assertEquals("The name of the ASEM module is not equal to the name of the SysML block!", asemModuleName,
                sysmlBlockName);

    }

    private void assertASEMComponentNameHasChangedAfterBlockNameChanged(final Block sysmlBlock) {

        Resource asemModelResource = this.getASEMModelResource(sysmlBlock.getBase_Class().getName());

        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, Component.class);

        Component asemRootComponent = (Component) asemModelResource.getContents().get(0);

        assertEquals("The name of the ASEM component is not equal to its SysML block name!",
                asemRootComponent.getName(), sysmlBlock.getBase_Class().getName());

    }

    private void assertASEMModelDoesNotExistForSysMLBlock(final Block block) {

        Resource asemModelResource = this.getASEMModelResource(block.getBase_Class().getName());
        final Boolean resourceDoesNotExist = (asemModelResource == null);

        assertTrue("An ASEM model does exist for SysML block " + block.getBase_Class().getName() + "!",
                resourceDoesNotExist);

    }

    private void assertExpectedASEMComponentType(final Class<? extends Component> expectedComponentType) {

        final String sysmlBlockName = "BlockTo" + expectedComponentType.getSimpleName();
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);

        int selection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(expectedComponentType);

        this.testUserInteractor.addNextSelections(selection);
        ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, sysmlBlockName, true, this);

        Resource asemModelResource = this.getASEMModelResource(sysmlBlockName);
        ASEMSysMLTestHelper.assertValidModelResource(asemModelResource, expectedComponentType);

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
                && portType.getAppliedStereotype("SysML::Blocks::Block") != null) {

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

    private void assertPartMappingExists() {
        /*
         * Add a block BlockA which has a part reference to a block BlockB. In ASEM a module cannot
         * be used as a subcomponent of another component. Therefore the BlockB must correspond with
         * a ASEM class, and NOT with a ASEM module.
         */

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;

        final int componentSelectionClass = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(asemComponentType);

        this.testUserInteractor.addNextSelections(componentSelectionClass, componentSelectionClass);

        Block blockA = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockA", true, this);
        Block blockB1 = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockB1", true, this);

        // Add a part property to BlockA. The aggregation kind is needed for the getParts() method.
        Property partPropertyB1 = blockA.getBase_Class().createOwnedAttribute("partReferenceB1",
                blockB1.getBase_Class());
        partPropertyB1.setAggregation(AggregationKind.COMPOSITE_LITERAL);

        saveAndSynchronizeChanges(blockA);

        assertTrue("Block A doesn't contain a part!", !blockA.getParts().isEmpty());

        // Check if the corresponding ASEM module of block A contains a reference to the
        // corresponding ASEM component of block B.
        Component componentA = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockA,
                asemComponentType);
        Component componentB1 = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockB1,
                asemComponentType);

        assertTrue("No corresponding element found for " + blockA.getBase_Class().getName(), componentA != null);
        assertTrue("No corresponding element found for " + blockB1.getBase_Class().getName(), componentB1 != null);

        ASEMSysMLTestHelper.assertPartReferenceExists(componentA, componentB1);

    }

    private void assertNestedPartMappingExists() {
        /*
         * Add a block BlockN1 which has a part reference to a block BlockN2 which itself has a part
         * reference to a third block BlockN3. In ASEM a module cannot be used as a subcomponent of
         * another component. Therefore the BlockN2 and BlockN3 must correspond with a ASEM class,
         * and NOT with a ASEM module.
         */

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;

        final int componentTypeSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(asemComponentType);

        this.testUserInteractor.addNextSelections(componentTypeSelection, componentTypeSelection,
                componentTypeSelection);
        Block blockN1 = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockN1", true, this);
        Block blockN2 = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockN2", true, this);
        Block blockN3 = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockN3", true, this);

        Property partPropertyN2 = blockN1.getBase_Class().createOwnedAttribute("partReferenceN2",
                blockN2.getBase_Class());
        partPropertyN2.setAggregation(AggregationKind.COMPOSITE_LITERAL);

        Property partPropertyN3 = blockN2.getBase_Class().createOwnedAttribute("partReferenceN3",
                blockN3.getBase_Class());
        partPropertyN3.setAggregation(AggregationKind.COMPOSITE_LITERAL);

        saveAndSynchronizeChanges(blockN1);
        saveAndSynchronizeChanges(blockN2);
        saveAndSynchronizeChanges(blockN3);

        assertTrue("BlockN1 doesn't contain a part!", !blockN1.getParts().isEmpty());
        assertTrue("BlockN2 doesn't contain a part!", !blockN2.getParts().isEmpty());

        Component componentN1 = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockN1,
                asemComponentType);
        Component componentN2 = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockN2,
                asemComponentType);
        Component componentN3 = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockN3,
                asemComponentType);

        assertTrue("No corresponding element found for " + blockN1.getBase_Class().getName(), componentN1 != null);
        assertTrue("No corresponding element found for " + blockN2.getBase_Class().getName(), componentN2 != null);
        assertTrue("No corresponding element found for " + blockN3.getBase_Class().getName(), componentN3 != null);

        ASEMSysMLTestHelper.assertPartReferenceExists(componentN1, componentN2);
        ASEMSysMLTestHelper.assertPartReferenceExists(componentN2, componentN3);
    }

    private void assertNoPartMappingToModules() {
        /*
         * An ASEM module can not be used as a subcomponent in other ASEM components. Therefore
         * SysML part references to a SysML block which corresponds to an ASEM module must be
         * ignored.
         */
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = Module.class;

        final int componentTypeSelection = ASEMSysMLUserInteractionHelper
                .getNextUserInteractorSelectionForASEMComponent(asemComponentType);

        this.testUserInteractor.addNextSelections(componentTypeSelection, componentTypeSelection);

        Block blockA = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockWithModuleAsPart", true, this);
        Block blockB = ASEMSysMLTestHelper.createSysMLBlock(sysmlModelResource, "BlockAsModule", true, this);

        Property partPropertyB = blockA.getBase_Class().createOwnedAttribute("partReferenceB", blockB.getBase_Class());
        partPropertyB.setAggregation(AggregationKind.COMPOSITE_LITERAL);

        saveAndSynchronizeChanges(blockA);
        saveAndSynchronizeChanges(blockB);

        assertTrue("BlockA doesn't contain a part!", !blockA.getParts().isEmpty());

        Component componentA = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockA,
                asemComponentType);
        Component componentB = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockB,
                asemComponentType);

        boolean partReferenceMappingExists = false;

        for (TypedElement typedElement : componentA.getTypedElements()) {
            if (typedElement.getType().equals(componentB)) {
                partReferenceMappingExists = true;
            }
        }

        assertTrue("A part reference to an ASEM module exists! ", !partReferenceMappingExists);

    }
}
