package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Property;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all ASEM constant mapping tests. An ASEM constant must be transformed to a SysML part
 * reference.
 * 
 * @author Benjamin Rupp
 *
 */
public class ConstantMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * After adding an ASEM constant, a SysML part reference must be added to the SysML model. A
     * part reference is a property of a block which is typed by the referenced block and its
     * aggregation kind is set to COMPOSITE. The name of this property is equal to the name of the
     * constant.
     */
    @Test
    public void testIfAConstantIsMappedToASysMLPartReference() {

        Logger.getRootLogger().setLevel(Level.INFO);

        final Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForConstants",
                Class.class, this);
        final Module asemModule = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForConstants",
                Module.class, this);
        final Class asemClassAsPart = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassAsPart",
                Class.class, this);

        Constant constantInClass = ASEMSysMLTestHelper.createASEMConstantAddToComponentAndSync("ConstantInClass",
                asemClassAsPart, asemClass, this);
        Constant constantInModule = ASEMSysMLTestHelper.createASEMConstantAddToComponentAndSync("ConstantInModule",
                asemClassAsPart, asemModule, this);

        final Property correspondenceA = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInClass, Property.class);
        final Property correspondenceB = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInModule, Property.class);

        assertTrue("No corresponding part property for constant " + constantInClass.getName() + " found!",
                correspondenceA != null);
        assertTrue("No corresponding part property for constant " + constantInModule.getName() + " found!",
                correspondenceB != null);

        assertEquals("Wrong name of part reference " + correspondenceA.getName() + "!", constantInClass.getName(),
                correspondenceA.getName());
        assertEquals("Wrong name of part reference " + correspondenceB.getName() + "!", constantInModule.getName(),
                correspondenceB.getName());

        assertEquals("Wrong aggregation kind of part reference " + correspondenceA.getName() + "!",
                AggregationKind.COMPOSITE_LITERAL, correspondenceA.getAggregation());
        assertEquals("Wrong aggregation kind of part reference " + correspondenceB.getName() + "!",
                AggregationKind.COMPOSITE_LITERAL, correspondenceB.getAggregation());

        final org.eclipse.uml2.uml.Class expectedType = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), asemClassAsPart, Block.class)
                .getBase_Class();

        assertEquals("Wrong type of part reference " + correspondenceA.getName() + "!", expectedType,
                correspondenceA.getType());
        assertEquals("Wrong type of part reference " + correspondenceB.getName() + "!", expectedType,
                correspondenceB.getType());

    }

}
