package org.sagebionetworks.assessmentmodel.serialization

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.survey.AnswerType

val resultSerializersModule = SerializersModule {
    polymorphic(Result::class) {
        AnswerResultObject::class with AnswerResultObject.serializer()
        AssessmentResultObject::class with AssessmentResultObject.serializer()
        BranchNodeResultObject::class with BranchNodeResultObject.serializer()
        CollectionResultObject::class with CollectionResultObject.serializer()
        ResultObject::class with ResultObject.serializer()
    }
}

@Serializable
@SerialName("answer")
data class AnswerResultObject(override val identifier: String,
                              override var answerType: AnswerType? = null,
                              @SerialName("value")
                              override var jsonValue: JsonElement? = null) : AnswerResult

@Serializable
@SerialName("assessment")
data class AssessmentResultObject(override val identifier: String,
                                  override val versionString: String? = null,
                                  @SerialName("stepHistory")
                                  override var pathHistoryResults: MutableList<Result> = mutableListOf(),
                                  @SerialName("asyncResults")
                                  override var inputResults: MutableSet<Result> = mutableSetOf(),
                                  @SerialName("taskRunUUID")
                                  override var runUUIDString: String = UUIDGenerator.uuidString(),
                                  @SerialName("startDate")
                                  override var startDateString: String = DateGenerator.nowString(),
                                  @SerialName("endDate")
                                  override var endDateString: String = DateGenerator.nowString()) : AssessmentResult

@Serializable
@SerialName("base")
data class ResultObject(override val identifier: String) : Result

@Serializable
@SerialName("collection")
data class CollectionResultObject(override val identifier: String,
                                  override var inputResults: MutableSet<Result> = mutableSetOf()) : CollectionResult

@Serializable
@SerialName("task")
data class BranchNodeResultObject(override val identifier: String,
                                  @SerialName("stepHistory")
                                  override var pathHistoryResults: MutableList<Result> = mutableListOf(),
                                  @SerialName("asyncResults")
                                  override var inputResults: MutableSet<Result> = mutableSetOf()) : BranchNodeResult


