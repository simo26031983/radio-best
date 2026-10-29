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
    val screen = when {
        viewModel.showingHistory -> Screen.HISTORY
        !state.started || viewModel.editingSetup -> Screen.SETUP
        else -> Screen.GAME
    }

    AnimatedContent(
        targetState = screen,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen",
    ) { target ->
        when (target) {
            Screen.HISTORY -> {
                BackHandler { viewModel.closeHistory() }
                HistoryScreen(
                    history = state.history,
                    onBack = viewModel::closeHistory,
                    onDelete = viewModel::deleteGame,
                    onClear = viewModel::clearHistory,
                )
            }
            Screen.SETUP -> {
                if (viewModel.editingSetup) BackHandler { viewModel.cancelEditSetup() }
                SetupScreen(
                    initial = state,
                    editing = viewModel.editingSetup,
                    onCancel = viewModel::cancelEditSetup,
                    arabicVoiceAvailable = viewModel.arabicVoiceAvailable,
                    onTestVoice = viewModel::testVoice,
                    onOpenHistory = viewModel::openHistory,
                    onConfirm = viewModel::startOrSave,
                )
            }
            Screen.GAME -> GameScreen(viewModel)
        }
    }
}

private enum class Screen { SETUP, GAME, HISTORY }
