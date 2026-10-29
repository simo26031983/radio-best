package com.touti.score.model

import kotlinx.serialization.Serializable

@Serializable
enum class Team {
    A, B;

    val other: Team get() = if (this == A) B else A
}

/** What the bidding team scores when it makes its contract. */
@Serializable
enum class TakerScoring {
    /** Its card points plus its announcements. */
    POINTS,

    /** Exactly the amount it bid. */
    BID,
}

/** What happens when the bidding team fails its contract ("chute"). */
@Serializable
enum class FailRule {
    /** Defenders take the whole deal: 130 plus every announcement. */
    DEFENSE_TAKES_ALL,

    /** Defenders keep only their own card points and announcements. */
    DEFENSE_KEEPS_OWN,
}

@Serializable
data class Rules(
    val target: Int = 600,
    val takerScoring: TakerScoring = TakerScoring.POINTS,
    val failRule: FailRule = FailRule.DEFENSE_TAKES_ALL,
)

/**
 * One deal. [taker] is null for a free entry, where [pointsA]/[pointsB] were typed directly.
 * Points are stored as scored at the time, so changing the rules mid-game never rewrites history.
 */
@Serializable
data class Round(
    val taker: Team? = null,
    val bid: Int = 0,
    val takerCardPoints: Int = 0,
    val announceA: Int = 0,
    val announceB: Int = 0,
    val made: Boolean = true,
    val pointsA: Int,
    val pointsB: Int,
) {
    fun points(team: Team): Int = if (team == Team.A) pointsA else pointsB
}

object Touti {
    /** 120 in card points + 10 for the last trick. */
    const val DEAL_POINTS = 130
    const val MIN_BID = 70
    const val BID_STEP = 10

    /** 130 + the theoretical 100 of announcements. */
    const val MAX_BID = 230
    const val MAX_ANNOUNCES = 100
    const val ANNOUNCE = 20
    const val TRUMP_ANNOUNCE = 40

    val bids: List<Int> = (MIN_BID..MAX_BID step BID_STEP).toList()

    fun scoreDeal(
        taker: Team,
        bid: Int,
        takerCardPoints: Int,
        announceA: Int,
        announceB: Int,
        rules: Rules,
    ): Round {
        val cards = takerCardPoints.coerceIn(0, DEAL_POINTS)
        val takerAnnounce = if (taker == Team.A) announceA else announceB
        val defenseAnnounce = if (taker == Team.A) announceB else announceA
        val defenseCards = DEAL_POINTS - cards
        val made = cards + takerAnnounce >= bid

        val takerPoints: Int
        val defensePoints: Int
        if (made) {
            takerPoints = when (rules.takerScoring) {
                TakerScoring.POINTS -> cards + takerAnnounce
                TakerScoring.BID -> bid
            }
            defensePoints = defenseCards + defenseAnnounce
        } else {
            takerPoints = 0
            defensePoints = when (rules.failRule) {
                FailRule.DEFENSE_TAKES_ALL -> DEAL_POINTS + announceA + announceB
                FailRule.DEFENSE_KEEPS_OWN -> defenseCards + defenseAnnounce
            }
        }
        return Round(
            taker = taker,
            bid = bid,
            takerCardPoints = cards,
            announceA = announceA,
            announceB = announceB,
            made = made,
            pointsA = if (taker == Team.A) takerPoints else defensePoints,
            pointsB = if (taker == Team.B) takerPoints else defensePoints,
        )
    }

    fun freeEntry(pointsA: Int, pointsB: Int): Round =
        Round(pointsA = pointsA.coerceAtLeast(0), pointsB = pointsB.coerceAtLeast(0))
}

@Serializable
data class TeamInfo(
    val name: String,
    val players: List<String>,
)

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
}
