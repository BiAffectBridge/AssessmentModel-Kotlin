package org.sagebionetworks.assessmentmodel.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import org.koin.android.ext.android.bind
import org.sagebionetworks.assessmentmodel.Step
import org.sagebionetworks.assessmentmodel.presentation.compose.QuestionContent
import org.sagebionetworks.assessmentmodel.presentation.databinding.ChoiceQuestionStepFragmentBinding
import org.sagebionetworks.assessmentmodel.survey.ChoiceQuestion
import org.sagebionetworks.assessmentmodel.survey.QuestionState

class ChoiceQuestionStepFragment: StepFragment() {

    private var _binding: ChoiceQuestionStepFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    val binding get() = _binding!!

    lateinit var questionStep: ChoiceQuestion
    lateinit var questionState: QuestionState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionState = nodeState as QuestionState
        questionStep = questionState.node as ChoiceQuestion
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = ChoiceQuestionStepFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.navBar.setForwardOnClickListener { assessmentViewModel.goForward() }
        binding.navBar.setBackwardOnClickListener { assessmentViewModel.goBackward() }
        binding.navBar.setSkipOnClickListener { assessmentViewModel.goForward() }
        binding.navBar.setup(questionStep as Step)
        binding.questionContent.setContent {
            //TODO: Need to figure out theming with compose -nbrown 2/17/22
            //AssesmentModelKotlinNativeas36Theme {
                // A surface container using the 'background' color from the theme
                //Surface(color = MaterialTheme.colors.background) {
                    QuestionContent(questionState = questionState)
                //}
            //}
        }
    }
}