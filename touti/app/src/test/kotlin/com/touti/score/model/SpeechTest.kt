package com.touti.score.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechTest {

    @Test
    fun readsRoundValueThenScoreInFrenchThenArabic() {
        val round = Round(Team.A, 70)
        val game = GameState(rounds = listOf(Round(Team.B, 90), round))
        val lines = Speech.afterRound(game, round)
        assertEquals(
            listOf(
                Line(Lang.FR, "Plus 70 pour Les Lions. Score : Les Lions 70, Les Aigles 90."),
                Line(Lang.AR, "الفريق الأخضر، زائد 70. النتيجة: الفريق الأخضر 70، الفريق الأحمر 90."),
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
        assertEquals("الفريق الأحمر", Speech.arabicName(game, Team.B))
    }
}
