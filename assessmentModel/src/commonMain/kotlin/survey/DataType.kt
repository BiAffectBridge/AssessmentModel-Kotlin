package org.sagebionetworks.assessmentmodel.forms

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import org.sagebionetworks.assessmentmodel.StringEnum
import org.sagebionetworks.assessmentmodel.matching
import org.sagebionetworks.assessmentmodel.Question

/**
 * [DataType] is used to describe the data type for a [Question]. This is tied to the category of the question as well
 * as the expected cast of the answer value.
 */
@Serializable
sealed class DataType() : StringEnum {
    abstract val baseType: BaseType

    /**
     * A mapping of the [UIHint] types for a given [DataType].
     */
    open val standardUIHints: List<UIHint>
        get() = baseType.standardUIHints

    /**
     * Base data types are basic types that can be defined with only a [baseType].
     */
    data class Base(override val baseType: BaseType)
        : DataType() {
        override val name: String
            get() = baseType.name
        companion object {
            fun values() : List<Base> = BaseType.values().map { Base(it) }
            fun valueOfOrNull(name: String): Base? = values().toTypedArray().matching(name)
        }
    }

    /**
     * Collection data types are some kind of a collection with a [baseType].
     */
    data class Collection(val collectionType: CollectionType, override val baseType: BaseType = defaultBaseType)
        : DataType() {
        override val name: String
            get() = "${collectionType.name}${if (baseType == defaultBaseType) "" else ".${baseType.name}"}"
        override val standardUIHints: List<UIHint>
            get() = when (collectionType) {
                CollectionType.SingleChoice -> UIHint.Choice.values().toList<UIHint>()
                        .plus(UIHint.Picker)
                CollectionType.MultipleChoice -> UIHint.Choice.values().toList<UIHint>()
                        .minus(UIHint.Choice.RadioButton)
                        .plus(UIHint.Picker)
                CollectionType.MultipleComponent -> listOf(UIHint.Picker, UIHint.TextField.Default)
            }
        companion object {
            val defaultBaseType = BaseType.String
            fun values(): List<Collection> = CollectionType.values().flatMap { collectionType ->
                BaseType.values().map { Collection(collectionType, it) }
            }
            fun valueOfOrNull(name: String): Collection? = values().toTypedArray().matching(name)
        }
    }

    /**
     * A date that includes encoding information for the portion of the date/time that is represented by this data type.
     */
    data class Date(val dateRangeType: DateRangeType)
        : DataType() {
        override val baseType: BaseType
            get() = BaseType.Date
        override val name: String
            get() = dateRangeType.serialName ?: dateRangeType.name
        companion object {
            fun values(): List<Date> = DateRangeType.values().map { Date(it) }
            fun valueOfOrNull(name: String): Date? = values().toTypedArray().matching(name)
        }
    }

    /**
     * A measurement is a human-data measurement. The measurement range indicates the expected size of the human being
     * measured. In US Customary units, this is required to determine the expected localization for the measurement.
     *
     * For example, an infant weight would be in lb/oz whereas an adult weight would be in lb. Default range is for an
     * adult.
     */
    data class Measurement(val measurementType: MeasurementType, val measurementRange: MeasurementRange = defaultRange)
        : DataType() {
        override val name: String
            get() = "${measurementType.name}${if (measurementRange == defaultRange) "" else ".${measurementRange.name}"}"
        override val baseType
            get() = if (measurementType == MeasurementType.BloodPressure) BaseType.Integer else BaseType.Decimal
        override val standardUIHints: List<UIHint>
            get() = when (measurementType) {
                MeasurementType.BloodPressure -> listOf(UIHint.TextField.Default)
                else -> listOf(UIHint.Picker, UIHint.TextField.Default)
            }
        companion object {
            val defaultRange = MeasurementRange.Adult
            fun values(): List<Measurement> = MeasurementType.values().flatMap { measurementType ->
                MeasurementRange.values().map { Measurement(measurementType, it) }
            }
            fun valueOfOrNull(name: String): Measurement? = values().toTypedArray().matching(name)
        }
    }

    /**
     * Custom data types are undefined in the base SDK.
     */
    data class Custom(val serialName: String, override val baseType: BaseType = defaultBaseType)
        : DataType() {
        override val name: String
            get() = "${serialName}${if (baseType == defaultBaseType) "" else ".${baseType.name}"}"
        companion object {
            val defaultBaseType = BaseType.Codable
            fun valueOf(name: String): Custom {
                val split = name.split(".")
                val baseType = if (split.count() == 2) BaseType.values().matching(split.last()) else null
                val first = if (baseType == null) name else split.first()
                return Custom(first, baseType ?: defaultBaseType)
            }
        }
    }

    /**
     * The base type of the form input field. This is used to indicate what the type is of the value being prompted
     * and will affect the choice of allowed formats.
     */
    enum class BaseType : StringEnum {

        /**
         * The Boolean question type asks the participant to enter Yes or No (or the appropriate equivalents).
         */
        Boolean,

