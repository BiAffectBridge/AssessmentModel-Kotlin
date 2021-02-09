package org.sagebionetworks.assessmentmodel.presentation

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.navigation.*

open class RootAssessmentViewModel(
    val assessmentPlaceholder: AssessmentPlaceholder,
    val registryProvider: AssessmentRegistryProvider,
    val nodeStateProvider: CustomNodeStateProvider? = null
) : ViewModel(), RootNodeController {

    var hasHandledLoad = false
    var assessmentNodeState: BranchNodeState? = null
    protected val assessmentLoadedMutableLiveData: MutableLiveData<BranchNodeState> = MutableLiveData()
    val assessmentLoadedLiveData: LiveData<BranchNodeState> = assessmentLoadedMutableLiveData

    protected val assessmentFinishedMutableLiveData: MutableLiveData<FinishedState> = MutableLiveData()
    val assessmentFinishedLiveData: LiveData<FinishedState> = assessmentFinishedMutableLiveData

    init {
        viewModelScope.launch {
            val assessment = registryProvider.loadAssessment(assessmentPlaceholder)
            assessmentNodeState = nodeStateProvider?.customBranchNodeStateFor(assessment!!, null)?: BranchNodeStateImpl(assessment!!, null)
            assessmentNodeState?.customNodeStateProvider = nodeStateProvider
            assessmentLoadedMutableLiveData.value = assessmentNodeState
        }
    }

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
        val resultString = nodeState.currentResult.toString()
        Log.d("Save Result", resultString)
        // syoung 11/25/2020 In an application, this is the callback for uploading the results.
    }

    override fun handleFinished(reason: FinishedReason, nodeState: NodeState) {
        val resultString = nodeState.currentResult.toString()
        Log.d("Result", resultString)

        //TODO: Before triggering UI to finish, should also check that any clean up is done - nbrown 01/21/21

        assessmentFinishedMutableLiveData.value =
            FinishedState(nodeState, reason)

    }

    /**
     * Data class for LiveData stream.
     */
    data class FinishedState(
        val nodeState: NodeState,
        val finishedReason: FinishedReason
    )

}


/**
 * Factory for the RootAssesmentViewModel.
 *
 * Providing ViewModelProvider.Factory allows us to inject dependencies and pass parameters
 * to an instance since the Android framework controls the instantiation of ViewModels.
 */
open class RootAssessmentViewModelFactory() {

    open fun create(
        assessmentInfo: AssessmentPlaceholder,
        assessmentProvider: AssessmentRegistryProvider,
        nodeStateProvider: CustomNodeStateProvider?
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(RootAssessmentViewModel::class.java)) {

                    @Suppress("UNCHECKED_CAST")
                    return RootAssessmentViewModel(assessmentInfo, assessmentProvider, nodeStateProvider) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
