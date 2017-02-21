package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.util.EcoreUtil;
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

    /**
     * After changing the name of an ASEM constant, the name of the corresponding part reference
     * must be adapted.
     */
    @Test
    public void testIfPartReferenceWillBeRenamed() {

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

        assertEquals("Wrong name of part reference " + correspondenceA.getName() + "!", constantInClass.getName(),
                correspondenceA.getName());
        assertEquals("Wrong name of part reference " + correspondenceB.getName() + "!", constantInModule.getName(),
                correspondenceB.getName());

        final String newNameA = constantInClass.getName() + "Renamed";
        final String newNameB = constantInModule.getName() + "Renamed";

        constantInClass.setName(newNameA);
        this.saveAndSynchronizeChanges(constantInClass);
        constantInModule.setName(newNameB);
        this.saveAndSynchronizeChanges(constantInModule);

        final Property correspondenceAAfter = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInClass, Property.class);
        final Property correspondenceBAfter = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInModule, Property.class);

        assertEquals("Renaming of part reference " + correspondenceA.getName() + " failed!", newNameA,
                correspondenceAAfter.getName());
        assertEquals("Renaming of part reference " + correspondenceB.getName() + " failed!", newNameB,
                correspondenceBAfter.getName());

    }

    /**
     * After deleting an ASEM constant, the corresponding part reference must be deleted, too.
     */
    @Test
    public void testIfPartReferenceWillBeDeleted() {

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

        EcoreUtil.delete(constantInClass);
        this.saveAndSynchronizeChanges(asemClass);

        EcoreUtil.delete(constantInModule);
        this.saveAndSynchronizeChanges(asemModule);

        final Property correspondenceA = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInClass, Property.class);
        final Property correspondenceB = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInModule, Property.class);

        assertTrue("Correspondence for " + constantInClass.getName() + " was not deleted!", correspondenceA == null);
        assertTrue("Correspondence for " + constantInModule.getName() + " was not deleted!", correspondenceB == null);

        assertTrue("Part reference was not deleted from model!",
                !asemClass.getTypedElements().contains(constantInClass));
        assertTrue("Part reference was not deleted from model!",
                !asemModule.getTypedElements().contains(constantInModule));

    }

    /**
     * After changing the type of an ASEM constant, the type of the corresponding part property must
     * be adapted.
     */
    @Test
    public void testIfPartReferenceTypeWillBeUpdated() {

        Logger.getRootLogger().setLevel(Level.INFO);
        
        final Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForConstants",
                Class.class, this);
        final Module asemModule = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForConstants",
                Module.class, this);
        final Class asemClassAsPartA = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassAsPartA",
                Class.class, this);
        final Class asemClassAsPartB = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassAsPartB",
                Class.class, this);

        Constant constantInClass = ASEMSysMLTestHelper.createASEMConstantAddToComponentAndSync("ConstantInClass",
                asemClassAsPartA, asemClass, this);
        Constant constantInModule = ASEMSysMLTestHelper.createASEMConstantAddToComponentAndSync("ConstantInModule",
                asemClassAsPartA, asemModule, this);

        constantInClass.setType(asemClassAsPartB);
        this.saveAndSynchronizeChanges(asemClass);

        constantInModule.setType(asemClassAsPartB);
        this.saveAndSynchronizeChanges(asemModule);

        final Property correspondenceA = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInClass, Property.class);
        final Property correspondenceB = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), constantInModule, Property.class);

        final org.eclipse.uml2.uml.Class expectedType = ASEMSysMLHelper
                .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), asemClassAsPartB, Block.class)
                .getBase_Class();

        assertEquals("Type change of part reference " + correspondenceA.getName() + " failed!", expectedType,
                correspondenceA.getType());
        assertEquals("Type change of part reference " + correspondenceB.getName() + " failed!", expectedType,
                correspondenceB.getType());

    }

}