        /**
         * In a date question, the participant can enter a date, time, or combination of the two. A date data type can
         * map to a [DateRange] to box the allowed values.
         */
        Date,

        /**
         * The decimal question type asks the participant to enter a decimal number. A decimal data type can map to a
         * [NumberRange] to box the allowed values.
         */
        Decimal,

        /**
         * In a duration question, the participant can enter a time span such as "8 hours, 5 minutes" or
         * "3 minutes, 15 seconds".
         */
        Duration,

        /**
         * The fraction question type asks the participant to enter a fractional number. A fractional data type can map
         * to a [NumberRange] to box the allowed values.
         */
        Fraction,

        /**
         * The integer question type asks the participant to enter an integer number. An integer data type can
         * map to a [NumberRange] to box the allowed values, but will store the value as an [Int].
         */
        Integer,

        /**
         * In a string question, the participant can enter text.
         */
        String,

        /**
         * In a year question, the participant can enter a year when an event occurred. A year data type can map
         * to a [DateRange] or [NumberRange] to box the allowed values.
         */
        Year,

        /**
         * A Serializable object. This is an object that can be represented using a JSON or XML dictionary.
         */
        Codable,
        ;

        val standardUIHints: List<UIHint>
            get() = when (this) {
                Boolean -> UIHint.Choice.values().toList<UIHint>().plus(UIHint.Picker)
                Date -> listOf(UIHint.Picker, UIHint.TextField.Default)
                String -> UIHint.TextField.values().toList()
                Codable -> UIHint.Detail.values().toList<UIHint>().plus(UIHint.TextField.Default)
                else -> listOf(UIHint.TextField.Default, UIHint.Picker)
            }
    }

    /**
     * The collection type for the input field. The supported types are for choice-style questions or multiple component
     * questions where the user selected from one or more fields to build a single answer result.
     */
    enum class CollectionType : StringEnum {

        /**
         * In a multiple choice question, the participant can pick one or more options.
         */
        MultipleChoice,

        /**
         * In a single choice question, the participant can pick one item from a list of options.
         */
        SingleChoice,

        /**
         * In a multiple component question, the participant can pick one choice from each component or enter a
         * formatted text string such as a phone number or blood pressure.
         */
        MultipleComponent,
        ;
    }

    /**
     * A measurement type is a human-data measurement such as height or weight.
     */
    enum class MeasurementType : StringEnum {

        /**
         * A measurement of height.
         */
        Height,

        /**
         * A measurement of weight.
         */
        Weight,

        /**
         * A measurement of blood pressure.
         */
        BloodPressure,
        ;
    }

    /**
     * The measurement range is used to determine units that are appropriate to the size of the person.
     */
    enum class MeasurementRange : StringEnum {

        /**
         * Measurement units should be ranged for an adult.
         */
        Adult,

        /**
         * Measurement units should be ranged for a child.
         */
        Child,

        /**
         * Measurement units should be ranged for an infant.
         */
        Infant,
        ;
    }

    /**
     * The `DateRangeType` is used to simplify transforming classes on Android.
     *
     * Android has different classes for the common date components that may be of interest when asking the participant
     * about a "date". Because of this, for many cases, that platform does not need to define an `RSDDateRange` to
     * differentiate between *which* subset of date components should be requested. This form data type is used to
     * differentiate between these different types. While on iOS, this requires some custom handling in the data source,
     * it greatly simplifies decoding on Android and is thus supported here.
     *
     * The date range type *only* supports those types that are native to Android. Custom date components such as year
     * only or year and month only still require defining the supported ranges using the [DateRange] interface.
     *
     * Additionally, the `rawValue` key of "date" still maps to `.base(.date)` to maintain reverse-compatibility to
     * existing JSON-encoded objects.
     */
    enum class DateRangeType(val serialName: String? = null) : StringEnum {

        /**
         * Includes time, date, and GMT timezone offset ("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ").
         */
        Timestamp(),

        /**
         * Includes date only ("yyyy-MM-dd").
         *
         * - note: This framework already maps the `rawValue` of "date" to the `.base(.date)` data type which is a
         * generic that can be used to define dates independently of which components are displayed to the user.
         */
        DateOnly(),

        /**
         * Includes time only ("HH:mm:ss").
         * - note: Use a custom naming key of "time" to match the `rawValue` already defined for Android.
         */
        TimeOnly("Time"),
        ;

        /**
         * The default coding date format.
         */
        val dateFormat: String
            get() =
                when (this) {
                    Timestamp -> "yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"
                    DateOnly -> "yyyy-MM-dd"
                    TimeOnly -> "HH:mm:ss"
                }
    }

