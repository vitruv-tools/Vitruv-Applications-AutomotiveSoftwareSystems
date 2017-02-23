package tools.vitruv.applications.asemsysml.tests.asem2sysml.testcases;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.papyrus.sysml14.portsandflows.FlowProperty;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.junit.Test;

import edu.kit.ipd.sdq.ASEM.base.TypedElement;
import edu.kit.ipd.sdq.ASEM.classifiers.Class;
import edu.kit.ipd.sdq.ASEM.classifiers.Module;
import edu.kit.ipd.sdq.ASEM.dataexchange.Message;
import edu.kit.ipd.sdq.ASEM.dataexchange.Method;
import edu.kit.ipd.sdq.ASEM.dataexchange.Parameter;
import edu.kit.ipd.sdq.ASEM.dataexchange.ReturnType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.BooleanType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.ContinuousType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.PrimitiveType;
import edu.kit.ipd.sdq.ASEM.primitivetypes.SignedDiscreteType;
import tools.vitruv.applications.asemsysml.ASEMSysMLHelper;
import tools.vitruv.applications.asemsysml.ASEMSysMLPrimitiveTypeHelper;
import tools.vitruv.applications.asemsysml.tests.asem2sysml.ASEM2SysMLTest;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLAssertionHelper;
import tools.vitruv.applications.asemsysml.tests.util.ASEMSysMLTestHelper;

/**
 * Class with test cases for all ASEM elements which are mapped to an UML port (ASEM message,
 * parameter and return type).
 * 
 * @author Benjamin Rupp
 *
 */
public class ASEMElementToPortMappingTransformationTest extends ASEM2SysMLTest {

    /**
     * After adding an ASEM message to an ASEM model, an UML Port with the same name (and a SysML
     * FlowProperty and BindingConnector, too) must be added to the SysML model.
     */
    @Test
    public void testIfAMessageIsMappedToASysMLPort() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<Message> messages = this.prepareMessages(module, asemClassForMessageType);

