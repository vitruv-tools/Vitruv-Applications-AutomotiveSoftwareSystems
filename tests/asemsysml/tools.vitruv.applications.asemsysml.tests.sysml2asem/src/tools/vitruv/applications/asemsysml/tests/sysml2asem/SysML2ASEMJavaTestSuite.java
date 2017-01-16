package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTest;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.util.ASEMSysMLTestHelper.TransformationType;

/**
 * Unifies all SysML2ASEM test cases. Use this test suite to run and test the <b>java transformations</b>.
 * 
 * @author Benjamin Rupp
 */
@RunWith(Suite.class)
@SuiteClasses({ ModelInitializationTest.class, SysMLBlockMappingTransformationTest.class })
public final class SysML2ASEMJavaTestSuite {

    /**
     * Set up the transformation type for all test cases.
     */
    @BeforeClass
    public static void setUpTestCases() {
        ASEMSysMLTest.setTransformationType(TransformationType.JAVA);
    }
}
