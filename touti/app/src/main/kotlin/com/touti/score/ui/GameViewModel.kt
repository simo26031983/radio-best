package com.touti.score.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.touti.score.data.GameStore
import com.touti.score.model.Announce
import com.touti.score.model.GameState
import com.touti.score.model.Round
import com.touti.score.model.Speech
import com.touti.score.model.Rules
import com.touti.score.model.Team
import com.touti.score.model.TeamInfo
import com.touti.score.speech.Announcer

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val store = GameStore(application)

    var state by mutableStateOf(store.load())
        private set

    /** True while the setup screen is shown on top of a running game. */
    var editingSetup by mutableStateOf(false)
        private set

    /** Null until the speech engine has started; false if the phone has no Arabic voice. */
    var arabicVoiceAvailable by mutableStateOf<Boolean?>(null)
        private set

    private val announcer = Announcer(application) { arabicVoiceAvailable = it }

    private fun update(transform: (GameState) -> GameState) {
        state = transform(state)
        store.save(state)
    }

    fun startOrSave(teamA: TeamInfo, teamB: TeamInfo, rules: Rules, firstDealer: Int) {
        update { it.copy(teamA = teamA, teamB = teamB, rules = rules, firstDealer = firstDealer, started = true) }
        editingSetup = false
    }

    fun editSetup() {
        editingSetup = true
    }

    fun cancelEditSetup() {
        editingSetup = false
    }

    fun addRound(round: Round) {
        update { it.copy(rounds = it.rounds + round) }
        if (!state.rules.muted) announcer.say(Speech.afterRound(state, round))
    }

    fun toggleMuted() = update { it.copy(rules = it.rules.copy(muted = !it.rules.muted)) }

    fun testVoice(teamA: TeamInfo, teamB: TeamInfo, announce: Announce) =
        announcer.say(Speech.sample(teamA, teamB, announce))

    override fun onCleared() {
        announcer.shutdown()
    }

    fun removeRound(index: Int) = update { it.copy(rounds = it.rounds.filterIndexed { i, _ -> i != index }) }

    fun undoLastRound() = update { it.copy(rounds = it.rounds.dropLast(1)) }

    /** Banks the win for the current winner, then deals a fresh game with the same teams. */
    fun rematch() = update {
        it.copy(
            rounds = emptyList(),
            winsA = it.winsA + if (it.winner == Team.A) 1 else 0,
            winsB = it.winsB + if (it.winner == Team.B) 1 else 0,
            // The deal keeps rotating from where the last game stopped.
            firstDealer = (it.firstDealer + it.rounds.size) % 4,
        )
    }

    /** Back to the setup screen with the same names, scores and win tally cleared. */
    fun newGame() {
        update { it.copy(rounds = emptyList(), winsA = 0, winsB = 0, started = false) }
        editingSetup = false
    }
}
