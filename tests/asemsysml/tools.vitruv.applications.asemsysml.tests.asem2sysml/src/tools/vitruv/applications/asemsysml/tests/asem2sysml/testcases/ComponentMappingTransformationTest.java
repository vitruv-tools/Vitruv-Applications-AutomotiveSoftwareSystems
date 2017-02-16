package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.ClassifiersFactory;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;

/**
 * Class for all ASEM component mapping tests. An ASEM component must be transformed to a SysML
 * block.
 * 
 * @author Benjamin Rupp
 *
 */
public class ComponentMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * After adding a ASEM Component to an ASEM model, a SysML block must be added to the SysML
     * model.
     */
    @Test
    public void testIfAComponentIsMappedToASysMLBlock() {

        Logger.getRootLogger().setLevel(Level.INFO);

        // Create ASEM class.
        // TODO Move to helper method.
        final String asemClassName = "SampleClass";
        Class asemClass = ClassifiersFactory.eINSTANCE.createClass();
        asemClass.setName(asemClassName);

        // Create new ASEM model and add component as root element.
        final String asemProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(asemClassName);
        this.createAndSynchronizeModel(asemProjectModelPath, asemClass);

        // Check correspondence.
        Block block = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), asemClass,
                Block.class);

        assertTrue("No corresponding element for ASEM class " + asemClass.getName() + " exists.", block != null);
        assertEquals("The names of the ASEM component and the SysML block must be equal!", asemClass.getName(),
                block.getBase_Class().getName());

    }
}
