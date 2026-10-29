package com.touti.score.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ToutiTest {

    @Test
    fun winnerScoresTheBid() {
        val game = GameState(rounds = listOf(Round(Team.A, 70), Round(Team.B, 90), Round(Team.A, 100)))
        assertEquals(170, game.total(Team.A))
        assertEquals(90, game.total(Team.B))
    }

    @Test
    fun winnerIsFirstToTarget() {
        val base = GameState(rules = Rules(target = 300))
        assertNull(base.copy(rounds = listOf(Round(Team.A, 200), Round(Team.B, 230))).winner)
        assertEquals(Team.A, base.copy(rounds = listOf(Round(Team.A, 200), Round(Team.A, 100))).winner)
    }

    @Test
    fun dealerRotatesAroundTheTable() {
        val game = GameState(firstDealer = 3)
        assertEquals("Joueur 4", game.dealer)
        assertEquals("Joueur 1", game.copy(rounds = listOf(Round(Team.A, 70))).dealer)
    }

    @Test
    fun finishedGamesAreArchivedButUnfinishedOnesAreNot() {
        val unfinished = GameState(rounds = listOf(Round(Team.A, 70)))
        assertEquals(0, unfinished.archived(endedAt = 1L).history.size)

        val won = GameState(rules = Rules(target = 100), rounds = listOf(Round(Team.B, 70), Round(Team.B, 80), Round(Team.A, 90)))
        val played = won.archived(endedAt = 42L).history.single()
        assertEquals(Team.B, played.winner)
        assertEquals(90, played.scoreA)
        assertEquals(150, played.scoreB)
        assertEquals(3, played.rounds)
        assertEquals(42L, played.endedAt)
    }
}
