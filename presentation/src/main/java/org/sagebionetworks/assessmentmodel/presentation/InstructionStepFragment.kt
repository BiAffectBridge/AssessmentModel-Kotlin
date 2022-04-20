package org.sagebionetworks.assessmentmodel.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import org.sagebionetworks.assessmentmodel.ContentNodeStep
import org.sagebionetworks.assessmentmodel.presentation.compose.InstructionStepUi
import org.sagebionetworks.assessmentmodel.presentation.databinding.ComposeQuestionStepFragmentBinding
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.SageSurveyTheme
import org.sagebionetworks.assessmentmodel.serialization.SageResourceImage
import org.sagebionetworks.assessmentmodel.serialization.loadDrawable

open class InstructionStepFragment: StepFragment() {

    private var _binding: ComposeQuestionStepFragmentBinding? = null
    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var step: ContentNodeStep

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        step = nodeState.node as ContentNodeStep
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = ComposeQuestionStepFragmentBinding.inflate(layoutInflater, container, false)
        val drawable = step.imageInfo?.loadDrawable(requireContext())
        val tint = (step.imageInfo as? SageResourceImage)?.name?.tint ?: false
        binding.questionContent.setContent {
            //TODO: Need to figure out theming with compose -nbrown 2/17/22
            SageSurveyTheme {
                InstructionStepUi(
                    icon = drawable,
                    iconTintColor = if (tint) {
                        MaterialTheme.colors.primary
                    } else {
                        null
                    },
                    title = step.title,
                    detail = step.detail,
                    next = { assessmentViewModel.goForward() },
                    close = { assessmentViewModel.cancel() }
                )
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        binding.title.text = step.title
//        binding.detail.text = step.detail
//        val drawable = step.imageInfo?.loadDrawable(requireContext())
//        binding.header.image.setImageDrawable(drawable)
//        if (drawable is AnimationDrawable) {
//            drawable.start()
//        }
//        binding.navBar.setForwardOnClickListener { assessmentViewModel.goForward() }
//        binding.navBar.setBackwardOnClickListener { assessmentViewModel.goBackward() }
//        binding.navBar.setup(step)
//        binding.header.closeBtn.setOnClickListener{ assessmentViewModel.cancel() }
//        binding.navBar.binding.skipButton.visibility = View.INVISIBLE
//    }

}