package tools.vitruv.applications.asemsysml;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.util.UMLUtil;

import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.correspondence.Correspondence;
import tools.vitruv.framework.correspondence.CorrespondenceModel;

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
        return (((ASEMSysMLConstants.MODEL_DIR_NAME + "/" + modelName) + ".") + fileExtension);
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
     * Get all ASEM elements which correspond to the given SysML element.
     * 
     * @param correspondenceModel
     *            The correspondence model.
     * @param sysmlElement
     *            The SysML element.
     * @return Corresponding ASEM elements or <code>null</code> if no corresponding elements exist.
     */
    public static Collection<EObject> getCorrespondingASEMElements(final CorrespondenceModel correspondenceModel,
            final EObject sysmlElement) {

        Collection<EObject> correspondingASEMElements = null;
        Set<Correspondence> correspondences = null;

        correspondences = correspondenceModel.getCorrespondences(Collections.singletonList(sysmlElement));

        for (Correspondence correspondence : correspondences) {
            correspondingASEMElements = correspondence.getElementsForMetamodel(AsemNamespace.METAMODEL_NAMESPACE);
        }

        return correspondingASEMElements;
    }

    /**
     * Get the first corresponding ASEM element for a SysML element. If there are multiple ASEM
     * elements corresponding to the given SysML element, the first of them will be returned.
     * 
     * @param sysmlElement
     *            The SysML element which corresponds with a ASEM element.
     * @param asemElementType
     *            The type of the corresponding ASEM element.
     * @return The first element of the corresponding ASEM elements.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFirstCorrespondingASEMElement(final CorrespondenceModel correspondenceModel,
            final EObject sysmlElement, final Class<T> asemElementType) {

        Collection<EObject> correspondingASEMElements = ASEMSysMLHelper
                .getCorrespondingASEMElements(correspondenceModel, sysmlElement);

        for (EObject correspondingASEMElement : correspondingASEMElements) {

            if (asemElementType.isAssignableFrom(correspondingASEMElement.getClass())) {

                // To get only one message element, return the first element which is part of
                // the corresponding elements. If there is a case where a port can have multiple
                // corresponding messages, this section must be reworked.
                return (T) correspondingASEMElement;

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
     * Get the flow property of a given port.
     * 
     * @param port
     *            The given port.
     * @return The flow property of the given port or <code>null</code> if no flow property exists.
     */
    public static FlowProperty getFlowProperty(final Port port) {

        FlowProperty flowProperty = null;
        Block block = getPortsBlock(port);
        if (block == null) {
            return null;
        }

        // TODO [BR] A port can have multiple connector ends. If this differentiation is necessary,
        // the following part should be reworked.
        ConnectorEnd portsConnectorEnd = port.getEnds().get(0);
        Connector connector = (Connector) portsConnectorEnd.eContainer();
        EList<ConnectorEnd> connectorEnds = connector.getEnds();

        for (ConnectorEnd connectorEnd : connectorEnds) {

            flowProperty = UMLUtil.getStereotypeApplication(connectorEnd.getRole(), FlowProperty.class);

            if (flowProperty != null) {
                break;
            }
        }

        return flowProperty;

    }

}
