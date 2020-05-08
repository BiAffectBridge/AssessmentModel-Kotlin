package org.sagebionetworks.assessmentmodel

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import org.sagebionetworks.assessmentmodel.navigation.NodeIdentifierPath
import org.sagebionetworks.assessmentmodel.navigation.Navigator
import org.sagebionetworks.assessmentmodel.navigation.NodeNavigator
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.resourcemanagement.ResourceInfo
import org.sagebionetworks.assessmentmodel.resourcemanagement.copyResourceInfo
import org.sagebionetworks.assessmentmodel.serialization.*

/**
 * A [Session] includes one or more [assessments] that are logically grouped together.
 *
 * The SageResearch equivalent is an `RSDTaskGroup`.
 *
 * TODO: syoung 01/28/2020 Flesh out the interface for a session including how to get its result.
 */
interface Session {
    val assessments: List<Assessment>
}

interface BranchNode : Node {

    /**
     * The [Navigator] for this assessment.
     */
    fun getNavigator(): Navigator

    // Override the default implementation to return a [BranchNodeResult]
    override fun createResult(): BranchNodeResult
            = BranchNodeResultObject(resultId())
}

/**
 * An [Assessment] is used to define the model information used to gather assessment (measurement) data needed for a
 * given study. It can include both the information needed to display a [Step] sequence to the participant as well as
 * the [AsyncActionConfiguration] data used to set up asynchronous actions such as sensors or web services that can be
 * used to inform the results.
 */
interface Assessment : BranchNode, ContentNode {

    /**
     * The [versionString] may be a semantic version, timestamp, or sequential revision integer.
     */
    val versionString: String?

    /**
     * The estimated number of minutes that the assessment will take. If `0`, then it is assumed that this value is not
     * defined. Where provided, it can be used by an application to indicate to the participant approximately how
     * long an assessment is expected to take to complete.
     */
    val estimatedMinutes: Int

    // Override the default implementation to return an [AssessmentResult]
    override fun createResult(): AssessmentResult
            = AssessmentResultObject(resultId(), versionString)

    override fun unpack(fileLoader: FileLoader, resourceInfo: ResourceInfo, jsonCoder: Json): Assessment
}

/**
 * A result map element is an element in the [Assessment] model that defines the expectations for a [Result] associated
 * with this element. It can define a user-facing step, a section (which may or may not map to a view), a background
 * web service, a sensor recorder, or any other piece of data collected by the overall [Assessment].
 *
 * The [identifier] is used to build the input model mapping and the [resultIdentifier] is used to build an output model
 * with the same hierarchy where all the elements of the model conform to the [Result] protocol.
 */
interface ResultMapElement {

    /**
     * The identifier for the node.
     */
    val identifier: String

    /**
     * The identifier to use for the [Result] (if any) associated with this node. If null, the [identifier] will be
     * used instead.
     */
    val resultIdentifier: String?

    /**
     * The [comment] is *not* intended to be user-facing and is a field that allows the [Assessment] designer to add
     * explanatory text describing the purpose of the assessment, section, step, or background action.
     */
    val comment: String?

    /**
     * Create an appropriate instance of a *new* [Result] for this map element.
     */
    fun createResult(): Result = ResultObject(resultId())
}

/**
 * Convenience method for accessing the result identifier associated with a given node.
 */
fun ResultMapElement.resultId() : String = resultIdentifier ?: identifier

/**
 * A [Node] is any object defined within the structure of an [Assessment] that is used to display a sequence of [Step]
 * nodes. All nodes have an [identifier] string that can be used to uniquely identify the node.
 */
interface Node : ResultMapElement {

    /**
     * List of button actions that should be hidden for this node even if the node subtype typically supports displaying
     * the button on screen. This property can be defined at any level and will default to whichever is the lowest level
     * for which this mapping is defined.
     */
    val hideButtons: List<ButtonAction>

