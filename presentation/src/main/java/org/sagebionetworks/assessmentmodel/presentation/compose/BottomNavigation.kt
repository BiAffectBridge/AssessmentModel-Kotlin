package org.sagebionetworks.assessmentmodel.presentation.compose

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.SageSurveyTheme

@Composable
fun BottomNavigation(
    onBackClicked: () -> Unit,
    onNextClicked: () -> Unit,
    nextText: String? = null,
    backEnabled: Boolean = true,
    backVisible: Boolean = true,
    nextEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .padding(horizontal = 20.dp)
            .fillMaxWidth()) {
        if (backVisible) {
            WhiteBackButton(onClick = onBackClicked, enabled = backEnabled)
        }
        Spacer(modifier = Modifier.weight(1f))
        if (nextText != null) {
            BlackButton(onClick = onNextClicked, enabled = nextEnabled, text = nextText)
        } else {
            BlackNextButton(onClick = onNextClicked, enabled = nextEnabled)
        }
    }
}

@Preview
@Composable
private fun BottomNavPreview() {
    SageSurveyTheme {
        Column() {
            BottomNavigation({}, {})
            BottomNavigation({}, {}, "Submit",false, false)
        }
    }
}