package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.BlockMappingTransformationTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.ModelInitializationTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.PartMappingTransformationTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.PortMappingTransformationTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.PropertyMappingTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases.RenameTransformationTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;

/**
 * Unifies all SysML2ASEM test cases. Use this test suite to run and test the <b>java
 * transformations</b>.
 * 
 * @author Benjamin Rupp
 */
@RunWith(Suite.class)
@SuiteClasses({ ModelInitializationTest.class, BlockMappingTransformationTest.class,
        PartMappingTransformationTest.class, PortMappingTransformationTest.class, RenameTransformationTest.class,
        PropertyMappingTest.class })
public final class SysML2ASEMJavaTestSuite {

    /**
     * Set up the transformation type for all test cases.
     */
    @BeforeClass
    public static void setUpTestCases() {
        ASEMSysMLTest.setTransformationType(TransformationType.JAVA);
    }
}
