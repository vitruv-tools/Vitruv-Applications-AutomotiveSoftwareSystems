package tools.vitruv.applications.asemsysml.tests.asem2sysml;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tools.vitruv.applications.asemsysml.tests.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases.ComponentMappingTransformationTest;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases.InitializationTest;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases.MessageMappingTransformationTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper.TransformationType;

/**
 * Unifies all ASEM2SysML test cases. Use this test suite to run and test the <b>reactions
 * transformations</b>.
 */
@RunWith(Suite.class)
@SuiteClasses({ InitializationTest.class, ComponentMappingTransformationTest.class,
        MessageMappingTransformationTest.class })
public class ASEM2SysMLReactionsTestSuite {

    /**
     * Set up the transformation type for all test cases.
     */
    @BeforeClass
    public static void setUpTestCases() {
        ASEMSysMLTest.setTransformationType(TransformationType.REACTIONS);
    }

}