    /**
     * A mapping of a [ButtonAction] to a [ButtonActionInfo].
     *
     * For example, this mapping can be used to define the url for a [ButtonAction.Navigation.LearnMore] link or to
     * customize the title of the [ButtonAction.Navigation.GoForward] button. It can also define the title, icon, etc.
     * on a custom button as long as the application knows how to interpret the custom action.
     *
     * Finally, a mapping can be used to explicitly mark a button as "should display" even if the overall assessment or
     * section includes the button action in the list of hidden buttons. For example, an assessment may define the
     * skip button as hidden but a lower level step within that assessment's hierarchy can return a mapping for the
     * skip button. The lower level mapping should be respected and the button should be displayed for that step only.
     */
    val buttonMap: Map<ButtonAction, ButtonActionInfo>

    /**
     * Unpack (and potentially replace) the node and set up any required resource pointers.
     */
    fun unpack(fileLoader: FileLoader, resourceInfo: ResourceInfo, jsonCoder: Json): Node = this
}

/**
 * A [ContentNode] contains additional content that may, under certain circumstances and where screen real estate
 * allows, be displayed to the participant to help them understand the intended purpose of the part of the assessment
 * described by this [Node].
 */
interface ContentNode : Node {

    /**
     * The primary text to display for the node in a localized string. The UI should display this using a larger font.
     */
    val title: String?

    /**
     * A [subtitle] to display for the node in a localized string.
     */
    val subtitle: String?

    /**
     * Detail text to display for the node in a localized string.
     */
    val detail: String?

    /**
     * An image or animation to display with this node.
     */
    val imageInfo: ImageInfo?

    /**
     *
     * Additional text to display for the node in a localized string at the bottom of the view.
     *
     * The footnote is intended to be displayed in a smaller font at the bottom of the screen. It is intended to be
     * used in order to include disclaimer, copyright, etc. that is important to display to the participant but should
     * not distract from the main purpose of the [Step] or [Assessment].
     */
    val footnote: String?
        get() = null

    override fun unpack(fileLoader: FileLoader, resourceInfo: ResourceInfo, jsonCoder: Json): Node {
        imageInfo?.copyResourceInfo(resourceInfo)
        return super.unpack(fileLoader, resourceInfo, jsonCoder)
    }
}

/**
 * A [NodeContainer] has a collection of child nodes defined by the [children]. Whether or not these child nodes are
 * presented in a single screen will depend upon the platform and the UI/UX defined by the [Assessment] designers.
 */
interface NodeContainer : BranchNode {

    /**
     * The children contained within this collection.
     */
    val children: List<Node>

    /**
     * A list of the node identifiers to include in showing progress through the section or assessment. This is used
     * by a [NodeNavigator] to calculate progress.
     */
    val progressMarkers: List<String>?

    override fun getNavigator(): Navigator = NodeNavigator(this)
}

/**
 * Convenience method for mapping the child nodes to their identifiers.
 */
fun NodeContainer.allNodeIdentifiers(): List<String> = children.map { it.identifier }

/**
 * An [AsyncActionContainer] is a node that contains the model description for asynchronous background actions that
 * should be started when this [Node] in the [Assessment] is presented to the user.
 */
interface AsyncActionContainer : Node {

    /**
     * A list of the [AsyncActionConfiguration] elements used to describe the configuration for background actions
     * (such as a sensor recorder or web service) that should should be started when this [Node] in the [Assessment] is
     * presented to the user.
     */
    val backgroundActions: List<AsyncActionConfiguration>
}

/**
 * A [Section] is used to define a logical sub-grouping of nodes and asynchronous background actions such as a section
 * in a longer survey or an active node that includes an instruction step, countdown step, and activity step.
 *
 * A [Section] is different from an [Assessment] in that it *always* describes a subgrouping of nodes that can be
 * displayed sequentially for platforms where the available screen real-estate does not support displaying the nodes
 * on a single view. A [Section] is also different from an [Assessment] in that it is a sub-node and does *not*
 * contain a measurement which, alone, is valuable to a study designer.
 */
interface Section : NodeContainer, ContentNode

/**
 * A user-interface step in an [Assessment].
 *
 * This is the base interface for the steps that can compose an assessment for presentation using a controller
 * appropriate to the device and application. Each [Step] object represents one logical piece of data entry,
 * information, or activity in a larger assessment.
 *
 * A step can be a question, an active test, or a simple instruction. It is typically paired with a step controller that
 * controls the actions of the [Step].
 */