    @Serializer(forClass = DataType::class)
    companion object : KSerializer<DataType> {
        override val descriptor: SerialDescriptor
                = StringDescriptor.withName("DataType")
        override fun deserialize(decoder: Decoder): DataType {
            val name = decoder.decodeString()
            return valueOf(name)
        }
        override fun serialize(encoder: Encoder, obj: DataType) {
            encoder.encodeString(obj.name)
        }
        fun valueOf(name: String): DataType {
            return  Base.valueOfOrNull(name)
                    ?: Collection.valueOfOrNull(name)
                    ?: Date.valueOfOrNull(name)
                    ?: Measurement.valueOfOrNull(name)
                    ?: Custom.valueOf(name)
        }
    }

//  TODO: syoung 02/05/2020 Implement or delete.
//    /// A postal code is a custom input field that only stores a part of the participant's postal
//    /// code (zipcode). This is to protect the participant's privacy. Typically, this will mean
//    /// only storing the first 3 characters of the postal code. The base type for a postal code is
//    /// always a string.
//    case postalCode

// TODO: syoung 02/05/2020 Figure out a clean way to implement this mapping in Kotlin. The syntax below is unreadable.
//
//    /// List of the standard UI hints that are valid for this data type.
//    ///
//    /// The valid hints are returned in priority order such that if the preferred hint is not
//    /// supported by the UI then a fall-back will be selected. For example, `.base(.date)` will
//    /// return `.picker` as its preferred hint, whereas `.base(.integer)` will return `.textfield`,
//    /// but both support `.textfield` *and* `.picker`.
//    ///
//    public var validStandardUIHints: [RSDFormUIHint] {
//        switch self {
//            case .base(let baseType):
//            switch baseType {
//                case .boolean:
//                return [.list, .checkbox, .radioButton, .toggle, .picker]
//
//                case .date, .duration:
//                return [.picker, .textfield]
//
//                case .decimal, .integer, .year, .fraction:
//                return [.textfield, .slider, .picker]
//
//                case .string:
//                return [.textfield, .multipleLine]
//
//                case .codable:
//                return [.disclosureArrow, .button, .link]
//            }
//
//            case .collection(let collectionType, _):
//            switch collectionType {
//                case .multipleChoice, .singleChoice:
//                return [.list, .checkbox, .combobox, .picker, .radioButton, .slider]
//
//                case .multipleComponent:
//                return [.picker, .textfield]
//            }
//
//            case .dateRange(_):
//            return [.picker, .textfield]
//
//            case .measurement(let measurementType, _):
//            switch measurementType {
//                case .bloodPressure:
//                return [.textfield]
//                case .height, .weight:
//                return [.picker, .textfield]
//            }
//
//            case .postalCode:
//            return [.textfield]
//
//            case .detail(_):
//            return [.disclosureArrow, .button, .link, .section]
//
//            case .custom(_):
//            return RSDFormUIHint.StandardHints.allCases.map { $0.hint }
//        }
//    }
//
//    /// The set of ui hints that can display using a list format such as a scrolling list.
//    public var listSelectionHints : Set<RSDFormUIHint> {
//        switch self {
//            case .collection(.singleChoice, _),
//            .collection(.multipleChoice, _),
//            .base(.boolean):
//            return [.list, .checkbox, .radioButton]
//
//            default:
//            return []
//        }
//    }

//    TODO: syoung 02/05/2020 Implement the AnswerResultType.
//    public func defaultAnswerResultType() -> RSDAnswerResultType {
//        let base = self.defaultAnswerResultBaseType()
//
//        switch self {
//            case .collection(let collectionType, _):
//            switch collectionType {
//                case .multipleChoice, .multipleComponent:
//                return RSDAnswerResultType(baseType: base, sequenceType: .array, formDataType: self, dateFormat: nil, unit: nil, sequenceSeparator: nil)
//                case .singleChoice:
//                return RSDAnswerResultType(baseType: base, sequenceType: nil, formDataType: self, dateFormat: nil, unit: nil, sequenceSeparator: nil)
//            }
//
//            case .dateRange(let rangeType):
//            return RSDAnswerResultType(baseType: .date, sequenceType: nil, formDataType: self, dateFormat: rangeType.dateFormat, unit: nil, sequenceSeparator: nil)
//
//            case .measurement(let measurementType, _):
//            switch measurementType {
//                case .bloodPressure:
//                return RSDAnswerResultType(baseType: .string, sequenceType: nil, formDataType: self, dateFormat: nil, unit: nil, sequenceSeparator: nil)
//                case .height, .weight:
//                return RSDAnswerResultType(baseType: .decimal, sequenceType: nil, formDataType: self, dateFormat: nil, unit: nil, sequenceSeparator: nil)
//            }
//
//            default:
//            return RSDAnswerResultType(baseType: base, sequenceType: nil, formDataType: self, dateFormat: nil, unit: nil, sequenceSeparator: nil)
//        }
//
//    }
//
//    /// Maps the base type of the `RSDFormDataType` to the base type of the `RSDAnswerResultType`.
//    ///
//    /// - returns: the default result answer type for this input field.
//    public func defaultAnswerResultBaseType() -> RSDAnswerResultType.BaseType {
//        switch self.baseType {
//            case .boolean:
//            return .boolean
//            case .date:
//            return .date
//            case .decimal, .fraction, .duration:
//            return .decimal
//            case .integer, .year:
//            return .integer
//            case .string:
//            return .string
//            case .codable:
//            return .codable
//        }
//    }
}
