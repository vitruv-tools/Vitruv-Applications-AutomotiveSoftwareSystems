package tools.vitruv.applications.asemsysml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.types.TypesPackage;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import edu.kit.ipd.sdq.ASEM.primitivetypes.SignedDiscreteType;
import tools.vitruv.domains.sysml.SysMlNamspace;
import tools.vitruv.framework.correspondence.CorrespondenceModel;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * A helper class containing methods which are useful for the transformation of primitive types.
 * 
 * @author Benjamin Rupp
 *
 */
public final class ASEMSysMLPrimitiveTypeHelper {

    private static final String PRIMITIVE_TYPE_MODEL_NAME = "PrimitiveTypes";

    private static boolean repoIsInitialized = false;

    /*
     * Add constants for UML primitive types, because I found no possibility to get a UML Type of
     * the ECORE EDatatType which will be returned for example by
     * TypesPackage.eINSTANCE.getBoolean().
     * 
     * TODO [BR] Use the UML predefined primitive types if there is a way to cast them to an UML
     * Type.
     */
    public static final PrimitiveType PRIMITIVE_TYPE_BOOLEAN = UMLFactory.eINSTANCE.createPrimitiveType();
    public static final PrimitiveType PRIMITIVE_TYPE_INTEGER = UMLFactory.eINSTANCE.createPrimitiveType();
    public static final PrimitiveType PRIMITIVE_TYPE_REAL = UMLFactory.eINSTANCE.createPrimitiveType();
    public static final PrimitiveType PRIMITIVE_TYPE_UNLIMITED_NATURAL = UMLFactory.eINSTANCE.createPrimitiveType();
    public static final PrimitiveType PRIMITIVE_TYPE_STRING = UMLFactory.eINSTANCE.createPrimitiveType();
    static {
        PRIMITIVE_TYPE_BOOLEAN.setName(TypesPackage.eINSTANCE.getBoolean().getName());
        PRIMITIVE_TYPE_INTEGER.setName(TypesPackage.eINSTANCE.getInteger().getName());
        PRIMITIVE_TYPE_REAL.setName(TypesPackage.eINSTANCE.getReal().getName());
        PRIMITIVE_TYPE_UNLIMITED_NATURAL.setName(TypesPackage.eINSTANCE.getUnlimitedNatural().getName());
        PRIMITIVE_TYPE_STRING.setName(TypesPackage.eINSTANCE.getString().getName());
    }

    /**
     * Defines the mapping of a SysML primitive type to an ASEM primitive type. SysML primitive
     * types with a <code>null</code> value are ignored at the moment.
     */
    public static final Map<PrimitiveType, Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType>> PRIMITIVE_TYPE_MAP = new HashMap<PrimitiveType, Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType>>();
    static {
        // TODO [BR] Handle mapping of SysML string and unlimited natural primitive type, too.
        PRIMITIVE_TYPE_MAP.put(PRIMITIVE_TYPE_BOOLEAN, BooleanType.class);
        PRIMITIVE_TYPE_MAP.put(PRIMITIVE_TYPE_INTEGER, SignedDiscreteType.class);
        PRIMITIVE_TYPE_MAP.put(PRIMITIVE_TYPE_REAL, ContinuousType.class);
        PRIMITIVE_TYPE_MAP.put(PRIMITIVE_TYPE_STRING, null);
        PRIMITIVE_TYPE_MAP.put(PRIMITIVE_TYPE_UNLIMITED_NATURAL, null);
    }

