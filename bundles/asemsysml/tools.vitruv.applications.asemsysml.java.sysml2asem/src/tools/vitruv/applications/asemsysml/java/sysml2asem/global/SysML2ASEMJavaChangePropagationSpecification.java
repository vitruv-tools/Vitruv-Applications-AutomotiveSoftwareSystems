package tools.vitruv.applications.asemsysml.java.sysml2asem.global;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.uml2.uml.UMLPackage;

import edu.kit.ipd.sdq.ASEM.ASEMPackage;
import tools.vitruv.applications.asemsysml.java.sysml2asem.SysML2ASEMJavaExecutor;
import tools.vitruv.framework.change.processing.impl.CompositeChangePropagationSpecification;
import tools.vitruv.framework.userinteraction.impl.UserInteractor;
import tools.vitruv.framework.util.datatypes.MetamodelPair;

/**
 * Change propagation specification for the SysML2ASEM transformation using java transformations.
 * This class is responsible for the change handling and manages the main and preprocessors.
 * 
 * @author Benjamin Rupp
 *
 */
public class SysML2ASEMJavaChangePropagationSpecification extends CompositeChangePropagationSpecification {

    private static Logger logger = Logger.getLogger(SysML2ASEMJavaChangePropagationSpecification.class);

    private final MetamodelPair metamodelPair;

    /**
     * Create new SysML2ASEM change propagation specification for the java transformations.
     */
    public SysML2ASEMJavaChangePropagationSpecification() {

        super(new UserInteractor());

        logger.setLevel(Level.INFO);

        this.metamodelPair = new MetamodelPair(UMLPackage.eNS_URI, ASEMPackage.eNS_URI);
        setup();

    }

    private void setup() {
        logger.info("[ASEMSysML][Java] Set up transformation environment using java transformations.");
        this.addChangeMainprocessor(new SysML2ASEMJavaExecutor(this.getUserInteracting()));
    }

    @Override
    public MetamodelPair getMetamodelPair() {
        return this.metamodelPair;
    }

}
