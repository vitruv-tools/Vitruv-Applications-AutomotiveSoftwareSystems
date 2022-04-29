package tools.vitruv.applications.asemsysml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.correspondence.Correspondence;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.util.bridges.EcoreResourceBridge;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * Global helper class which can be used in the reflections and in the test cases.
 * 
 * @author Benjamin Rupp
 */
public class ASEMSysMLHelper {

    /**
     * Get the project model path for a given model (model name and file extension).
     * 
     * @param modelName
     *            Name of the model.
     * @param fileExtension
     *            Associated file extension of the model.
     * @return Project model path.
     */
    public static String getProjectModelPath(final String modelName, final String fileExtension) {
        return (((ASEMSysMLConstants.MODEL_DIR_NAME + File.separatorChar + modelName) + ".") + fileExtension);
    }

    /**
     * Get the ASEM model name which is composed of the {@link #TEST_ASEM_MODEL_NAME_PREFIX ASEM
     * model prefix}, a {@link #TEST_ASEM_MODEL_NAME_SEPARATOR separator} and the given SysML block
     * name.
     * 
     * @param blockName
     *            Name of the SysML block for which the ASEM model exists.
     * @return The complete ASEM model name.
     */
    public static final String getASEMModelName(final String blockName) {
        return ASEMSysMLConstants.TEST_ASEM_MODEL_NAME_PREFIX + ASEMSysMLConstants.TEST_ASEM_MODEL_NAME_SEPARATOR
                + blockName;
    }

    /**
     * Get the project model path for the ASEM model which corresponds to a SysML Block.
     * 
     * @param blockName
     *            Name of the corresponding SysML block.
     * @return Project model path of the ASEM model.
     */
    public static String getASEMProjectModelPath(final String blockName) {

        String modelName = ASEMSysMLHelper.getASEMModelName(blockName);
        String projectModelPath = ASEMSysMLHelper.getProjectModelPath(modelName, AsemNamespace.FILE_EXTENSION);

        return projectModelPath;

    }

    /**
     * Get the VURI of a model with the given project model path.
     * 
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @param projectModelPath
     *            The project model path which starts with
     *            {@link ASEMSysMLConstants#MODEL_DIR_NAME}.
     * @return The VURI of the model.
     */
    public static VURI getModelVURI(final EObject alreadyPersistedObject, final String projectModelPath) {

        String existingElementURI = VURI.getInstance(alreadyPersistedObject.eResource()).getEMFUri().toFileString();
        String uriPrefix = existingElementURI.substring(0,
                existingElementURI.lastIndexOf(ASEMSysMLConstants.MODEL_DIR_NAME + java.io.File.separatorChar));
        String uriString = uriPrefix + projectModelPath;
        VURI modelVURI = VURI.getInstance(URI.createFileURI(uriString));
        return modelVURI;
    }

    /**
     * Get the model resource of the given project model path. The resource must be contained in the
     * same resource set as the correspondence model!
     * 
     * @param correspondenceModel
     *            The correspondence model to get the resource set.
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @param projectModelPath
     *            The project model path which starts with
     *            {@link ASEMSysMLConstants#MODEL_DIR_NAME}.
     * @return The model resource.
     */
    public static Resource getModelResource(final CorrespondenceModel correspondenceModel,
            final EObject alreadyPersistedObject, final String projectModelPath) {

        ResourceSet rs = correspondenceModel.getResource().getResourceSet();
        VURI modelVURI = ASEMSysMLHelper.getModelVURI(alreadyPersistedObject, projectModelPath);

        return rs.getResource(modelVURI.getEMFUri(), false);
    }

    /**
     * Get the SysML model which is the root element of the SysML model resource.
     * 
     * @param correspondenceModel
     *            The correspondence model to get the resource set.
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @return The SysML model element or <code>null</code> if no or multiple SysML model element(s)
     *         exists as root element(s).
     */
    public static Model getSysMLModel(final CorrespondenceModel correspondenceModel,
            final EObject alreadyPersistedObject) {

        final String sysmlProjectModelPath = getProjectModelPath(ASEMSysMLConstants.TEST_SYSML_MODEL_NAME,
                SysMlNamspace.FILE_EXTENSION);
        final Resource sysmlResource = getModelResource(correspondenceModel, alreadyPersistedObject,
                sysmlProjectModelPath);

        try {

            return EcoreResourceBridge.getUniqueTypedRootEObject(sysmlResource,
                    ASEMSysMLConstants.TEST_SYSML_MODEL_NAME, Model.class);

        } catch (RuntimeException re) {
            return null;
        }
    }

    /**
     * Get all ASEM elements which correspond to the given SysML element.
     * 
     * @param correspondenceModel
     *            The correspondence model.
     * @param sysmlElement
     *            The SysML element.
     * @return Corresponding ASEM elements or an empty collection if no corresponding elements
     *         exist.
     */
    public static Collection<EObject> getCorrespondingASEMElements(final CorrespondenceModel correspondenceModel,
            final EObject sysmlElement) {

        Collection<EObject> correspondingASEMElements = Collections.emptyList();
        Set<Correspondence> correspondences = Collections.emptySet();

        correspondences = correspondenceModel.getCorrespondences(Collections.singletonList(sysmlElement));

        for (Correspondence correspondence : correspondences) {
            correspondingASEMElements = correspondence.getElementsForMetamodel(AsemNamespace.METAMODEL_NAMESPACE);
        }

        return correspondingASEMElements;
    }

