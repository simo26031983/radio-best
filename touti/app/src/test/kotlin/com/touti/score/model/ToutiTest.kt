package com.touti.score.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToutiTest {

    @Test
    fun madeContractScoresCardsAndAnnouncesForBothTeams() {
        val round = Touti.scoreDeal(Team.A, bid = 90, takerCardPoints = 80, announceA = 20, announceB = 0, rules = Rules())
        assertTrue(round.made)
        assertEquals(100, round.pointsA)
        assertEquals(50, round.pointsB)
    }

    @Test
    fun announcesCountTowardTheContract() {
        val round = Touti.scoreDeal(Team.B, bid = 100, takerCardPoints = 60, announceA = 0, announceB = 40, rules = Rules())
        assertTrue(round.made)
        assertEquals(100, round.pointsB)
        assertEquals(70, round.pointsA)
    }

    @Test
    fun failedContractGivesEverythingToDefenseByDefault() {
        val round = Touti.scoreDeal(Team.A, bid = 100, takerCardPoints = 75, announceA = 20, announceB = 20, rules = Rules())
        assertFalse(round.made)
        assertEquals(0, round.pointsA)
        assertEquals(170, round.pointsB)
    }

    @Test
    fun failedContractWithKeepOwnRule() {
        val rules = Rules(failRule = FailRule.DEFENSE_KEEPS_OWN)
        val round = Touti.scoreDeal(Team.A, bid = 100, takerCardPoints = 75, announceA = 20, announceB = 20, rules = rules)
        assertEquals(0, round.pointsA)
        assertEquals(75, round.pointsB)
    }

    @Test
    fun bidScoringGivesExactlyTheBid() {
        val rules = Rules(takerScoring = TakerScoring.BID)
        val round = Touti.scoreDeal(Team.B, bid = 80, takerCardPoints = 110, announceA = 0, announceB = 0, rules = rules)
        assertEquals(80, round.pointsB)
        assertEquals(20, round.pointsA)
    }

    @Test
    fun winnerIsFirstToTargetAndTiesKeepPlaying() {
        val base = GameState(rules = Rules(target = 300))
        assertNull(base.copy(rounds = listOf(Touti.freeEntry(299, 0))).winner)
        assertEquals(Team.A, base.copy(rounds = listOf(Touti.freeEntry(300, 10))).winner)
        assertEquals(Team.B, base.copy(rounds = listOf(Touti.freeEntry(310, 320))).winner)
        assertNull(base.copy(rounds = listOf(Touti.freeEntry(310, 310))).winner)
    }

    @Test
    fun dealerRotatesAroundTheTable() {
        val game = GameState(firstDealer = 3)
        assertEquals("Joueur 4", game.dealer)
        assertEquals("Joueur 1", game.copy(rounds = listOf(Touti.freeEntry(0, 0))).dealer)
    }
}
