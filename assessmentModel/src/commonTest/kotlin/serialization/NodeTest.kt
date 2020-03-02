package org.sagebionetworks.assessmentmodel.serialization

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.survey.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

open class NodeTest : NodeSerializationTestHelper() {

    private val jsonCoder = Serialization.JsonCoder.default

    /**
     * AssessmentObject
     */

    @Test
    fun testAssessment_Serialization() {
        val inputString = """
            {
                "type": "assessment",
                "identifier": "foo",
                "versionString": "1.2.3",
                "resultIdentifier":"bar",
                "title": "Hello World!",
                "subtitle": "Subtitle",
                "detail": "Some text. This is a test.",
                "estimatedMinutes": 4,
                "icon": "fooIcon",
                "footnote": "This is a footnote.",
                "actions": { "goForward": { "type": "default", "buttonTitle" : "Go, Dogs! Go!" },
                            "cancel": { "type": "default", "iconName" : "closeX" }
                           },
                "shouldHideActions": ["goBackward"],
                "progressMarkers": ["step1","step2"],
                "steps": [
                    {
                        "identifier": "step1",
                        "type": "instruction",
                        "title": "Step 1"
                    },
                    {
                        "identifier": "step2",
                        "type": "instruction",
                        "title": "Step 2"
                    }
                ]
            }
            """

        val original = AssessmentObject(
                identifier = "foo",
                resultIdentifier = "bar",
                versionString = "1.2.3",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        original.title = "Hello World!"
        original.subtitle = "Subtitle"
        original.detail = "Some text. This is a test."
        original.estimatedMinutes = 4
        original.footnote = "This is a footnote."
        original.hideButtons = listOf(ButtonAction.Navigation.GoBackward)
        original.buttonMap = mapOf(
                ButtonAction.Navigation.GoForward to ButtonObject(buttonTitle = "Go, Dogs! Go!"),
                ButtonAction.Navigation.Cancel to ButtonObject(imageInfo = FetchableImage("closeX")))
        original.imageInfo = FetchableImage("fooIcon")
        original.progressMarkers = listOf("step1", "step2")

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is AssessmentObject)
        assertContainerNode(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertEquals(original.estimatedMinutes, decoded.estimatedMinutes)
        assertEquals(original.versionString, decoded.versionString)

        assertTrue(restored is AssessmentObject)
        assertContainerNode(original, restored)
        assertEqualContentNodes(original, restored)
        assertEquals(original.estimatedMinutes, restored.estimatedMinutes)
        assertEquals(original.versionString, restored.versionString)

    }

    @Test
    fun testAssessment_Result_NullResultId() {
        val original = AssessmentObject(
                identifier = "foo",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        val result = original.createResult()
        val expected = AssessmentResultObject("foo", runUUIDString = result.runUUIDString)
        assertEquals(expected, result)
    }

    @Test
    fun testAssessment_Result_WithResultId() {
        val original = AssessmentObject(
                identifier = "foo",
                resultIdentifier = "bar",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        val result = original.createResult()
        val expected = AssessmentResultObject("bar", runUUIDString = result.runUUIDString)
        assertEquals(expected, result)
    }

    /**
     * ChoiceQuestionObject
     */

    @Test
    fun testChoiceQuestion_Serialization() {
        val inputString = """
           {
               "identifier": "foo",
               "type": "choiceQuestion",
               "title": "Hello World!",
               "subtitle": "Question subtitle",
               "detail": "Some text. This is a test.",
               "footnote": "This is a footnote.",
               "image"  : {    "type" : "animated",
                               "imageNames" : ["foo1", "foo2", "foo3", "foo4"],
                               "placementType" : "topBackground",
                               "animationDuration" : 2
                                  },
                "optional": false,
                "singleChoice": false,
                "baseType": "integer",
                "uiHint": "checkmark",
                "choices":[
                {"text":"choice 1","icon":"choice1","value":1},
                {"text":"choice 2","value":2},
                {"text":"choice 3","value":3},
                {"text":"none of the above","exclusive":true}
                ]
           }
           """
        val original = ChoiceQuestionObject(
                identifier = "foo",
                choices = listOf(
                        ChoiceOptionObject(JsonPrimitive(1), "choice 1", FetchableImage("choice1")),
                        ChoiceOptionObject(JsonPrimitive(2), "choice 2"),
                        ChoiceOptionObject(JsonPrimitive(3), "choice 3"),
                        ChoiceOptionObject(JsonNull, "none of the above", null, true)
                ),
                baseType = BaseType.INTEGER)
        original.uiHint = UIHint.Choice.Checkmark
        original.title = "Hello World!"
        original.subtitle = "Question subtitle"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.imageInfo = AnimatedImage(
                imageNames = listOf("foo1", "foo2", "foo3", "foo4"),
                imagePlacement = ImagePlacement.Standard.TopBackground,
                animationDuration = 2.0)

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is ChoiceQuestionObject)
        assertEqualStep(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertTrue(restored is ChoiceQuestionObject)
        assertEqualStep(original, restored)
        assertEqualContentNodes(original, restored)
    }

    /**
     * ComboBoxQuestion
     */

    @Test
    fun testComboBoxQuestion_Serialization() {
        val inputString = """
           {
               "identifier": "foo",
               "type": "comboBoxQuestion",
               "title": "Hello World!",
               "subtitle": "Question subtitle",
               "detail": "Some text. This is a test.",
               "footnote": "This is a footnote.",
               "image"  : {    "type" : "animated",
                               "imageNames" : ["foo1", "foo2", "foo3", "foo4"],
                               "placementType" : "topBackground",
                               "animationDuration" : 2
                                  },
                "optional": false,
                "singleChoice": false,
                "uiHint": "checkmark",
                "choices":[
                {"text":"choice 1","value":"one"},
                {"text":"choice 2","value":"two"},
                {"text":"choice 3","value":"three"}
                ],
                "otherInputItem":{
                    "type": "string",
                    "fieldLabel": "Something else"
                   }
           }
           """
        val otherInputItem = StringTextInputItemObject()
        otherInputItem.fieldLabel = "Something else"

        val original = ComboBoxQuestionObject(
                identifier = "foo",
                choices = listOf(
                        ChoiceOptionObject(JsonPrimitive("one"), "choice 1"),
                        ChoiceOptionObject(JsonPrimitive("two"), "choice 2"),
                        ChoiceOptionObject(JsonPrimitive("three"), "choice 3"),
                        ChoiceOptionObject(JsonNull, "none of the above", null, true)
                ),
                otherInputItem = otherInputItem)
        original.uiHint = UIHint.Choice.Checkmark
        original.title = "Hello World!"
        original.subtitle = "Question subtitle"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.imageInfo = AnimatedImage(
                imageNames = listOf("foo1", "foo2", "foo3", "foo4"),
                imagePlacement = ImagePlacement.Standard.TopBackground,
                animationDuration = 2.0)

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is ComboBoxQuestionObject)
        assertEqualStep(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertTrue(restored is ComboBoxQuestionObject)
        assertEqualStep(original, restored)
        assertEqualContentNodes(original, restored)
    }

    /**
     * InstructionStepObject
     */

    @Test
    fun testInstructionStep_Serialization() {
        val inputString = """
           {
               "identifier": "foo",
               "type": "instruction",
               "title": "Hello World!",
               "detail": "Some text. This is a test.",
               "footnote": "This is a footnote.",
               "fullInstructionsOnly": true,
               "spokenInstructions": {"start": "Start now"},
               "actions": { "goForward": { "type": "default", "buttonTitle" : "Go, Dogs! Go!" },
                            "cancel": { "type": "default", "iconName" : "closeX" }
                           },
               "shouldHideActions": ["goBackward"],
               "image"  : {    "type" : "animated",
                               "imageNames" : ["foo1", "foo2", "foo3", "foo4"],
                               "placementType" : "topBackground",
                               "animationDuration" : 2
                                  }
           }
           """
        val original = InstructionStepObject("foo")
        original.title = "Hello World!"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.fullInstructionsOnly = true
        original.hideButtons = listOf(ButtonAction.Navigation.GoBackward)
        original.buttonMap = mapOf(
                ButtonAction.Navigation.GoForward to ButtonObject(buttonTitle = "Go, Dogs! Go!"),
                ButtonAction.Navigation.Cancel to ButtonObject(imageInfo = FetchableImage("closeX")))
        original.imageInfo = AnimatedImage(
                imageNames = listOf("foo1", "foo2", "foo3", "foo4"),
                imagePlacement = ImagePlacement.Standard.TopBackground,
                animationDuration = 2.0)
        original.spokenInstructions = mapOf("start" to "Start now")

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is InstructionStepObject)
        assertEqualOptionalStep(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertTrue(restored is InstructionStepObject)
        assertEqualOptionalStep(original, restored)
        assertEqualContentNodes(original, restored)
    }

    @Test
    fun testInstructionStep_Result_NullResultId() {
        val original = InstructionStepObject("foo")
        val result = original.createResult()
        val expected = ResultObject("foo")
        assertEquals(expected, result)
    }

    @Test
    fun testInstructionStep_Result_WithResultId() {
        val original = InstructionStepObject("foo", resultIdentifier = "bar")
        val result = original.createResult()
        val expected = ResultObject("bar")
        assertEquals(expected, result)
    }

    /**
     * SimpleQuestion
     */

    @Test
    fun testSimpleQuestion_Serialization() {
        val inputString = """
           {
               "identifier": "foo",
               "type": "simpleQuestion",
               "title": "Hello World!",
               "subtitle": "Question subtitle",
               "detail": "Some text. This is a test.",
               "footnote": "This is a footnote.",
               "image"  : {    "type" : "animated",
                               "imageNames" : ["foo1", "foo2", "foo3", "foo4"],
                               "placementType" : "topBackground",
                               "animationDuration" : 2
                                  },
                "optional": false,
                "inputItem":{"type" : "year"},
                "skipCheckbox":{"type":"skipCheckbox","fieldLabel":"No answer"}
           }
           """
        val original = SimpleQuestionObject(
                identifier = "foo",
                inputItem = YearTextInputItemObject())
        original.optional = false
        original.title = "Hello World!"
        original.subtitle = "Question subtitle"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.imageInfo = AnimatedImage(
                imageNames = listOf("foo1", "foo2", "foo3", "foo4"),
                imagePlacement = ImagePlacement.Standard.TopBackground,
                animationDuration = 2.0)
        original.skipCheckbox = SkipCheckboxInputItemObject("No answer")

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is SimpleQuestionObject)
        assertEqualStep(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertTrue(restored is SimpleQuestionObject)
        assertEqualStep(original, restored)
        assertEqualContentNodes(original, restored)
    }

    /**
     * MultipleInputQuestion
     */

    @Test
    fun testMultipleInputQuestion_Serialization() {
        val inputString = """
           {
               "identifier": "foo",
               "type": "multipleInputQuestion",
               "title": "Hello World!",
               "subtitle": "Question subtitle",
               "detail": "Some text. This is a test.",
               "footnote": "This is a footnote.",
               "image"  : {    "type" : "animated",
                               "imageNames" : ["foo1", "foo2", "foo3", "foo4"],
                               "placementType" : "topBackground",
                               "animationDuration" : 2
                                  },
                "optional": false,
                "sequenceSeparator": ",",
                "inputItems":[
                    {"type" : "string"},
                    {"type" : "year"},
                    {"type" : "integer"},
                    {"type" : "decimal"}
                ],
                "skipCheckbox":{"type":"skipCheckbox","fieldLabel":"No answer"}
           }
           """
        val original = MultipleInputQuestionObject(
                identifier = "foo",
                inputItems = listOf(
                    StringTextInputItemObject(),
                    YearTextInputItemObject(),
                    IntegerTextInputItemObject(),
                    DecimalTextInputItemObject()),
                sequenceSeparator = ",")
        original.optional = false
        original.title = "Hello World!"
        original.subtitle = "Question subtitle"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.imageInfo = AnimatedImage(
                imageNames = listOf("foo1", "foo2", "foo3", "foo4"),
                imagePlacement = ImagePlacement.Standard.TopBackground,
                animationDuration = 2.0)
        original.skipCheckbox = SkipCheckboxInputItemObject("No answer")

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is MultipleInputQuestionObject)
        assertEqualStep(original, decoded)
        assertEqualContentNodes(original, decoded)
        assertTrue(restored is MultipleInputQuestionObject)
        assertEqualStep(original, restored)
        assertEqualContentNodes(original, restored)
    }

    /**
     * SectionObject
     */

    @Test
    fun testSection_Serialization() {
        val inputString = """
            {
                "identifier": "foobar",
                "type": "section",
                "title": "Hello World!",
                "subtitle": "Subtitle",
                "detail": "Some text. This is a test.",
                "icon": "fooIcon",
                "footnote": "This is a footnote.",
                "actions": { "goForward": { "type": "default", "buttonTitle" : "Go, Dogs! Go!" },
                            "cancel": { "type": "default", "iconName" : "closeX" }
                           },
                "shouldHideActions": ["goBackward"],
                "progressMarkers": ["step1","step2"],
                "steps": [
                    {
                        "identifier": "step1",
                        "type": "instruction",
                        "title": "Step 1"
                    },
                    {
                        "identifier": "step2",
                        "type": "instruction",
                        "title": "Step 2"
                    }
                ]
            }
            """

        val original = SectionObject(
                identifier = "foobar",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        original.title = "Hello World!"
        original.subtitle = "Subtitle"
        original.detail = "Some text. This is a test."
        original.footnote = "This is a footnote."
        original.hideButtons = listOf(ButtonAction.Navigation.GoBackward)
        original.buttonMap = mapOf(
                ButtonAction.Navigation.GoForward to ButtonObject(buttonTitle = "Go, Dogs! Go!"),
                ButtonAction.Navigation.Cancel to ButtonObject(imageInfo = FetchableImage("closeX")))
        original.imageInfo = FetchableImage("fooIcon")
        original.progressMarkers = listOf("step1", "step2")

        val serializer = PolymorphicSerializer(Node::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertTrue(decoded is SectionObject)
        assertContainerNode(original, decoded)
        assertEqualContentNodes(original, decoded)

        assertTrue(restored is SectionObject)
        assertContainerNode(original, restored)
        assertEqualContentNodes(original, restored)
    }

    @Test
    fun testSection_Result_NullResultId() {
        val original = SectionObject(
                identifier = "foo",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        val result = original.createResult()
        val expected = BranchNodeResultObject("foo")
        assertEquals(expected, result)
    }

    @Test
    fun testSection_Result_WithResultId() {
        val original = SectionObject(
                identifier = "foo",
                resultIdentifier = "bar",
                children = listOf(
                        buildInstructionStep("step1", "Step 1"),
                        buildInstructionStep("step2", "Step 2")))
        val result = original.createResult()
        val expected = BranchNodeResultObject("bar")
        assertEquals(expected, result)
    }
}

open class NodeSerializationTestHelper {
    /* MARK: Helper methods */

    fun assertEqualResultMapElement(expected: ResultMapElement, actual: ResultMapElement) {
        assertEquals(expected.identifier, actual.identifier)
        assertEquals(expected.resultIdentifier, actual.resultIdentifier)
        assertEquals(expected.comment, actual.comment)
    }

    fun assertEqualNodes(expected: Node, actual: Node) {
        assertEqualResultMapElement(expected, actual)
        assertEquals(expected.hideButtons, actual.hideButtons)
        assertEquals(expected.buttonMap, actual.buttonMap)
    }

    fun assertEqualContentNodes(expected: ContentNode, actual: ContentNode) {
        assertEquals(expected.title, actual.title)
        assertEquals(expected.subtitle, actual.subtitle)
        assertEquals(expected.detail, actual.detail)
        assertEquals(expected.imageInfo, actual.imageInfo)
        assertEquals(expected.footnote, actual.footnote)
        assertEquals(expected.hideButtons, actual.hideButtons)
        assertEquals(expected.buttonMap, actual.buttonMap)
    }

    fun assertContainerNode(expected: NodeContainer, actual: NodeContainer) {
        assertEqualNodes(expected, actual)
        assertEquals(expected.progressMarkers, actual.progressMarkers)
        assertEquals(expected.children.count(), actual.children.count())
        actual.children.forEachIndexed { index, node ->
            assertEqualNodes(expected.children[index], node)
        }
    }

    fun assertEqualOptionalStep(expected: OptionalStep, actual: OptionalStep) {
        assertEqualStep(expected, actual)
        assertEquals(expected.fullInstructionsOnly, actual.fullInstructionsOnly)
    }

    fun assertEqualStep(expected: Step, actual: Step) {
        assertEqualNodes(expected, actual)
        assertEquals(expected.spokenInstructions, actual.spokenInstructions)
    }

    fun buildInstructionStep(identifier: String, title: String): InstructionStepObject {
        val instruction = InstructionStepObject(identifier)
        instruction.title = title
        return instruction
    }
}