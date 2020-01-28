
package org.sagebionetworks.assessmentmodel.serialization

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.sagebionetworks.assessmentmodel.*

// TODO: syoung 01/27/2020 Uncomment these one at a time once tests are created.

//@Serializable
//abstract class StepNodeObject(override val identifier: String,
//                              override val resultIdentifier: String?,
//                              override val comment: String?,
//                              override val title: String?,
//                              override val label: String?,
//                              @Polymorphic
//                              @SerialName("image")
//                              override val imageInfo: ImageInfo?,
//                              override val detail: String?,
//                              override val footnote: String?,
//                              override val hideButtons: List<ButtonAction>,
//                              override val buttonMap: Map<ButtonAction, Button>): Node, Step




//
//@Serializable
//@SerialName("assessment")
//data class AssessmentObject(override val identifier: String,
//                            override val resultIdentifier: String? = null,
//                            override val versionString: String? = null,
//                            @SerialName("steps")
//                            override val children: List<Node>,
//                            override val comment: String? = null,
//                            override val title: String? = null,
//                            override val label: String? = null,
//                            @Serializable(with=ImageNameSerializer::class)
//                            override val imageInfo: FetchableImage? = null,
//                            override val detail: String? = null,
//                            @SerialName("shouldHideActions")
//                            @Serializable(with=ButtonActionTypeSerializer::class)
//                            override val hideButtons: List<ButtonActionType> = listOf(),
//                            @SerialName("actions")
//                            override val buttonMap: Map<String, Button> = mapOf(),
//                            @SerialName("asyncActions")
//                            override val backgroundActions: List<AsyncActionConfiguration> = listOf()) : Assessment, NodeNavigator {
//    override fun createResult(): Result
//            = AssessmentResultObject(resultIdentifier ?: identifier, versionString)
//
//    override val navigator: Navigator
//        get() = this
//}
//
//@Serializable
//@SerialName("section")
//data class SectionObject(override val identifier: String,
//                         override val resultIdentifier: String? = null,
//                         @SerialName("steps")
//                         override val children: List<Node>,
//                         override val comment: String? = null,
//                         override val title: String? = null,
//                         override val label: String? = null,
//                         @SerialName("icon")
//                         @Serializable(with=ImageNameSerializer::class)
//                         override val imageInfo: FetchableImage? = null,
//                         override val detail: String? = null,
//                         @SerialName("shouldHideActions")
//                         @Serializable(with=ButtonActionTypeSerializer::class)
//                         override val hideButtons: List<ButtonActionType> = listOf(),
//                         @SerialName("actions")
//                         override val buttonMap: Map<String, Button> = mapOf(),
//                         @SerialName("asyncActions")
//                         override val backgroundActions: List<AsyncActionConfiguration> = listOf()) : Section, AsyncActionContainer {
//    override fun createResult(): Result {
//        return CollectionResultObject(identifier = identifier)
//    }
//}
//
//
//@Serializable
//abstract class FormObject(override val identifier: String,
//                         override val resultIdentifier: String? = null,
//                         @SerialName("inputFields")
//                         override val children: List<Node>,
//                         override val comment: String? = null,
//                         override val title: String? = null,
//                         override val label: String? = null,
//                         @SerialName("image")
//                         override val imageInfo: ImageInfo? = null,
//                         override val detail: String? = null,
//                         @SerialName("shouldHideActions")
//                         @Serializable(with=ButtonActionTypeSerializer::class)
//                         override val hideButtons: List<ButtonActionType> = listOf(),
//                         @SerialName("actions")
//                         override val buttonMap: Map<String, Button> = mapOf(),
//                         @SerialName("asyncActions")
//                         override val backgroundActions: List<AsyncActionConfiguration> = listOf()) : Form, AsyncActionContainer {
//    override fun createResult(): Result {
//        return CollectionResultObject(identifier = identifier)
//    }
//}
