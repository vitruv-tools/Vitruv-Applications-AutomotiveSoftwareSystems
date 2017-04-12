package tools.vitruv.applications.asemsysml.java.sysml2asem.transformations;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PrimitiveType;

import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitivetypesFactory;
import edu.kit.ipd.sdq.ASEM.primitivetypes.SignedDiscreteType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.UnsignedDiscreteType;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.java.sysml2asem.AbstractTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;
import tools.vitruv.framework.change.echange.feature.reference.InsertEReference;
import tools.vitruv.framework.userinteraction.UserInteracting;

/**
 * The transformation class for transforming the SysML primitive types.<br>
 * <br>
 * 
 * Therefore the transformation reacts on a {@link InsertEReference} change which will be triggered
 * if a SysML primitive type was added to the UML model element. <br>
 * <br>
 * 
 * @author Benjamin Rupp
 *
 */
public class PrimitiveTypeTransformation extends AbstractTransformationRealization<InsertEReference<EObject, EObject>> {

    private static Logger logger = Logger.getLogger(PrimitiveTypeTransformation.class);

    public PrimitiveTypeTransformation(UserInteracting userInteracting) {
        super(userInteracting);
    }

    @Override
    public Class<? extends EChange> getExpectedChangeType() {
        return InsertEReference.class;
    }

    @Override
    protected void executeTransformation(InsertEReference<EObject, EObject> change) {

        if (!ASEMSysMLPrimitiveTypeHelper.isPrimitiveTypeModelInitialized(change.getAffectedEObject(),
                this.executionState.getCorrespondenceModel())) {
            logger.info("[ASEMSysML][Java] Initialize ASEM primitive types.");
            initializeASEMPrimitveTypeRepo(change.getAffectedEObject());
        }
    }

    @Override
    protected boolean checkPreconditions(InsertEReference<EObject, EObject> change) {
        return (isUMLModel(change) && isPrimitiveTypeAdded(change));
    }

    private boolean isUMLModel(final InsertEReference<EObject, EObject> change) {
        return (change.getAffectedEObject() instanceof Model);
    }

    private boolean isPrimitiveTypeAdded(final InsertEReference<EObject, EObject> change) {
        return (change.getNewValue() instanceof PrimitiveType);
    }

    private void initializeASEMPrimitveTypeRepo(final EObject alreadyPersistedObject) {

        PrimitiveTypeRepository primitiveTypeRepo = PrimitivetypesFactory.eINSTANCE.createPrimitiveTypeRepository();
        primitiveTypeRepo.setName("PrimitiveTypeRepo");

        BooleanType pBoolean = PrimitivetypesFactory.eINSTANCE.createBooleanType();
        pBoolean.setName("Boolean");
        primitiveTypeRepo.getPrimitiveTypes().add(pBoolean);

        ContinuousType pContinuous = PrimitivetypesFactory.eINSTANCE.createContinuousType();
        pContinuous.setName("Continuous");
        primitiveTypeRepo.getPrimitiveTypes().add(pContinuous);

        UnsignedDiscreteType pUnsignedDiscrete = PrimitivetypesFactory.eINSTANCE.createUnsignedDiscreteType();
        pUnsignedDiscrete.setName("UnsignedDiscrete");
        primitiveTypeRepo.getPrimitiveTypes().add(pUnsignedDiscrete);

        SignedDiscreteType pSignedDiscrete = PrimitivetypesFactory.eINSTANCE.createSignedDiscreteType();
        pSignedDiscrete.setName("SignedDiscrete");
        primitiveTypeRepo.getPrimitiveTypes().add(pSignedDiscrete);

        persistASEMElement(alreadyPersistedObject, primitiveTypeRepo,
                ASEMSysMLPrimitiveTypeHelper.getPrimitiveTypeProjectModelPath());

    }

}
