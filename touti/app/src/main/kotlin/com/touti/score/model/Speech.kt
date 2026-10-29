package com.touti.score.model

enum class Lang {
    FR,

    /** Moroccan darija, written in Arabic script for the phone's Arabic voice. */
    DARIJA,
}

data class Line(val lang: Lang, val text: String)

/** What gets read out after a round: the round's value, then the current score. */
object Speech {

    fun afterRound(game: GameState, round: Round): List<Line> {
        val winner = game.winner
        return game.rules.announce.langs().map { lang ->
            when {
                winner != null -> Line(lang, gameWon(game, winner, lang))
                else -> Line(lang, roundWon(game, round, lang))
            }
        }
    }

    /** A made-up first round of 70 for the given teams, to try the voices from the setup screen. */
    fun sample(teamA: TeamInfo, teamB: TeamInfo, announce: Announce): List<Line> {
        val round = Round(Team.A, 70)
        val game = GameState(teamA = teamA, teamB = teamB, rules = Rules(announce = announce), rounds = listOf(round))
        return afterRound(game, round)
    }

    private fun roundWon(game: GameState, round: Round, lang: Lang): String {
        val a = game.total(Team.A)
        val b = game.total(Team.B)
        return when (lang) {
            Lang.FR ->
                "Plus ${round.bid} pour ${game.team(round.winner).name}. " +
                    "Score : ${game.teamA.name} $a, ${game.teamB.name} $b."
            Lang.DARIJA ->
                "${arabicName(game, round.winner)} داو ${Darija.number(round.bid)}. " +
                    "دابا: ${arabicName(game, Team.A)} ${Darija.number(a)}، ${arabicName(game, Team.B)} ${Darija.number(b)}."
        }
    }

    private fun gameWon(game: GameState, winner: Team, lang: Lang): String {
        val w = game.total(winner)
        val l = game.total(winner.other)
        return when (lang) {
            Lang.FR -> "Mabrouk ! Victoire pour ${game.team(winner).name}, $w à $l !"
            Lang.DARIJA -> "مبروك! ${arabicName(game, winner)} ربحو الطرح، ${Darija.number(w)} على ${Darija.number(l)}!"
        }
    }

    /**
     * Arabic voices mangle Latin names, so a team named in Latin letters is called by its colour
     * on the scoreboard instead. A team named in Arabic keeps its name.
     */
    fun arabicName(game: GameState, team: Team): String {
        val name = game.team(team).name
        return when {
            name.any { it in '؀'..'ۿ' } -> name
            team == Team.A -> "الفرقة الخضرا"
            else -> "الفرقة الحمرا"
        }
    }

    private fun Announce.langs(): List<Lang> = when (this) {
        Announce.BOTH -> listOf(Lang.FR, Lang.DARIJA)
        Announce.FRENCH -> listOf(Lang.FR)
        Announce.ARABIC -> listOf(Lang.DARIJA)
    }
}