interface Step : Node {

    /**
     * A mapping of the localized text that represents an instructional voice prompt to the time marker for speaking
     * the instruction. Time markers can be defined by a set of key words or as time intervals. Any step *could* include
     * a time marker on the step, though typically, this will only apply to active steps.
     *
     * - Example:
     * ```
     * {
     *      "start": "Start moving",
     *      "10": "Keep going",
     *      "halfway": "Halfway there",
     *      "countdown": "5",
     *      "end": "Stop moving"
     * }
     * ```
     */
    val spokenInstructions: Map<SpokenInstructionTiming, String>?

    /**
     * The [ViewTheme] to use to provide a custom view for this step.
     */
    val viewTheme: ViewTheme?
        get() = null
}

/**
 * [OverviewStep] extends [Step] to include general overview information about an [Assessment].
 */
interface OverviewStep : PermissionStep {

    /**
     * Detail text to display for the node in a localized string. For an overview step, the detail is readwrite.
     */
    override var detail: String?

    /**
     * The learn more button for the assessment that this overview step is describing. This is defined as readwrite so
     * that researchers who are using the [Assessment] as a part of their application can define a custom learn more
     * action.
     */
    var learnMore: ButtonActionInfo?

    /**
     * The [icons] that are used to define the list of things you will need for an active assessment.
     */
    val icons: List<ImageInfo>?

    override fun unpack(fileLoader: FileLoader, resourceInfo: ResourceInfo, jsonCoder: Json): Node {
        icons?.forEach { it.copyResourceInfo(resourceInfo) }
        return super.unpack(fileLoader, resourceInfo, jsonCoder)
    }
}

/**
 * [PermissionStep] extends the [Step] to include information about an activity including what permissions are
 * required by this step or assessment. Without these preconditions, the [Assessment] cannot measure or collect the data
 * needed for this assessment.
 */
interface PermissionStep : Step, ContentNode {
    val permissions: List<PermissionInfo>?
}

/**
 * An [OptionalStep] is a subclass of step where the step should be displayed if and only if the [fullInstructionsOnly]
 * flag has been set for displaying the full instructions. This is used to allow the assessment designer to show more
 * detailed instructions only to users who are not already familiar with the assessment rather than showing a full set
 * of instructions every time.
 */
interface OptionalStep : Step {

    /**
     * Should this step only be displayed when showing the full instruction sequence?
     */
    val fullInstructionsOnly: Boolean
}

/**
 * An [InstructionStep] is a UI [Step] that includes detailed text with instructions. By design, there is *only* one
 * text label in an instruction step with the intention that the amount of text will be short enough to be readable on
 * a single screen.
 */
interface InstructionStep : OptionalStep, ContentNode

/**
 * A [FormStep] is a container of other nodes where design of the form *requires* displaying all the components on a
 * single screen.
 *
 * For example, a [FormStep] may describe entering a participant's demographics data where the study designer wants to
 * display height, weight, gender, and birth year on a single screen.
 */
interface FormStep : Step, ContentNode {

    /**
     * The children contained within this collection.
     */
    val children: List<Node>

    /**
     * A form should always return a collection of results.
     */
    override fun createResult(): CollectionResult = CollectionResultObject(resultId())
}

/**
 * A result summary step is used to display a result that is calculated or measured earlier in the [Assessment].
 */
interface ResultSummaryStep : Step, ContentNode {

    /**
     * Text to display as the title above the result.
     */
    val resultTitle: String?

    /**
     * The localized unit to display for this result.
     */
    val unitText: String?

    /**
     * A link list that describes the path in an [AssessmentResult] down which to look for the result to use as the
     * answer to the result. If [null], then the application UI must define a custom presentation for showing the
     * result.
     */
    val scoringResultNodeIdentifier: NodeIdentifierPath?
}

/**
 * [ActiveStep] extends the [Step] to include a [duration] and [commands]. This is used for the case where a step has
 * an action such as "start walking", "tap the screen", or "get ready".
 */
interface ActiveStep : Step {

    /**
     * The duration of time to run the step. If `0`, then this value is ignored.
     */
    val duration: Double

