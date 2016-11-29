package tools.vitruv.applications.asemsysml.tests.sysml2asem;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Unifies all SysML2ASEM test cases.
 * 
 * @author Benjamin Rupp
 */
@RunWith(Suite.class)
@SuiteClasses({ ModelInitializationTest.class, SysMLBlockMappingTransformationTest.class })
public class SysML2ASEMTestSuite {

}
