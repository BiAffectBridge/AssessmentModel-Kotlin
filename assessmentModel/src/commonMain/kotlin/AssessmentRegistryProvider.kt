package org.sagebionetworks.assessmentmodel

import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.ModuleInfoObject
import org.sagebionetworks.assessmentmodel.serialization.moduleInfoSerializersModule

/**
 * The [AssessmentRegistryProvider] is a configuration tool for providing the mapping to allow
 * an application to look for assessments where the Json serialization, resources, etc. are in
 * different modules. This should be set up and defined at the app level and include a pointer
 * for each module used to load assessments.
 *
 * [ModuleInfoObject] provides a default implementation for a serializers module. Currently, this
 * is used to wrap the serialization on iOS. The use of this is experimental, but you could add to
 * the [moduleInfoSerializersModule] a different decoding for each module and use the "type"
 * to define the coder for the module. syoung 01/27/2021
 */
interface AssessmentRegistryProvider {

    /**
     * The [FileLoader] to use for loading JSON from an embedded resource.
     */
    val fileLoader: FileLoader

    /**
     * A list of the [ModuleInfo] objects included in this registry.
     */
    val modules: List<ModuleInfo>

    /**
     * Load the [Assessment] from the given [AssessmentPlaceholder].
     */
    fun loadAssessment(assessmentPlaceholder: AssessmentPlaceholder): Assessment {
        val moduleInfo = modules.find { it.hasAssessment(assessmentPlaceholder) }
        return moduleInfo?.getAssessment(assessmentPlaceholder, this)
            ?: throw IllegalStateException("This version of the application cannot load " + {assessmentPlaceholder.assessmentInfo.identifier})
    }
}

