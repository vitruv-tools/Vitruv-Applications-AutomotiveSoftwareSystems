package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.classifiers.Classifier;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.dataexchange.Variable;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.domains.asem.AsemNamespace;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.ReplaceSingleValuedEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the type of a port.<br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link ReplaceSingleValuedEReference} change which will
 * be triggered if the <code>type</code> of a port was set. <br>
 * <br>
 * 
 * [Requirement 1.d)iii]
 * 
 * @author Benjamin Rupp
 *
 */
public class PortTypeTransformation
        extends AbstractTransformationRealization<ReplaceSingleValuedEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PortTypeTransformation.class);

    public PortTypeTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEReference.class;
    }

    @Override
    protected void executeTransformation(ReplaceSingleValuedEReference<EObject, EObject> change) {

        logger.info("[ASEMSysML][JAVA] Transforming port type ...");

        Port port = (Port) change.getAffectedEObject();
        Type portType = port.getType();

        final Block block = ASEMSysMLHelper.getPortsBlock(port);
        final Component correspondingASEMComponent = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), block, Component.class);

        if (correspondingASEMComponent instanceof edu.kit.ipd.sdq.ASEM.classifiers.Class) {
            // In this case, the mapping of the port is direction specific. Therefore the
            // transformation will be handled in the PortDirectionTransformation class.
            return;
        }

        Classifier variableType = ASEMSysMLHelper.getClassifierForASEMVariable(portType,
                this.executionState.getCorrespondenceModel());

        // Set variable type.
        Variable variable = ASEMSysMLHelper
                .getFirstCorrespondingASEMElement(this.executionState.getCorrespondenceModel(), port, Variable.class);
        variable.setType(variableType);

        // Persist variable.
        EObject variableContainer = variable.eContainer();

        if (!(variableContainer instanceof Named)) {
            throw new IllegalArgumentException(
                    "The eContainer of the variable object is not an instance of ASEM Named!");
        }

        final Named container = (Named) variableContainer;
        final String blockName = ASEMSysMLHelper.getPortsBlock(port).getBase_Class().getName();
        final String asemModelName = ASEMSysMLHelper.getASEMModelName(blockName);
        final String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName,
                AsemNamespace.FILE_EXTENSION);

        persistASEMElement(port, container, asemProjectModelPath);

    }

    @Override
    protected boolean checkPreconditions(ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (isPort(change) && isPortTypeSet(change));
    }

    private boolean isPort(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof Port);
    }

    private boolean isPortTypeSet(final ReplaceSingleValuedEReference<EObject, EObject> change) {
        return (change.getAffectedFeature().equals(UMLPackage.Literals.TYPED_ELEMENT__TYPE));
    }
}
