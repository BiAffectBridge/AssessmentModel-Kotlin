package org.sagebionetworks.assessmentmodel.serialization

import kotlinx.serialization.*
import org.sagebionetworks.assessmentmodel.forms.*
import org.sagebionetworks.assessmentmodel.survey.FormattedValue
import org.sagebionetworks.assessmentmodel.survey.InvalidMessageObject
import kotlin.test.*

open class InputItemsTest {

    val jsonCoder = Serialization.JsonCoder.default

    @Serializable
    data class TestUIHintWrapper(val hints: List<UIHint>)

    /**
     * [UIHint] Tests
     */

    @Test
    fun testUIHint_Choice_Serialization() {
        val hints = UIHint.Choice.values().toList()
        val original = TestUIHintWrapper(hints)
        val inputString = """{"hints":["list","checkmark","checkbox","radiobutton"]}"""

        val jsonString = jsonCoder.stringify(TestUIHintWrapper.serializer(), original)
        val restored = jsonCoder.parse(TestUIHintWrapper.serializer(), jsonString)
        val decoded = jsonCoder.parse(TestUIHintWrapper.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(inputString.toLowerCase(), jsonString.toLowerCase())
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testUIHint_Detail_Serialization() {
        val hints = UIHint.Detail.values().toList()
        val original = TestUIHintWrapper(hints)
        val inputString = """{"hints":["disclosureArrow","button","link"]}"""

        val jsonString = jsonCoder.stringify(TestUIHintWrapper.serializer(), original)
        val restored = jsonCoder.parse(TestUIHintWrapper.serializer(), jsonString)
        val decoded = jsonCoder.parse(TestUIHintWrapper.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(inputString.toLowerCase(), jsonString.toLowerCase())
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testUIHint_TextField_Serialization() {
        val hints = UIHint.TextField.values().toList()
        val original = TestUIHintWrapper(hints)
        val inputString = """{"hints":["textfield","multipleLine","popover"]}"""

        val jsonString = jsonCoder.stringify(TestUIHintWrapper.serializer(), original)
        val restored = jsonCoder.parse(TestUIHintWrapper.serializer(), jsonString)
        val decoded = jsonCoder.parse(TestUIHintWrapper.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(inputString.toLowerCase(), jsonString.toLowerCase())
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testUIHint_CustomAndPicker_Serialization() {
        val hints = listOf(UIHint.Custom("foo"), UIHint.Picker)
        val original = TestUIHintWrapper(hints)
        val inputString = """{"hints":["foo","picker"]}"""

        val jsonString = jsonCoder.stringify(TestUIHintWrapper.serializer(), original)
        val restored = jsonCoder.parse(TestUIHintWrapper.serializer(), jsonString)
        val decoded = jsonCoder.parse(TestUIHintWrapper.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(inputString.toLowerCase(), jsonString.toLowerCase())
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Serializable
    data class UIHintTextFieldWrapper(val types: List<UIHint.TextField>)

    @Test
    fun testUIHintTextField_Cast_Serialization() {
        val original = UIHintTextFieldWrapper(UIHint.TextField.values().toList())
        val inputString = """{"types":["textfield","multipleLine","popover"]}"""

        val jsonString = jsonCoder.stringify(UIHintTextFieldWrapper.serializer(), original)
        val restored = jsonCoder.parse(UIHintTextFieldWrapper.serializer(), jsonString)
        val decoded = jsonCoder.parse(UIHintTextFieldWrapper.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(inputString.toLowerCase(), jsonString.toLowerCase())
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    /**
     * [TextFieldOptions] Tests
     */

    @Test
    fun testTextFieldOptions_Serialization() {

        val original = TextFieldOptionsObject(
                autocapitalizationType = AutoCapitalizationType.Words,
                autocorrectionType = AutoCorrectionType.No,
                keyboardType = KeyboardType.NumberPad,
                spellCheckingType = SpellCheckingType.No)
        val inputString = """
                {
                    "autocapitalizationType":"words",
                    "autocorrectionType":"no",
                    "keyboardType":"numberPad",
                    "spellCheckingType":"no"
                }
            """.trimIndent()

        val jsonString = jsonCoder.stringify(TextFieldOptionsObject.serializer(), original)
        val restored = jsonCoder.parse(TextFieldOptionsObject.serializer(), jsonString)
        val decoded = jsonCoder.parse(TextFieldOptionsObject.serializer(), inputString)

        // Look to see that the restored, decoded, and original all are equal
        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    /**
     * [StringTextInputItemObject] Tests
     */

    @Test
    fun testStringInputItemObject_Serialization() {
        val inputString = """
           {
            "identifier": "foo",
            "type": "string",
            "uiHint": "popover",
            "prompt": "Favorite color",
            "placeholder": "Blue, no! Red!",
            "textFieldOptions" : {
                        "autocapitalizationType" : "words",
                        "keyboardType" : "asciiCapable",
                        "isSecureTextEntry" : true },
            "regExValidator" : {
                        "pattern" : "[A:D]",
                        "invalidMessage" : "Only ABCD are valid letters."
            }
           }
           """
        val original = StringTextInputItemObject("foo")
        original.fieldLabel = "Favorite color"
        original.placeholder = "Blue, no! Red!"
        original.uiHint = UIHint.TextField.Popover
        original.textFieldOptions = TextFieldOptionsObject(
                autocapitalizationType = AutoCapitalizationType.Words,
                keyboardType = KeyboardType.AsciiCapable,
                isSecureTextEntry = true)
        original.regExValidator = RegExValidator("[A:D]", InvalidMessageObject("Only ABCD are valid letters."))

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testStringInputItemObject_DefaultValue_Serialization() {
        val inputString = """
           {
            "type": "string"
           }
           """
        val original = StringTextInputItemObject()

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    /**
     * [IntegerTextInputItemObject] Tests
     */

    @Test
    fun testIntNumberOptions_Serialization() {
        val inputString = """
           {
                "numberStyle" : "percent",
                "usesGroupingSeparator" : false,
                "minimumValue" : 0,
                "maximumValue" : 1000,
                "stepInterval" : 10,
                "minInvalidMessage" : "Min is zero",
                "maxInvalidMessage" : "Max is one thousand",
                "invalidMessage" : "You must enter an integer between 0 and 1000"
           }
           """

        val original = IntFormatOptions(
                numberStyle = NumberFormatOptions.Style.Percent,
                usesGroupingSeparator = false)
        original.minimumValue = 0
        original.maximumValue = 1000
        original.stepInterval = 10
        original.minInvalidMessage = InvalidMessageObject("Min is zero")
        original.maxInvalidMessage = InvalidMessageObject("Max is one thousand")
        original.invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")

        val serializer = IntFormatOptions.serializer()
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)

        assertEquals(original.minInvalidMessage, restored.minInvalidMessage)
        assertEquals(original.maxInvalidMessage, restored.maxInvalidMessage)
        assertEquals(original.invalidMessage, restored.invalidMessage)
    }

    @Test
    fun testIntNumberOptions_Format() {
        val original = IntFormatOptions(usesGroupingSeparator = false)
        original.minimumValue = 0
        original.maximumValue = 1000
        original.minInvalidMessage = InvalidMessageObject("Min is zero")
        original.maxInvalidMessage = InvalidMessageObject("Max is one thousand")
        original.invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")

        val formatter = IntFormatter(original)

        val retString0: FormattedValue<String> = formatter.localizedStringFor(5)
        assertEquals(FormattedValue("5"), retString0)

        val retString1: FormattedValue<String> = formatter.localizedStringFor(1000)
        assertEquals(FormattedValue("1000"), retString1)

        val validate1 = original.validate(10)
        assertEquals(FormattedValue(10), validate1)

        val retVal0 = formatter.valueFor("10")
        assertEquals(FormattedValue(10), retVal0)


//        val retVal1 = formatter.valueFor("-1")
//        assertEquals(FormattedValue(invalidMessage = InvalidMessageObject("Min is zero")), retVal1)
//
//        val retVal2 = formatter.valueFor("2000")
//        assertEquals(FormattedValue(invalidMessage = InvalidMessageObject("Max is one thousand")), retVal2)
//
//        val retVal3 = formatter.valueFor("foo")
//        assertEquals(FormattedValue(invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")), retVal3)
    }

    @Test
    fun testIntInputItemObject_Serialization() {
        val inputString = """
           {
            "identifier": "foo",
            "type": "integer",
            "uiHint": "popover",
            "prompt": "Favorite color",
            "placeholder": "Blue, no! Red!",
            "textFieldOptions" : {
                        "keyboardType" : "NumbersAndPunctuation",
                        "isSecureTextEntry" : true },
            "formatOptions" : {
                        "usesGroupingSeparator" : false,
                        "minimumValue" : 0,
                        "maximumValue" : 1000,
                        "stepInterval" : 10,
                        "minInvalidMessage" : "Min is zero",
                        "maxInvalidMessage" : "Max is one thousand",
                        "invalidMessage" : "You must enter an integer between 0 and 1000"
            }
           }
           """
        val original = IntegerTextInputItemObject("foo")
        original.fieldLabel = "Favorite color"
        original.placeholder = "Blue, no! Red!"
        original.uiHint = UIHint.TextField.Popover
        original.textFieldOptions = TextFieldOptionsObject(
                keyboardType = KeyboardType.NumbersAndPunctuation,
                isSecureTextEntry = true)
        original.formatOptions = IntFormatOptions(usesGroupingSeparator = false)
        original.formatOptions.minimumValue = 0
        original.formatOptions.maximumValue = 1000
        original.formatOptions.stepInterval = 10
        original.formatOptions.minInvalidMessage = InvalidMessageObject("Min is zero")
        original.formatOptions.maxInvalidMessage = InvalidMessageObject("Min is one thousand")
        original.formatOptions.invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testIntInputItemObject_DefaultValue_Serialization() {
        val inputString = """
           {
            "type": "integer"
           }
           """
        val original = IntegerTextInputItemObject()

        // Check the defaults for an integer
        assertTrue(original.formatOptions.usesGroupingSeparator)
        assertEquals(TextFieldOptionsObject.NumberEntryOptions, original.textFieldOptions)

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }


    /**
     * [YearTextInputItemObject] Tests
     */

    @Test
    fun testYearInputItemObject_Serialization() {
        val inputString = """
           {
            "identifier": "foo",
            "type": "year",
            "uiHint": "popover",
            "prompt": "Favorite color",
            "placeholder": "Blue, no! Red!"
           }
           """
        val original = YearTextInputItemObject("foo")
        original.fieldLabel = "Favorite color"
        original.placeholder = "Blue, no! Red!"
        original.uiHint = UIHint.TextField.Popover

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testYearInputItemObject_DefaultValue_Serialization() {
        val inputString = """
           {
            "type": "year"
           }
           """
        val original = YearTextInputItemObject()

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }


    /**
     * [DecimalTextInputItemObject] Tests
     */

    @Test
    fun testDoubleFormatOptions_Serialization() {
        val inputString = """
           {
                "numberStyle" : "percent",
                "usesGroupingSeparator" : false,
                "maximumFractionDigits" : 1,
                "minimumValue" : 0,
                "maximumValue" : 1000,
                "stepInterval" : 10,
                "minInvalidMessage" : "Min is zero",
                "maxInvalidMessage" : "Max is one thousand",
                "invalidMessage" : "You must enter an integer between 0 and 1000"
           }
           """

        val original = DoubleFormatOptions(
                numberStyle = NumberFormatOptions.Style.Percent,
                usesGroupingSeparator = false,
                maximumFractionDigits = 1)
        original.minimumValue = 0.0
        original.maximumValue = 1000.0
        original.stepInterval = 10.0
        original.minInvalidMessage = InvalidMessageObject("Min is zero")
        original.maxInvalidMessage = InvalidMessageObject("Min is one thousand")
        original.invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")

        val serializer = DoubleFormatOptions.serializer()
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testDoubleFormatOptions_Format() {
        val original = DoubleFormatOptions(numberStyle = NumberFormatOptions.Style.Percent)
        original.minimumValue = 0.0
        original.maximumValue = 1.0
        val invalidMessage = InvalidMessageObject("You must enter a percentage between 0% and 100%")
        original.invalidMessage = invalidMessage

        val formatter = DoubleFormatter(original)

        val retString: FormattedValue<String> = formatter.localizedStringFor(0.05)
        assertEquals(FormattedValue("5%"), retString)

        val retVal0 = formatter.valueFor("3%")
        assertEquals(FormattedValue(0.03), retVal0)

        val expected = FormattedValue<Double>(invalidMessage = invalidMessage)
        val retVal1 = formatter.valueFor("-1")
        assertEquals(expected, retVal1)

        val retVal2 = formatter.valueFor("150%")
        assertEquals(expected, retVal2)

        val retVal3 = formatter.valueFor("foo")
        assertEquals(expected, retVal3)
    }

    @Test
    fun testDecimalInputItemObject_Serialization() {
        val inputString = """
           {
            "identifier": "foo",
            "type": "decimal",
            "uiHint": "popover",
            "prompt": "Favorite color",
            "placeholder": "Blue, no! Red!",
            "textFieldOptions" : {
                        "keyboardType" : "NumbersAndPunctuation",
                        "isSecureTextEntry" : true },
            "formatOptions" : {
                        "usesGroupingSeparator" : false,
                        "minimumValue" : 0,
                        "maximumValue" : 1000,
                        "stepInterval" : 10,
                        "minInvalidMessage" : "Min is zero",
                        "maxInvalidMessage" : "Max is one thousand",
                        "invalidMessage" : "You must enter an integer between 0 and 1000"
            }
           }
           """
        val original = DecimalTextInputItemObject("foo")
        original.fieldLabel = "Favorite color"
        original.placeholder = "Blue, no! Red!"
        original.uiHint = UIHint.TextField.Popover
        original.textFieldOptions = TextFieldOptionsObject(
                keyboardType = KeyboardType.NumbersAndPunctuation,
                isSecureTextEntry = true)
        original.formatOptions = DoubleFormatOptions(usesGroupingSeparator = false)
        original.formatOptions.minimumValue = 0.0
        original.formatOptions.maximumValue = 1000.0
        original.formatOptions.stepInterval = 10.0
        original.formatOptions.minInvalidMessage = InvalidMessageObject("Min is zero")
        original.formatOptions.maxInvalidMessage = InvalidMessageObject("Min is one thousand")
        original.formatOptions.invalidMessage = InvalidMessageObject("You must enter an integer between 0 and 1000")

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }

    @Test
    fun testDecimalTextInputItemObject_DefaultValue_Serialization() {
        val inputString = """
           {
            "type": "decimal"
           }
           """
        val original = DecimalTextInputItemObject()

        // Check the defaults for an integer
        assertTrue(original.formatOptions.usesGroupingSeparator)
        assertEquals(TextFieldOptionsObject.NumberEntryOptions, original.textFieldOptions)

        val serializer = PolymorphicSerializer(InputItem::class)
        val jsonString = jsonCoder.stringify(serializer, original)
        val restored = jsonCoder.parse(serializer, jsonString)
        val decoded = jsonCoder.parse(serializer, inputString)

        assertEquals(original, restored)
        assertEquals(original, decoded)
    }
}