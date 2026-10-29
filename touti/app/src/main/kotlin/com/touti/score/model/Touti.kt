package com.touti.score.model

import kotlinx.serialization.Serializable

@Serializable
enum class Team {
    A, B;

    val other: Team get() = if (this == A) B else A
}

/** Which languages the score is read out in after each round. */
@Serializable
enum class Announce { BOTH, FRENCH, ARABIC }

@Serializable
data class Rules(
    val target: Int = 600,
    val announce: Announce = Announce.BOTH,
    val muted: Boolean = false,
)

/** One deal: the team that won it scores exactly the bid, the other team scores nothing. */
@Serializable
data class Round(
    val winner: Team,
    val bid: Int,
) {
    fun points(team: Team): Int = if (team == winner) bid else 0
}

object Touti {
    const val MIN_BID = 70
    const val BID_STEP = 10
    const val MAX_BID = 230

    val bids: List<Int> = (MIN_BID..MAX_BID step BID_STEP).toList()
}

@Serializable
data class TeamInfo(
    val name: String,
    val players: List<String>,
)

/** A finished game, kept in the history. Names are copied so renaming teams later doesn't rewrite it. */
@Serializable
data class PlayedGame(
    val endedAt: Long,
    val teamA: TeamInfo,
    val teamB: TeamInfo,
    val scoreA: Int,
    val scoreB: Int,
    val winner: Team,
    val rounds: Int,
    val target: Int,
) {
    fun team(team: Team): TeamInfo = if (team == Team.A) teamA else teamB

    fun score(team: Team): Int = if (team == Team.A) scoreA else scoreB
}

@Serializable
data class GameState(
    val teamA: TeamInfo = TeamInfo("Les Lions", listOf("Joueur 1", "Joueur 3")),
    val teamB: TeamInfo = TeamInfo("Les Aigles", listOf("Joueur 2", "Joueur 4")),
    val rules: Rules = Rules(),
    val rounds: List<Round> = emptyList(),
    /** Seat (0..3) of whoever deals the first round. */
    val firstDealer: Int = 0,
    val winsA: Int = 0,
    val winsB: Int = 0,
    val started: Boolean = false,
    /** Finished games, oldest first. */
    val history: List<PlayedGame> = emptyList(),
) {
    fun team(team: Team): TeamInfo = if (team == Team.A) teamA else teamB

    fun total(team: Team): Int = rounds.sumOf { it.points(team) }

    /** Players around the table, alternating teams: A1, B1, A2, B2. */
    val seats: List<String>
        get() = listOf(teamA.players[0], teamB.players[0], teamA.players[1], teamB.players[1])

    /** The deal passes to the next seat after every round. */
    val dealer: String get() = seats[(firstDealer + rounds.size) % seats.size]

    /** First team to reach the target wins; if both do on the same deal, the higher total wins. */
    val winner: Team?
        get() {
            val a = total(Team.A)
            val b = total(Team.B)
            return when {
                a < rules.target && b < rules.target -> null
                a > b -> Team.A
                b > a -> Team.B
                else -> null
            }
        }

    val leader: Team?
        get() = when {
            total(Team.A) > total(Team.B) -> Team.A
            total(Team.B) > total(Team.A) -> Team.B
            else -> null
        }

    fun wins(team: Team): Int = if (team == Team.A) winsA else winsB

    /** The current game as a history entry, or null while nobody has won yet. */
    fun toPlayedGame(endedAt: Long): PlayedGame? {
        val winner = winner ?: return null
        return PlayedGame(endedAt, teamA, teamB, total(Team.A), total(Team.B), winner, rounds.size, rules.target)
    }

    /** Appends the current game to the history if it has a winner. */
    fun archived(endedAt: Long): GameState {
        val played = toPlayedGame(endedAt) ?: return this
        return copy(history = (history + played).takeLast(MAX_HISTORY))
    }

    private companion object {
        const val MAX_HISTORY = 500
    }
}