    /**
     * Get all SysML elements which correspond to the given ASEM element.
     * 
     * @param correspondenceModel
     *            The correspondence model.
     * @param asemElement
     *            The ASEM element.
     * @return Corresponding SysML elements or an empty collection if no corresponding elements
     *         exist.
     */
    public static Collection<EObject> getCorrespondingSysMLElements(final CorrespondenceModel correspondenceModel,
            final EObject asemElement) {

        Collection<EObject> correspondingSysMLElements = Collections.emptyList();
        Set<Correspondence> correspondences = Collections.emptySet();

        correspondences = correspondenceModel.getCorrespondences(Collections.singletonList(asemElement));

        for (Correspondence correspondence : correspondences) {
            correspondingSysMLElements = correspondence.getElementsForMetamodel(SysMlNamspace.METAMODEL_NAMESPACE);
        }

        return correspondingSysMLElements;
    }

    /**
     * Get the first corresponding ASEM element of the given type for a SysML element. If there are
     * multiple ASEM elements corresponding to the given SysML element, the first of them will be
     * returned.
     * 
     * @param <T>
     *            Expected type of the corresponding ASEM element.
     * @param correspondenceModel
     *            The correspondence model.
     * @param sysmlElement
     *            The SysML element which corresponds with a ASEM element.
     * @param asemElementType
     *            The type of the corresponding ASEM element.
     * @return The first element of the corresponding ASEM elements or <code>null</code> if no
     *         corresponding element of the given type was found.
     */
    public static <T> T getFirstCorrespondingASEMElement(final CorrespondenceModel correspondenceModel,
            final EObject sysmlElement, final Class<T> asemElementType) {

        Collection<EObject> correspondingASEMElements = ASEMSysMLHelper
                .getCorrespondingASEMElements(correspondenceModel, sysmlElement);

        return getFirstCorrespondingElement(correspondingASEMElements, asemElementType);
    }