    /**
     * Get the SysML primitive type which is mapped to the given ASEM primitive type.
     * 
     * @param asemType
     *            The ASEM primitive type class.
     * @return The SysML primitive type which is mapped to the given ASEM primitive type.
     */
    public static final PrimitiveType getSysMLTypeByASEMType(
            final Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> asemType) {
        for (Entry<PrimitiveType, Class<? extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType>> entry : PRIMITIVE_TYPE_MAP
                .entrySet()) {
            if (entry.getValue() != null && entry.getValue().isAssignableFrom(asemType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /** Utility classes should not have a public or default constructor. */
    private ASEMSysMLPrimitiveTypeHelper() {
    }

    /**
     * Get the project model path for the primitive types model.
     * 
     * @return The project model path of the ASEM primitive types model.
     */
    public static String getPrimitiveTypeProjectModelPath() {
        return ASEMSysMLHelper.getASEMProjectModelPath(PRIMITIVE_TYPE_MODEL_NAME);
    }

    /**
     * Get the primitive type instance from the ASEM primitive type repository.
     * 
     * @param <T>
     *            Type of the needed primitive type instance.
     * @param type
     *            Type of the primitive type which shall be returned.
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @return The primitive type instance or <code>null</code> if no instance of this type exists.
     */
    @SuppressWarnings("unchecked")
    public static <T extends edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType> edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType getASEMPrimitiveTypeFromRepository(
            final Class<T> type, final EObject alreadyPersistedObject) {

        edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType primitiveType = null;

        Resource resource = getPrimitiveTypesResource(alreadyPersistedObject);
        PrimitiveTypeRepository pRepo = (PrimitiveTypeRepository) resource.getContents().get(0);

        for (edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType pType : pRepo.getPrimitiveTypes()) {
            if (type.isAssignableFrom(pType.getClass())) {
                primitiveType = (T) pType;
            }
        }

        return primitiveType;

    }

    /**
     * Get the primitive type instance from the SysML model.
     * 
     * @param correspondenceModel
     *            The correspondence model.
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @param type
     *            The primitive type for which an instance shall be returned.
     * @return The instance of the primitive type or <code>null</code> if no instance was found.
     */
    public static PrimitiveType getSysMLPrimitiveTypeFromSysMLModel(final CorrespondenceModel correspondenceModel,
            final EObject alreadyPersistedObject, final PrimitiveType type) {

        String sysmlProjectModelPath = ASEMSysMLHelper.getProjectModelPath(ASEMSysMLConstants.TEST_SYSML_MODEL_NAME,
                SysMlNamspace.FILE_EXTENSION);
        Resource resource = ASEMSysMLHelper.getModelResource(correspondenceModel, alreadyPersistedObject,
                sysmlProjectModelPath);

        return getSysMLPrimitiveTypeFromSysMLModel(resource, type);

    }

    /**
     * Get the primitive type instance from the given SysML model.
     * 
     * @param sysmlResource
     *            SysML model resource.
     * @param type
     *            The primitive type for which an instance shall be returned.
     * @return The instance of the primitive type or <code>null</code> if no instance was found.
     */
    public static PrimitiveType getSysMLPrimitiveTypeFromSysMLModel(final Resource sysmlResource,
            final PrimitiveType type) {

        if (sysmlResource == null) {
            throw new IllegalArgumentException("No SysML model resource exists.");
        }

        Model sysmlModel = (Model) EcoreUtil.getObjectByType(sysmlResource.getContents(),
                UMLPackage.eINSTANCE.getModel());

        if (sysmlModel == null) {
            throw new IllegalArgumentException("SysML model does not contain a UML model element.");
        }

        for (PackageableElement modelType : sysmlModel.getPackagedElements()) {
            PrimitiveType pType = (PrimitiveType) modelType;
            if (pType.getName().equals(type.getName())) {
                return pType;
            }
        }

        return null;
    }

    /**
     * Check if the primitive types resource is already initialized.
     * 
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @return <code>True</code> if the primitive types model resource is initialized, otherwise
     *         <code>false</code>.
     */
    public static boolean isPrimitiveTypeModelInitialized(final EObject alreadyPersistedObject) {

        if (repoIsInitialized) {
            return true;
        }

        Resource resource = null;
        try {
            resource = getPrimitiveTypesResource(alreadyPersistedObject);
        } catch (Exception e) {
            return false;
        }

        if (resource != null) {
            repoIsInitialized = true;
        }

        return repoIsInitialized;
    }

    /**
     * Reset the {@link #repoIsInitialized} flag to enable a new {@link PrimitiveTypeRepository}
     * initialization. Please call this method before each test case. <br>
     * <br>
     * 
     * TODO [BR] This mechanism should be reworked. Currently, it is used to initialize the
     * {@link PrimitiveTypeRepository} only once per test case.
     * 
     * @see #isPrimitiveTypeModelInitialized(EObject, CorrespondenceModel)
     */
    public static void resetRepoInitializationFlag() {
        repoIsInitialized = false;
    }

    private static Resource getPrimitiveTypesResource(final EObject alreadyPersistedObject) {

        String existingElementURI = VURI.getInstance(alreadyPersistedObject.eResource()).getEMFUri().toFileString();
        String uriPrefix = existingElementURI.substring(0,
                existingElementURI.lastIndexOf(ASEMSysMLConstants.MODEL_DIR_NAME + "/"));
        String asemURIString = uriPrefix + getPrimitiveTypeProjectModelPath();

        ResourceSet resourceSet = alreadyPersistedObject.eResource().getResourceSet();
        URI uri = URI.createFileURI(asemURIString);
        Resource primitiveTypesResource = resourceSet.getResource(uri, true);

        return primitiveTypesResource;
    }

}
