package tools.vitruv.applications.asemsysml.tests.sysml2asem.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.blocks.Block;
import org.eclipse.uml2.uml.Property;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Component;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Constant;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.tests.sysml2asem.SysML2ASEMTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class for all test cases checking the part mapping of a SysML block to an ASEM component.
 * 
 * @author Benjamin Rupp
 *
 */
public class PartMappingTransformationTest extends SysML2ASEMTest {

    /**
     * Part references in SysML are mapped to an ASEM constant which references the element which
     * corresponds with the part block. Therefore a ASEM constant element has to exist in the parent
     * ASEM component which has a reference to the child component. <br>
     * <br>
     * 
     * ASEM modules can not be used as subcomponents, see
     * {@link #testIfPartMappingToModuleIsIgnored()}. <br>
     * <br>
     * 
     * [Requirement 1.e)] [Requirement 2.f)]
     */
    @Test
    public void testIfPartMappingExists() {
        /*
         * Add a block BlockA which has a part reference to a block BlockB. In ASEM a module cannot
         * be used as a subcomponent of another component. Therefore the BlockB must correspond with
         * a ASEM class, and NOT with a ASEM module.
         */

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;

        Block blockA = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockA", true,
                asemComponentType, this);
        Block blockB1 = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockB1", true,
                asemComponentType, this);

        ASEMSysMLTestHelper.createPartReferenceForBlockAndSync("partReferenceB1", blockA, blockB1, this);

        assertTrue("Block A doesn't contain a part!", !blockA.getParts().isEmpty());

        ASEMSysMLAssertionHelper.assertPartReferenceBetweenBlocksExists(blockA, asemComponentType, blockB1,
                asemComponentType, this.getCorrespondenceModel());

    }

    /**
     * Check if nested part references are mapped, too.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfNestedPartMappingExists() throws IOException {
        /*
         * Add a block BlockN1 which has a part reference to a block BlockN2 which itself has a part
         * reference to a third block BlockN3. In ASEM a module cannot be used as a subcomponent of
         * another component. Therefore the BlockN2 and BlockN3 must correspond with a ASEM class,
         * and NOT with a ASEM module.
         */

        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;

        Block blockN1 = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockN1", true,
                asemComponentType, this);
        Block blockN2 = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockN2", true,
                asemComponentType, this);
        Block blockN3 = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockN3", true,
                asemComponentType, this);

        ASEMSysMLTestHelper.createPartReferenceForBlockAndSync("partReferenceN2", blockN1, blockN2, this);
        ASEMSysMLTestHelper.createPartReferenceForBlockAndSync("partReferenceN3", blockN2, blockN3, this);

        saveAndSynchronizeChanges(blockN1);
        saveAndSynchronizeChanges(blockN2);
        saveAndSynchronizeChanges(blockN3);

        assertTrue("BlockN1 doesn't contain a part!", !blockN1.getParts().isEmpty());
        assertTrue("BlockN2 doesn't contain a part!", !blockN2.getParts().isEmpty());

        ASEMSysMLAssertionHelper.assertPartReferenceBetweenBlocksExists(blockN1, asemComponentType, blockN2,
                asemComponentType, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPartReferenceBetweenBlocksExists(blockN2, asemComponentType, blockN3,
                asemComponentType, this.getCorrespondenceModel());
    }

    /**
     * An ASEM module can not be used as a subcomponent of another ASEM component. Therefore part
     * references to a SysML block which corresponds with an ASEM module are ignored.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfPartMappingToModuleIsIgnored() throws IOException {
        /*
         * An ASEM module can not be used as a subcomponent in other ASEM components. Therefore
         * SysML part references to a SysML block which corresponds to an ASEM module must be
         * ignored.
         */
        Resource sysmlModelResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = Module.class;

        Block blockA = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource,
                "BlockWithModuleAsPart", true, asemComponentType, this);
        Block blockB = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlModelResource, "BlockAsModule", true,
                asemComponentType, this);

        ASEMSysMLTestHelper.createPartReferenceForBlockAndSync("partReferenceB", blockA, blockB, this);

        saveAndSynchronizeChanges(blockA);
        saveAndSynchronizeChanges(blockB);

        assertTrue("BlockA doesn't contain a part!", !blockA.getParts().isEmpty());

        Component componentA = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockA,
                asemComponentType);
        Component componentB = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockB,
                asemComponentType);

        boolean partReferenceMappingExists = false;

        for (TypedElement typedElement : componentA.getTypedElements()) {
            if (typedElement.getType().equals(componentB)) {
                partReferenceMappingExists = true;
            }
        }

        assertTrue("A part reference to an ASEM module exists! ", !partReferenceMappingExists);

    }

    /**
     * If a part reference of a SysML block is deleted the reference mapping on the ASEM model must
     * be deleted, too. If both SysML blocks are not deleted, the corresponding ASEM components
     * shall not be deleted, too.
     * 
     * @throws IOException
     *             If saving and synchronizing the changed object failed.
     */
    @Test
    public void testIfPartMappingIsRemovedAfterPartDeletion() throws IOException {

        Resource sysmlResource = this.getModelResource(sysmlProjectModelPath);
        Class<? extends Component> asemComponentType = edu.kit.ipd.sdq.ASEM.classifiers.Class.class;

        Block blockA = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlResource, "BlockADeletionTest", true,
                asemComponentType, this);
        Block blockB = ASEMSysMLTestHelper.createSysMLBlockAddToModelAndSync(sysmlResource, "BlockBDeletionTest", true,
                asemComponentType, this);

        Property partProperty = ASEMSysMLTestHelper.createPartReferenceForBlockAndSync("partReference", blockA, blockB,
                this);

        saveAndSynchronizeChanges(blockA);
        saveAndSynchronizeChanges(blockB);

        ASEMSysMLAssertionHelper.assertPartReferenceBetweenBlocksExists(blockA, asemComponentType, blockB,
                asemComponentType, this.getCorrespondenceModel());

        // Remove part reference in blockA.
        EcoreUtil.delete(partProperty);
        saveAndSynchronizeChanges(blockA);

        assertTrue("Part property of block " + blockA.getBase_Class().getName() + " was not deleted succesfully!",
                !blockA.getBase_Class().getAllAttributes().contains(partProperty));

        // Check if part reference was deleted in ASEM model.
        assertPartReferenceBetweenBlocksDoesNotExist(blockA, asemComponentType, blockB, asemComponentType);

        // Check if correspondence was deleted, too.
        assertPartCorrespondenceDoesNotExist(partProperty);

    }

    private void assertPartReferenceBetweenBlocksDoesNotExist(final Block blockA,
            final Class<? extends Component> componentTypeA, final Block blockB,
            final Class<? extends Component> componentTypeB) {

        Component componentA = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockA,
                componentTypeA);
        Component componentB = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(), blockB,
                componentTypeB);

        assertTrue("No corresponding element found for " + blockA.getBase_Class().getName(), componentA != null);
        assertTrue("No corresponding element found for " + blockB.getBase_Class().getName(), componentB != null);

        final boolean referenceExists = ASEMSysMLTestHelper.doesPartReferenceExists(componentA, componentB);

        assertTrue("Part reference mapping exists in ASEM component " + componentA.getName(), !referenceExists);
    }

    private void assertPartCorrespondenceDoesNotExist(final Property partProperty) {

        final String msg = "Part correspondence for part property " + partProperty.getName() + " was not deleted!";

        try {

            Constant constant = ASEMSysMLHelper.getFirstCorrespondingASEMElement(this.getCorrespondenceModel(),
                    partProperty, Constant.class);
            assertEquals(msg, constant, null);

        } catch (Exception e) {
            fail(msg);
        }
    }

}
