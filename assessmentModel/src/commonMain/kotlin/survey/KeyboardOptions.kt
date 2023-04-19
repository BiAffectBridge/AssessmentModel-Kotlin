package org.sagebionetworks.assessmentmodel.survey

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.sagebionetworks.assessmentmodel.StringEnum
import org.sagebionetworks.assessmentmodel.matching

/**
 * Defines the options that can be used for a text field.
 */
interface KeyboardOptions {

    /**
     * Is the text field for password entry?
     */
    val isSecureTextEntry: Boolean

    /**
     * Auto-capitalization type for the text field.
     */
    val autocapitalizationType: AutoCapitalizationType

    /**
     * Auto-correction type for the text field.
     */
    val autocorrectionType: AutoCorrectionType

    /**
     * Spell checking type for the text field.
     */
    val spellCheckingType: SpellCheckingType

    /**
     * Keyboard type for the text field.
     *
     * For devices that do not support all these keyboard types (or where its use is not applicable) the OS is
     * responsible for determining what is the best mapping.
     */
    val keyboardType: KeyboardType
}

@Serializable(with = AutoCapitalizationType.AutoCapitalizationTypeSerializer::class)
enum class AutoCapitalizationType : StringEnum {
    None, Words, Sentences, AllCharacters;

    @Serializer(forClass = AutoCapitalizationType::class)
    object AutoCapitalizationTypeSerializer : KSerializer<AutoCapitalizationType>{
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AutoCapitalizationType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): AutoCapitalizationType {
            val name = decoder.decodeString()
            return values().matching(name) ?: throw SerializationException("Unknown $name for ${descriptor.serialName}. Needs to be one of ${values()}")
        }
        override fun serialize(encoder: Encoder, value: AutoCapitalizationType) {
            encoder.encodeString(value.name)
        }
    }
}

@Serializable(with = AutoCorrectionType.AutoCorrectionTypeSerializer::class)
enum class AutoCorrectionType : StringEnum {
    Default, No, Yes;

    @Serializer(forClass = AutoCorrectionType::class)
    object AutoCorrectionTypeSerializer : KSerializer<AutoCorrectionType>{
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("AutoCorrectionType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): AutoCorrectionType {
            val name = decoder.decodeString()
            return values().matching(name) ?: throw SerializationException("Unknown $name for ${descriptor.serialName}. Needs to be one of ${values()}")
        }
        override fun serialize(encoder: Encoder, value: AutoCorrectionType) {
            encoder.encodeString(value.name)
        }
    }
}

@Serializable(with = SpellCheckingType.SpellCheckingTypeSerializer::class)
enum class SpellCheckingType : StringEnum {
    Default, No, Yes;

    @Serializer(forClass = SpellCheckingType::class)
    object SpellCheckingTypeSerializer : KSerializer<SpellCheckingType>{
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("SpellCheckingType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): SpellCheckingType {
            val name = decoder.decodeString()
            return values().matching(name) ?: throw SerializationException("Unknown $name for ${descriptor.serialName}. Needs to be one of ${values()}")
        }
        override fun serialize(encoder: Encoder, value: SpellCheckingType) {
            encoder.encodeString(value.name)
        }
    }
}

@Serializable(with = KeyboardType.KeyboardTypeSerializer::class)
enum class KeyboardType : StringEnum {
    Default,
    AsciiCapable,
    NumbersAndPunctuation,
    URL,
    NumberPad,
    PhonePad,
    NamePhonePad,
    EmailAddress,
    DecimalPad,
    Twitter,
    WebSearch,
    AsciiCapableNumberPad,
    ;

    @Serializer(forClass = KeyboardType::class)
    object KeyboardTypeSerializer : KSerializer<KeyboardType>{
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("KeyboardType", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): KeyboardType {
            val name = decoder.decodeString()
            return values().matching(name) ?: throw SerializationException("Unknown $name for ${descriptor.serialName}. Needs to be one of ${ values() }")
        }
        override fun serialize(encoder: Encoder, value: KeyboardType) {
            encoder.encodeString(value.name)
        }
    }
}