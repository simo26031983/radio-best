package com.touti.score.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable

@Composable
fun ToutiApp(viewModel: GameViewModel) {
    val state = viewModel.state
    val showSetup = !state.started || viewModel.editingSetup

    AnimatedContent(
        targetState = showSetup,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { setup ->
        if (setup) {
            if (viewModel.editingSetup) BackHandler { viewModel.cancelEditSetup() }
            SetupScreen(
                initial = state,
                editing = viewModel.editingSetup,
                onCancel = viewModel::cancelEditSetup,
                onConfirm = viewModel::startOrSave,
            )
        } else {
            GameScreen(viewModel)
        }
    }
}
