package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.papyrus.sysml14.portsandflows.FlowDirection;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Port;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.Named;
import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import tools.vitruv.applications.asemsysml.ASEMSysMLConstants;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all test cases checking the renaming of a Named element.
 * 
 * @author Benjamin Rupp
 *
 */
public class RenameTransformationTest extends SysML2ASEMTest {

    private static Logger logger = Logger.getLogger(RenameTransformationTest.class);

    /**
     * If a SysML Named element is renamed, the corresponding ASEM element must be renamed, too.
     */
    @Test
    public void testRenamingOfNamedElements() {

        final String renameSuffix = "-Renamed";
        String oldName = "";
        String newName = "";
        List<NamedElement> namedElements = this.prepareNamedElements();

        for (NamedElement namedElement : namedElements) {

            oldName = namedElement.getName();
            newName = oldName + renameSuffix;
            namedElement.setName(newName);

            this.saveAndSynchronizeChanges(namedElement);
            this.assertRenamedElement(namedElement, oldName, newName);
        }

    }

    private void assertRenamedElement(final NamedElement namedElement, final String oldName, final String newName) {

        final Named asemNamedElement = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                namedElement, Named.class);

        if (asemNamedElement == null) {
            logger.info("[ASEMSysML] No corresponding ASEM Named element for SysML NamedElement "
                    + namedElement.getName() + " found.");
            return;
        }

        this.assertOldResourceFileWasDeleted(namedElement, oldName, newName);

        assertEquals("ASEM Named element was not renamed successfully!", newName, asemNamedElement.getName());

    }

    private void assertOldResourceFileWasDeleted(final NamedElement namedElement, final String oldName,
            final String newName) {

        if (namedElement.getAppliedStereotype(ASEMSysMLConstants.QUALIFIED_BLOCK_NAME) != null) {

            final String asemProjectModelPath = ASEMSysMLHelper.getASEMProjectModelPath(oldName);
            final String platformModelPath = this.getPlatformModelPath(asemProjectModelPath);
            String workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();

            File workspace = new File(workspacePath);
            File file = new File(workspace, platformModelPath);

            assertTrue("The old resource file of the renamed ASEM component " + newName + " was not deleted!",
                    !file.exists());
        }
    }

    private List<NamedElement> prepareNamedElements() {

        List<NamedElement> elements = new ArrayList<NamedElement>();

        // Add a block and port.
        Resource sysmlResource = this.getModelResource(sysmlProjectModelPath);
        Block blockToModule = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlResource,
                "BlockToRename-Module", true, Module.class, this);
        Block blockToClass = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlResource, "BlockToRename-Class",
                true, Class.class, this);
        Block blockPortType = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlResource, "BlockPortType",
                true, Class.class, this);

        Port portOfModule = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(blockToModule, "PortToRename-Module",
                FlowDirection.IN, blockPortType.getBase_Class(), this);
        Port portOfClass = ASEMSysMLTestHelper.createUMLPortAddToBlockAndSync(blockToClass, "PortToRename-Class",
                FlowDirection.IN, blockPortType.getBase_Class(), this);

        elements.add(blockToModule.getBase_Class());
        elements.add(blockToClass.getBase_Class());
        elements.add(portOfModule);
        elements.add(portOfClass);

        return elements;
    }

}
