package tools.vitruv.applications.asemsysml.java.sysml2asem.util;

import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.util.UMLUtil;

import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.framework.correspondence.CorrespondenceModel;

/**
 * Helper class containing useful methods for all the java transformation classes.
 * 
 * @author Benjamin Rupp
 *
 */
public final class ASEMSysMLJavaHelper {

    /** Utility classes should not have a public or default constructor. */
    private ASEMSysMLJavaHelper() {
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
                    .getASEMPrimitiveTypeFromRepository(primitiveMessageType, portType);

            return primitiveVariableType;
        }

        return null;
    }

}