    /**
     * Get the first corresponding SysML element of the given type for a ASEM element. If there are
     * multiple SysML elements corresponding to the given ASEM element, the first of them will be
     * returned.
     * 
     * @param <T>
     *            Expected type of the corresponding SysML element.
     * @param correspondenceModel
     *            The correspondence model.
     * @param asemElement
     *            The SysML element which corresponds with a ASEM element.
     * @param sysmlElementType
     *            The type of the corresponding ASEM element.
     * @return The first element of the corresponding ASEM elements or <code>null</code> if no
     *         corresponding element of the given type was found.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFirstCorrespondingSysMLElement(final CorrespondenceModel correspondenceModel,
            final EObject asemElement, final Class<T> sysmlElementType) {

        Collection<EObject> correspondingSysMLElements = ASEMSysMLHelper
                .getCorrespondingSysMLElements(correspondenceModel, asemElement);

        if (sysmlElementType.isAssignableFrom(Block.class)) {

            org.eclipse.uml2.uml.Class baseClass = getFirstCorrespondingElement(correspondingSysMLElements,
                    org.eclipse.uml2.uml.Class.class);

            if (baseClass == null) {
                return null;
            }

            Block block = UMLUtil.getStereotypeApplication(baseClass, Block.class);
            return (T) block;

        } else {

            return getFirstCorrespondingElement(correspondingSysMLElements, sysmlElementType);

        }

    }

    @SuppressWarnings("unchecked")
    private static <T> T getFirstCorrespondingElement(final Collection<EObject> correspondingElements,
            final Class<T> correspondingElementType) {

        for (EObject correspondingElement : correspondingElements) {

            if (correspondingElementType.isAssignableFrom(correspondingElement.getClass())) {

                return (T) correspondingElement;

            }
        }

        return null;
    }

    /**
     * Get the block a given port belongs to.
     * 
     * @param port
     *            The given port.
     * @return The block the given port belongs to or <code>null</code> if the port does not belong
     *         to a block.
     */
    public static Block getPortsBlock(final Port port) {

        Element portOwner = port.getOwner();

        if (!(portOwner instanceof org.eclipse.uml2.uml.Class)) {
            return null;
        }

        org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) portOwner;
        return (Block) UMLUtil.getStereotypeApplication(baseClass, Block.class);

    }

    /**
     * This method checks if the given property represents a property of an UML port. This is needed
     * to distinguish between properties of a port and a part reference.
     * 
     * @param property
     *            The property to check.
     * @return <code>True</code> if the property is a property of an UML port, otherwise
     *         <code>false</code>.
     */
    public static boolean isPropertyAPortProperty(final Property property) {

        boolean isPort = false;

        for (ConnectorEnd propertyEnd : property.getEnds()) {
            final Connector connector = getConnector(propertyEnd);
            for (ConnectorEnd connectorEnd : connector.getEnds()) {
                if (connectorEnd.getRole() instanceof Port) {
                    isPort = true;
                }
            }
        }

        return isPort;
    }

    /**
     * Get the classifier for an ASEM variable depending on the given port type.
     * 
     * @param portType
     *            Type of a SysML port.
     * @param correspondenceModel
     *            Current correspondence model.
     * @return The classifier for the ASEM variable which corresponds with the SysML port.
     */
    public static Classifier getClassifierForASEMVariable(final Type portType,
            final CorrespondenceModel correspondenceModel) {

        if (portType instanceof org.eclipse.uml2.uml.Class) {
            org.eclipse.uml2.uml.Class baseClass = (org.eclipse.uml2.uml.Class) portType;
            Block portTypeBlock = UMLUtil.getStereotypeApplication(baseClass, Block.class);

            Component correspondingComponent = ASEMSysMLHelper.getFirstCorrespondingASEMElement(correspondenceModel,
                    portTypeBlock, Component.class);

            return correspondingComponent;

        } else if (portType instanceof PrimitiveType) {

            final PrimitiveType primitivePortType = (PrimitiveType) portType;

            final Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> primitiveMessageType;
            primitiveMessageType = ASEMSysMLPrimitiveTypeHelper.PRIMITIVE_TYPE_MAP.get(primitivePortType);

            final edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType primitiveVariableType = ASEMSysMLPrimitiveTypeHelper
                    .getASEMPrimitiveTypeFromRepository(primitiveMessageType, portType,
                            correspondenceModel.getResource().getResourceSet());

            return primitiveVariableType;
        }

        return null;
    }

    /**
     * Get the flow property of a given port.
     * 
     * @param port
     *            The given port.
     * @return The flow property of the given port or <code>null</code> if no flow property exists.
     */
    public static FlowProperty getFlowProperty(final Port port) {

        FlowProperty flowProperty = null;
        flowProperty = UMLUtil.getStereotypeApplication(port, FlowProperty.class);

        return flowProperty;

    }

    /**
     * Get the connector end of a port. If there exists more than on connector end the first one
     * will be returned at the moment.
     * 
     * @param port
     *            The port which must have at least on connector end specified.
     * @return The first connector end of the given port.
     */
    public static ConnectorEnd getConnectorEnd(final Port port) {
        if (port.getEnds().isEmpty()) {
            throw new IllegalArgumentException("The given port has no connector ends.");
        }
        // TODO [BR] A port can have multiple connector ends. If this this is necessary, this method
        // must be reworked.
        return port.getEnds().get(0);
    }

    /**
     * Get the connector the given connector end belongs to.
     * 
     * @param connectorEnd
     *            The given connector end.
     * @return The connector the given connector end belongs to.
     */
    public static Connector getConnector(final ConnectorEnd connectorEnd) {

        EObject container = connectorEnd.eContainer();

        if (!(container instanceof Connector)) {
            throw new IllegalArgumentException("The eContainer of the connector end is not an instance of Connector!");
        }

        return (Connector) container;
    }

    /**
     * Get all available ASEM methods in the given ASEM model resource.
     * 
     * @param asemResource
     *            The ASEM model resource.
     * @return All available ASEM methods in this model resource.
     */
    public static List<Method> getAllASEMMethods(final Resource asemResource) {

        List<Method> methods = new ArrayList<Method>();

        for (EObject object : asemResource.getContents()) {
            if (object instanceof Component) {
                Component component = (Component) object;
                methods.addAll(component.getMethods());
            }
        }

        return methods;
    }

    /**
     * Get all available ASEM methods in the given ASEM model resource which have no return type.
     * 
     * @param asemResource
     *            The ASEM model resource.
     * @return All available ASEM methods in this model resource which have no return type.
     */
    public static List<Method> getAllASEMMethodsWithoutReturnType(final Resource asemResource) {

        List<Method> methods = getAllASEMMethods(asemResource);
        Collection<Method> methodsToRemove = new HashSet<>();

        for (Method method : methods) {
            if (method.getReturnType() != null) {
                methodsToRemove.add(method);
            }
        }

        methods.removeAll(methodsToRemove);

        return methods;
    }

    /**
     * Returns whether the given ASEM resource contains at least one ASEM method or not.
     * 
     * @param asemResource
     *            The ASEM model resource.
     * @return <code>True</code> if the ASEM model resource contains at least one ASEM method
     *         element, otherwise <code>false</code>.
     */
    public static boolean areMethodsAvailable(final Resource asemResource) {
        return !(getAllASEMMethods(asemResource).isEmpty());
    }

    /**
     * Returns whether the given ASEM resource contains at least one ASEM method which has no return
     * type or not.
     * 
     * @param asemResource
     *            The ASEM model resource.
     * @return <code>True</code> if the ASEM model resource contains at least one ASEM method
     *         element which has no return type, otherwise <code>false</code>.
     */
    public static boolean areMethodsWithoutReturnTypeAvailable(final Resource asemResource) {
        return !(getAllASEMMethodsWithoutReturnType(asemResource).isEmpty());
    }
}
