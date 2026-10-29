package com.touti.score.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTest {

    @Test
    fun readsRoundValueThenScoreInFrenchThenDarija() {
        val round = Round(Team.A, 70)
        val game = GameState(rounds = listOf(Round(Team.B, 90), round))
        val lines = Speech.afterRound(game, round)
        assertEquals(
            listOf(
                Line(Lang.FR, "Plus 70 pour Les Lions. Score : Les Lions 70, Les Aigles 90."),
                Line(Lang.DARIJA, "الفرقة الخضرا داو سبعين. دابا: الفرقة الخضرا سبعين، الفرقة الحمرا تسعين."),
            ),
            lines,
        )
    }

    @Test
    fun announcesTheWinnerWhenTheGameEnds() {
        val round = Round(Team.B, 100)
        val game = GameState(rules = Rules(target = 300, announce = Announce.FRENCH), rounds = listOf(Round(Team.B, 230), round))
        assertEquals(listOf(Line(Lang.FR, "Mabrouk ! Victoire pour Les Aigles, 330 à 0 !")), Speech.afterRound(game, round))
    }

    @Test
    fun arabicTeamNamesAreKept() {
        val game = GameState(teamA = TeamInfo("السباع", listOf("a", "b")))
        assertEquals("السباع", Speech.arabicName(game, Team.A))
        assertEquals("الفرقة الحمرا", Speech.arabicName(game, Team.B))
    }

    @Test
    fun spellsNumbersInDarija() {
        assertEquals("سبعين", Darija.number(70))
        assertEquals("مية", Darija.number(100))
        assertEquals("مية و عشرة", Darija.number(110))
        assertEquals("مية و سبعين", Darija.number(170))
        assertEquals("ميتين و تلاتين", Darija.number(230))
        assertEquals("ست مية و عشرة", Darija.number(610))
        assertEquals("ألف و ميتين", Darija.number(1200))
        assertEquals("خمسة و عشرين", Darija.number(25))
        assertEquals("صفر", Darija.number(0))
    }
}
