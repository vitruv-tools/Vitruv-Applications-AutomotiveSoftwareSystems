package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.blocks.BlocksPackage;

import edu.kit.ipd.sdq.ASEM.classifiers.ClassifiersFactory;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.framework.tuid.TuidManager;
import tools.vitruv.framework.userinteraction.UserInteracting;
import tools.vitruv.framework.util.datatypes.VURI;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.domains.asem.AsemNamespace;

/**
 * The transformation class for transforming a added SysML block.<br>
 * <br>
 * 
 * The transformation reacts on a {@link ReplaceSingleValuedEAttribute} change which will be
 * triggered if the <code>isEncapsulated</code> flag of a SysML block was set. This is necessary
 * because the corresponding ASEM component will only be generated if this flag is set to
 * <code>true</code>. <br>
 * <br>
 * 
 * [Requirement 1.b)][Requirement 2.b)]
 * 
 * @see Block
 * 
 * @author Benjamin Rupp
 *
 */
public class CreatedSysMLBlockTransformation extends AbstractTransformationRealization {

    public CreatedSysMLBlockTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return ReplaceSingleValuedEAttribute.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkPreconditions(EChange untypedChange) {

        boolean preconditionsFulfilled = false;

        preconditionsFulfilled = isEncapsulatedFlagSet((ReplaceSingleValuedEAttribute<EObject, Object>) untypedChange);

        return preconditionsFulfilled;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeTransformation(EChange change) {

        System.out.println("[ASEMSysML][Java] Transforming a SysML Block ...");

        createASEMModule((ReplaceSingleValuedEAttribute<EObject, Object>) change);

    }

    private boolean isEncapsulatedFlagSet(ReplaceSingleValuedEAttribute<EObject, Object> change) {
        return (change.getAffectedEObject() instanceof Block
                && change.getAffectedFeature() == BlocksPackage.Literals.BLOCK__IS_ENCAPSULATED
                && (Boolean) change.getNewValue());
    }

    private void createASEMModule(ReplaceSingleValuedEAttribute<EObject, Object> change) {

        Block block = (Block) change.getAffectedEObject();

        // (A) Create ASEM module.
        Module module = ClassifiersFactory.eINSTANCE.createModule();
        module.setName(block.getBase_Class().getName());

        // (B) Persist element.
        // TODO [BR] Add a helper method to wrap these commands.
        VURI oldVURI = null;
        if (module.eResource() != null) {
            oldVURI = VURI.getInstance(module.eResource());
        }

        String asemModelName = ASEMSysMLHelper.getASEMModelName(block.getBase_Class().getName());
        String asemProjectModelPath = ASEMSysMLHelper.getProjectModelPath(asemModelName, AsemNamespace.FILE_EXTENSION);

        String sysmlBlockURI = VURI.getInstance(block.eResource()).getEMFUri().toPlatformString(false);
        String uriPrefix = sysmlBlockURI.substring(0,
                sysmlBlockURI.lastIndexOf(ASEMSysMLConstants.MODEL_DIR_NAME + "/"));
        String asemURIString = uriPrefix + asemProjectModelPath;
        VURI asemModuleVURI = VURI.getInstance(URI.createPlatformResourceURI(asemURIString, true));

        this.executionState.getTransformationResult().addRootEObjectToSave(module, asemModuleVURI);
        this.executionState.getTransformationResult().addVuriToDeleteIfNotNull(oldVURI);

        // (C) Add correspondence.
        // TODO [BR] Add a helper method to wrap both commands.
        TuidManager.getInstance().updateTuidsOfRegisteredObjects();
        this.executionState.getCorrespondenceModel().createAndAddCorrespondence(Collections.singletonList(block),
                Collections.singletonList(module));
    }
}
