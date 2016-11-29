package tools.vitruv.applications.asemsysml.tests.sysml2asem.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.papyrus.sysml14.portsandflows.PortsandflowsPackage;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil.StereotypeApplicationHelper;

import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.java.sysml2asem.global.SysML2ASEMJavaChangePropagationSpecification;
import tools.vitruv.applications.asemsysml.reactions.sysml2asem.global.SysML2ASEMChangePropagationSpecification;
import tools.vitruv.framework.change.processing.ChangePropagationSpecification;
import tools.vitruv.framework.tests.TestUserInteractor;

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
     * Get the change propagation specifications of the transformation type which should be used for
     * the transformations.
     * 
     * @param transformationType
     *            The {@link TransformationType type of the transformation} which should be used.
     * @return The change propagation specifications for the given transformation type. If no change
     *         propagation specification is available <code>null</code> is returned.
     * @see ChangePropagationSpecification
     */
    public static Iterable<ChangePropagationSpecification> getChangePropagationSpecificationsByTransformationType(
            final TransformationType transformationType) {

        switch (transformationType) {
        case REACTIONS:
            return Collections.singletonList(new SysML2ASEMChangePropagationSpecification());

        case JAVA:
            return Collections.singletonList(new SysML2ASEMJavaChangePropagationSpecification());

        default:
            return null;
        }

    }

    /**
     * Get all available ports of the given SysML model resource.
     * 
     * @param sysmlModelResource
     *            The SysML model resource.
     * @return A set of available ports.
     */
    public static Collection<Port> getSysMLPorts(final Resource sysmlModelResource) {

        assertValidModelResource(sysmlModelResource, Model.class);

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
     * Create a SysML block and add it to the SysML model.
     * 
     * @param sysmlModelResource
     *            SysML model resource.
     * @param blockName
     *            Name of the SysML block to add.
     * @param isEncapsulated
     *            Encapsulated flag, see {@link Block#isEncapsulated()}
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The created {@link Block SysML Block}.
     * 
     * @see Block#isEncapsulated
     */
    public static Block createSysMLBlock(Resource sysmlModelResource, final String blockName,
            final Boolean isEncapsulated, final ASEMSysMLTest testCaseClass) {

        assertValidModelResource(sysmlModelResource, Model.class);

        Model sysmlRootModel = getSysMLRootModelElement(sysmlModelResource);

        // Create a SysML block with its base class.
        Class baseClass = sysmlRootModel.createOwnedClass(blockName, false);
        Block sysmlBlock = (Block) StereotypeApplicationHelper.getInstance(null).applyStereotype(baseClass,
                BlocksPackage.eINSTANCE.getBlock());
        sysmlBlock.setIsEncapsulated(isEncapsulated);

        testCaseClass.saveAndSynchronizeChanges(sysmlRootModel);

        return sysmlBlock;

    }

    /**
     * Add a UML port with the given name and the given flow direction to an existing block. The
     * method will save and synchronize the model, too.
     * 
     * @param block
     *            The SysML block to which the port shall be added to.
     * @param portName
     *            The name of the port.
     * @param flowDirection
     *            The flow direction of the port.
     * @param testCaseClass
     *            Test case class. Needed for accessing synchronization method.
     * @return The added port.
     * 
     * @see FlowDirection
     */
    public static Port addPortToBlockAndSync(final Block block, final String portName,
            final FlowDirection flowDirection, final ASEMSysMLTest testCaseClass) {

        Port port = addPortToBlock(block, portName, flowDirection);
        testCaseClass.saveAndSynchronizeChanges(port);

        return port;

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
        ASEMSysMLTestHelper.assertResourceExists(modelResource);
        ASEMSysMLTestHelper.assertRootElementExists(modelResource);
        ASEMSysMLTestHelper.assertRootElementIsTypeOf(modelResource, rootElementClass);
    }

    /**
     * Get the number of the given ASEM component type which has to be used to set the next
     * selection of the {@link TestUserInteractor}.
     * 
     * @param expectedComponentType
     *            Type of the ASEM component which should be selected for the transformation of a
     *            SysML block.
     * @return The magic number for the {@link TestUserInteractor}
     * @see TestUserInteractor#addNextSelections(Integer...)
     */
    public static int getNextUserInteractorSelectionAsNumber(
            final java.lang.Class<? extends Component> expectedComponentType) {
        // FIXME [BR] Remove magic numbers!
        // The order must be the same as in the transformation!

        int selection = 0;
        if (expectedComponentType.getSimpleName().equals("Module")) {
            selection = 0;
        } else if (expectedComponentType.getSimpleName().equals("Class")) {
            selection = 1;
        }

        return selection;
    }

    private static Model getSysMLRootModelElement(final Resource sysmlModelResource) {
        Model sysmlRootModel = (Model) sysmlModelResource.getContents().get(0);
        assertFalse("The SysML profil must be applied.", sysmlRootModel.getAppliedProfiles().isEmpty());

        return sysmlRootModel;
    }

    private static Port addPortToBlock(final Block block, final String portName, final FlowDirection flowDirection) {

        // 1) Set the port type.
        // We add a UML port to the block, not a SysML (full or proxy) port! Therefore we have to
        // use a other block or a primitive type as port type.
        // TODO [BR] At the moment the block of the port is used as type. If a more realistic usage
        // is needed, the following part should be reworked.
        Type portType = block.getBase_Class();

        // 2) Add SysML flow property to block.
        Property portProperty = block.getBase_Class().createOwnedAttribute(portName + "Property", portType);
        FlowProperty flowProperty = (FlowProperty) StereotypeApplicationHelper.getInstance(null)
                .applyStereotype(portProperty, PortsandflowsPackage.eINSTANCE.getFlowProperty());
        flowProperty.setDirection(flowDirection);

        // 3) Create UML port for the block.
        Port port = block.getBase_Class().createOwnedPort(portName, portType);

        // 4) Create SysML binding connector between property and port.
        Connector connector = block.getBase_Class()
                .createOwnedConnector("BindingConnector_" + portProperty.getName() + "<->" + port.getName());
        ConnectorEnd propertyEnd = connector.createEnd();
        propertyEnd.setRole(portProperty);
        ConnectorEnd portEnd = connector.createEnd();
        portEnd.setRole(port);

        StereotypeApplicationHelper.getInstance(null).applyStereotype(connector,
                BlocksPackage.eINSTANCE.getBindingConnector());

        return port;
    }
}
