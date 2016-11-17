package tools.vitruv.applications.asemsysml.java.sysml2asem.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.AbstractTransformationRealization;
import tools.vitruv.applications.asemsysml.java.sysml2asem.transformations.JavaTransformationRealization;
import tools.vitruv.framework.change.echange.EChange;

/**
 * This class provides a map which stores the available java transformations the change types they
 * react to.
 * 
 * @author Benjamin Rupp
 *
 */
public class Change2TransformationMap {

    private Map<Class<? extends EChange>, List<JavaTransformationRealization>> change2transformationMap;

    public Change2TransformationMap() {
        this.change2transformationMap = new HashMap<Class<? extends EChange>, List<JavaTransformationRealization>>();
    }

    /**
     * Add a java transformation and the change type to which the transformation reacts to.
     * 
     * @param changeType
     *            The change type the transformation reacts to.
     * @param transformation
     *            The java transformation.
     */
    public void addJavaTransformation(final Class<? extends EChange> changeType,
            final JavaTransformationRealization transformation) {

        if (!this.change2transformationMap.containsKey(changeType)) {
            this.change2transformationMap.put(changeType, new ArrayList<JavaTransformationRealization>());
        }

        this.change2transformationMap.get(changeType).add(transformation);

    }

    /**
     * Get the available java transformations for the given change.
     * 
     * @param change
     *            The change for which the available transformations are searched.
     * 
     * @return A set of {@link AbstractTransformationRealization java transformations} or
     *         <code>null</code> if no transformation is available.
     */
    public Set<JavaTransformationRealization> getJavaTransformations(final EChange change) {

        Set<JavaTransformationRealization> availableTransformations = new HashSet<JavaTransformationRealization>();

        Class<?> clazz = (Class<?>) change.getClass();
        LinkedList<Class<?>> dependantInterfaces = new LinkedList<Class<?>>();
        dependantInterfaces.addAll(Arrays.asList(change.getClass().getInterfaces()));

        // Get all transformations for the change class and its super classes.
        while (clazz != null) {

            List<JavaTransformationRealization> currentTransformations = this.change2transformationMap.get(clazz);

            if (currentTransformations != null) {
                availableTransformations.addAll(currentTransformations);
            }

            clazz = clazz.getSuperclass();

            if (clazz != null) {
                // Collect the interfaces of the current class to check them later for available
                // transformations.
                dependantInterfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            }
        }

        // Get all transformations for the interfaces of the change class and its super classes.
        while (!dependantInterfaces.isEmpty()) {

            final Class<?> currentInterface = dependantInterfaces.remove(0);
            List<JavaTransformationRealization> currentTransformations = this.change2transformationMap
                    .get(currentInterface);

            if (currentTransformations != null) {
                availableTransformations.addAll(currentTransformations);
            }

            dependantInterfaces.addAll(Arrays.asList(currentInterface.getInterfaces()));
        }

        return availableTransformations;

    }
}
