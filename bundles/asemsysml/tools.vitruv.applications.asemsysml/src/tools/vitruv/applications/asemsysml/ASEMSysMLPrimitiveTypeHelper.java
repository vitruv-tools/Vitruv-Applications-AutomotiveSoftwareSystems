package tools.vitruv.applications.asemsysml;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.types.TypesPackage;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.UMLFactory;

import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveTypeRepository;
import edu.kit.ipd.sdq.ASEM.primitivetypes.SignedDiscreteType;
import tools.vitruv.framework.util.datatypes.VURI;

/**
 * A helper class containing methods which are useful for the transformation of primitive types.
 * 
 * @author Benjamin Rupp
 *
 */
public final class ASEMSysMLPrimitiveTypeHelper {

    private static final String PRIMITIVE_TYPE_MODEL_NAME = "PrimitiveTypes";

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
     * Check if the primitive types resource is already initialized.
     * 
     * @param alreadyPersistedObject
     *            An object that already exists. This is needed to get the correct URI (test project
     *            name, etc.).
     * @return <code>True</code> if the primitive types model resource contains at least one
     *         element, otherwise <code>false</code>.
     */
    public static boolean isPrimitiveTypeModelInitialized(final EObject alreadyPersistedObject) {

        boolean isInitialized = false;

        Resource resource = null;
        try {
            resource = getPrimitiveTypesResource(alreadyPersistedObject);
        } catch (Exception e) {
            isInitialized = false;
        }

        if (resource != null && resource.getContents().size() > 0) {
            isInitialized = true;
        }

        return isInitialized;
    }

    private static Resource getPrimitiveTypesResource(final EObject alreadyPersistedObject) {

        String existingElementURI = VURI.getInstance(alreadyPersistedObject.eResource()).getEMFUri()
                .toPlatformString(false);
        String uriPrefix = existingElementURI.substring(0,
                existingElementURI.lastIndexOf(ASEMSysMLConstants.MODEL_DIR_NAME + "/"));
        String asemURIString = uriPrefix + getPrimitiveTypeProjectModelPath();

        ResourceSet resourceSet = new ResourceSetImpl();
        URI uri = URI.createURI(asemURIString);
        Resource primitiveTypesResource = resourceSet.getResource(uri, true);

        return primitiveTypesResource;
    }

}