    /**
     * The set of commands to apply to this active step. These indicate actions to fire at the beginning and end of
     * the step such as playing a sound as well as whether or not to automatically start and finish the step.
     */
    val commands: Set<ActiveUIStepCommand>

    /**
     * Whether or not the step uses audio, such as the speech synthesizer, that should play whether or not the user
     * has the mute switch turned on.
     */
    val requiresBackgroundAudio: Boolean

    /**
     * Should the assessment end early if the assessment is interrupted by a phone call?
     */
    val shouldEndOnInterrupt : Boolean
}

/**
 * A [CountdownStep] is a subtype of the [ActiveStep] that may only be displayed when showing the full instructions.
 * Typically, this type of step is shown using a label that displays a countdown to displaying the [ActiveStep] that
 * follows it.
 */
interface CountdownStep : OptionalStep, ActiveStep

/**
 * A set of commands that can be set on an [ActiveStep]. Typically, these deal with desired step behavior during
 * transitions to start, finish, interrupt, and pause.
 *
 * - [PlaySoundOnStart]: Play a default sound when the step starts.
 * - [PlaySoundOnFinish]: Play a default sound when the step finishes.
 *
 * - [VibrateOnStart]: Vibrate when the step starts.
 * - [VibrateOnFinish]: Vibrate when the step finishes.
 *
 * - [StartTimerAutomatically]: Start the count down timer automatically when the step start.
 * - [ContinueOnFinish]: Transition automatically when the step finishes.
 *
 * - [ShouldDisableIdleTimer]: Disable the idle timer if included.
 *
 * - [SpeakWarningOnPause]: Speak a warning when the pause button is tapped.
 */
enum class ActiveUIStepCommand : StringEnum {
    PlaySoundOnStart, PlaySoundOnFinish,
    VibrateOnStart, VibrateOnFinish,
    StartTimerAutomatically, ContinueOnFinish,
    ShouldDisableIdleTimer,
    SpeakWarningOnPause,
    ;

    companion object {
        private val mapping = values().map { it.name.decapitalize() to setOf(it) }.toMap().plus(mapOf(
            "playSound" to setOf(PlaySoundOnStart, PlaySoundOnFinish),
            "transitionAutomatically" to setOf(StartTimerAutomatically, ContinueOnFinish),
            "vibrate" to setOf(VibrateOnStart, VibrateOnFinish)
        ))

        fun fromStrings(strings: Set<String>) : Set<ActiveUIStepCommand>
                = strings.mapNotNull { mapping[it] }.flatten().toSet()
    }
}

/**
 * The [SpokenInstructionTiming] is a serializable class used to describe the timing of spoken instructions.
 */
@Serializable
sealed class SpokenInstructionTiming : StringEnum {

    /**
     * - [Start]: Speak the instruction at the start of the step.
     * - [Halfway]: Speak the instruction at the halfway point.
     * - [Countdown]: Speak a countdown.
     * - [End]: Speak the instruction at the end of the step.
     */
    sealed class Keyword(override val name: String) : SpokenInstructionTiming() {
        object Start : Keyword("start")
        object Halfway : Keyword("halfway")
        object Countdown : Keyword("countdown")
        object End : Keyword("end")
        companion object {
            fun values() = arrayOf(Start, Halfway, Countdown, End)
        }
    }

    /**
     * The [TimeInterval] denotes a time (in seconds) from when the step timer started until the instruction should
     * be spoken. For example, `10.0` should be spoken once when the timer fires after the 10th second.
     */
    data class TimeInterval(val time: Double) : SpokenInstructionTiming() {
        override val name: String
            get() = time.toString()
    }

    @Serializer(forClass = SpokenInstructionTiming::class)
    companion object : KSerializer<SpokenInstructionTiming> {
        override val descriptor: SerialDescriptor = PrimitiveDescriptor("SpokenInstructionTiming", PrimitiveKind.STRING)
        override fun deserialize(decoder: Decoder): SpokenInstructionTiming {
            val name = decoder.decodeString()
            return Keyword.values().matching(name) ?: TimeInterval(name.toDouble())
        }
        override fun serialize(encoder: Encoder, value: SpokenInstructionTiming) {
            encoder.encodeString(value.name)
        }
    }
}