        for (Message message : messages) {

            ASEMSysMLAssertionHelper.assertPortWasCreated(message, module, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(message, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectType(message, this.getCorrespondenceModel());
        }

    }

    /**
     * After adding an ASEM parameter, an UML port with the same name (and a SysML FlowProperty and
     * BindingConnector, too) must be added to the SysML model.
     */
    @Test
    public void testIfAParameterIsMappedToASysMLPort() {

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMethods", Class.class,
                this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Method method = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForParameters", asemClass, this);

        Collection<Parameter> parameters = this.prepareParameters(method, asemClassForMessageType);

        for (Parameter parameter : parameters) {

            ASEMSysMLAssertionHelper.assertPortWasCreated(parameter, asemClass, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(parameter, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectType(parameter, this.getCorrespondenceModel());
        }

    }

    /**
     * After adding an ASEM return type, an UML port with the same name (and a SysML FlowProperty
     * and BindingConnector, too) must be added to the SysML model.
     */
    @Test
    public void testIfAReturnTypeIsMappedToASysMLPort() {

        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMethods", Class.class,
                this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<ReturnType> returnTypes = this.prepareReturnTypes(asemClass, asemClassForMessageType);

        for (ReturnType returnType : returnTypes) {

            ASEMSysMLAssertionHelper.assertPortWasCreated(returnType, asemClass, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(returnType, this.getCorrespondenceModel());
            ASEMSysMLAssertionHelper.assertPortHasCorrectType(returnType, this.getCorrespondenceModel());
        }
    }

    /**
     * After deleting an ASEM message or ASEM parameter, the corresponding UML port and the
     * correspondence between both must be deleted, too.
     */
    @Test
    public void testIfAPortWillBeDeleted() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessageToDelete",
                Module.class, this);
        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMethods", Class.class,
                this);
        Method method = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForParameters", asemClass, this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<TypedElement> typedElements = this.prepareTypedElements(method, module, asemClass,
                asemClassForMessageType);

        for (TypedElement typedElement : typedElements) {

            final Port portBckp = ASEMSysMLHelper.getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(),
                    typedElement, Port.class);
            final org.eclipse.uml2.uml.Class portContainerBckp = (org.eclipse.uml2.uml.Class) portBckp.eContainer();
            final EObject rootElement = EcoreUtil.getRootContainer(typedElement);
            final FlowProperty flowPropertyBckp = ASEMSysMLHelper.getFlowProperty(portBckp);
            final Property propertyBckp = flowPropertyBckp.getBase_Property();

            EcoreUtil.delete(typedElement);
            this.saveAndSynchronizeChanges(rootElement);

            ASEMSysMLAssertionHelper.assertPortWasDeleted(typedElement, portBckp, portContainerBckp, propertyBckp,
                    flowPropertyBckp, this.getCorrespondenceModel(), module);
        }

    }

    /**
     * After renaming an ASEM message or ASEM parameter, the port must be renamed, too.
     */
    @Test
    public void testIfPortWillBeRenamed() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessagesToRename",
                Module.class, this);
        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMethods", Class.class,
                this);
        Method method = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForParameters", asemClass, this);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Collection<TypedElement> typedElements = this.prepareTypedElements(method, module, asemClass,
                asemClassForMessageType);

        for (TypedElement typedElement : typedElements) {
            final Port portBeforeRenaming = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), typedElement, Port.class);
            assertEquals("Port " + portBeforeRenaming.getName() + " has wrong name!", typedElement.getName(),
                    portBeforeRenaming.getName());

            final String newName = typedElement.getName() + "Renamed";
            typedElement.setName(newName);
            this.saveAndSynchronizeChanges(typedElement);

            final Port portAfterRenaming = ASEMSysMLHelper
                    .getFirstCorrespondingSysMLElement(this.getCorrespondenceModel(), typedElement, Port.class);
            assertEquals("Port was renamed successfully!", newName, portAfterRenaming.getName());
        }

    }

    /**
     * After changing the access parameters of an ASEM message (readable, writable), the port
     * direction must be adapted.
     */
    @Test
    public void testIfPortDirectionWillBeUpdated() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final Class asemClassForMessageType = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageType", Class.class, this);

        Message messagePrimitiveType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBoolean", true,
                false, pBoolean, module, this);
        Message messageComponentType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClass", true,
                false, asemClassForMessageType, module, this);

        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messagePrimitiveType, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messageComponentType, this.getCorrespondenceModel());

        // INOUT
        messagePrimitiveType.setWritable(true);
        messageComponentType.setWritable(true);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messagePrimitiveType, this.getCorrespondenceModel());
        this.saveAndSynchronizeChanges(messageComponentType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messageComponentType, this.getCorrespondenceModel());

        // OUT
        messagePrimitiveType.setReadable(false);
        messageComponentType.setReadable(false);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messagePrimitiveType, this.getCorrespondenceModel());
        this.saveAndSynchronizeChanges(messageComponentType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectDirection(messageComponentType, this.getCorrespondenceModel());

    }

    /**
     * After changing the type of an ASEM message or ASEM parameter, the port type must be adapted.
     */
    @Test
    public void testIfPortTypeWillBeUpdated() {

        Module module = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ModuleForMessages", Module.class,
                this);
        Class asemClass = ASEMSysMLTestHelper.createASEMComponentAsModelRootAndSync("ClassForMethods", Class.class,
                this);
        Method method = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForParameters", asemClass, this);

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, module);
        final Class asemClassForMessageTypeA = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageTypeA", Class.class, this);
        final Class asemClassForMessageTypeB = ASEMSysMLTestHelper
                .createASEMComponentAsModelRootAndSync("ClassForMessageTypeB", Class.class, this);

        Message messagePrimitiveType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBoolean", true,
                false, pBoolean, module, this);
        Message messageComponentType = ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClass", true,
                false, asemClassForMessageTypeA, module, this);

        Parameter parameterPrimitiveType = ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterBoolean",
                pBoolean, method, this);
        Parameter parameterComponentType = ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterClass",
                asemClassForMessageTypeA, method, this);

        ASEMSysMLAssertionHelper.assertPortHasCorrectType(messagePrimitiveType, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(messageComponentType, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(parameterPrimitiveType, this.getCorrespondenceModel());
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(parameterComponentType, this.getCorrespondenceModel());

        messagePrimitiveType.setType(pContinuous);
        this.saveAndSynchronizeChanges(messagePrimitiveType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(messagePrimitiveType, this.getCorrespondenceModel());

        messageComponentType.setType(asemClassForMessageTypeB);
        this.saveAndSynchronizeChanges(messageComponentType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(messageComponentType, this.getCorrespondenceModel());

        parameterPrimitiveType.setType(pContinuous);
        this.saveAndSynchronizeChanges(parameterPrimitiveType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(parameterPrimitiveType, this.getCorrespondenceModel());

        parameterComponentType.setType(asemClassForMessageTypeB);
        this.saveAndSynchronizeChanges(parameterComponentType);
        ASEMSysMLAssertionHelper.assertPortHasCorrectType(parameterComponentType, this.getCorrespondenceModel());

    }

    private Collection<Message> prepareMessages(final Module module, final Class classAsType) {

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, module);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, module);
        final PrimitiveType pSignedDiscreteType = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(SignedDiscreteType.class, module);

        Collection<Message> messages = new HashSet<Message>();
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanIN", true, false, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanOUT", false, true, pBoolean,
                module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageBooleanINOUT", true, true,
                pBoolean, module, this));

        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousIN", true, false,
                pContinuous, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousOUT", false, true,
                pContinuous, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageContinuousINOUT", true, true,
                pContinuous, module, this));

        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteIN", true, false,
                pSignedDiscreteType, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteOUT", false, true,
                pSignedDiscreteType, module, this));
        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageSignedDiscreteINOUT", true, true,
                pSignedDiscreteType, module, this));

        messages.add(ASEMSysMLTestHelper.createASEMMessageAddToModuleAndSync("MessageClassIN", true, false, classAsType,
                module, this));

        return messages;
    }

    private Collection<Parameter> prepareParameters(final Method method, final Class classAsType) {

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, method);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, method);
        final PrimitiveType pSignedDiscreteType = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(SignedDiscreteType.class, method);

        Collection<Parameter> parameters = new HashSet<>();

        parameters.add(
                ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterBoolean", pBoolean, method, this));
        parameters.add(ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterContinuous", pContinuous,
                method, this));
        parameters.add(ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterSignedDiscrete",
                pSignedDiscreteType, method, this));
        parameters.add(
                ASEMSysMLTestHelper.createASEMParameterAddToMethodAndSync("ParameterClass", classAsType, method, this));

        return parameters;
    }

    private Collection<ReturnType> prepareReturnTypes(final Class asemClass, final Class classAsType) {

        Collection<ReturnType> returnTypes = new HashSet<>();

        final PrimitiveType pBoolean = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(BooleanType.class, asemClass);
        final PrimitiveType pContinuous = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(ContinuousType.class, asemClass);
        final PrimitiveType pSignedDiscreteType = ASEMSysMLPrimitiveTypeHelper
                .getASEMPrimitiveTypeFromRepository(SignedDiscreteType.class, asemClass);

        Method methodA = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForReturnTypeA", asemClass, this);
        Method methodB = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForReturnTypeB", asemClass, this);
        Method methodC = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForReturnTypeC", asemClass, this);
        Method methodD = ASEMSysMLTestHelper.createASEMMethodAddToClassAndSync("MethodForReturnTypeD", asemClass, this);

        returnTypes.add(ASEMSysMLTestHelper.createASEMReturnTypeAddToMethodAndSync("ReturnTypeBoolean", pBoolean,
                methodA, this));
        returnTypes.add(ASEMSysMLTestHelper.createASEMReturnTypeAddToMethodAndSync("ReturnTypeContinuous", pContinuous,
                methodB, this));
        returnTypes.add(ASEMSysMLTestHelper.createASEMReturnTypeAddToMethodAndSync("ReturnTypeSignedDiscrete",
                pSignedDiscreteType, methodC, this));
        returnTypes.add(ASEMSysMLTestHelper.createASEMReturnTypeAddToMethodAndSync("ReturnTypeClass", classAsType,
                methodD, this));

        return returnTypes;
    }

    private Collection<TypedElement> prepareTypedElements(final Method method, final Module module,
            final Class asemClass, final Class classAsType) {

        Collection<TypedElement> typedElements = new HashSet<>();

        typedElements.addAll(this.prepareMessages(module, classAsType));
        typedElements.addAll(this.prepareParameters(method, classAsType));
        typedElements.addAll(this.prepareReturnTypes(asemClass, classAsType));

        return typedElements;
    }
}
