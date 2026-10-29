package com.touti.score.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touti.score.model.PlayedGame
import com.touti.score.model.Team
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DateFormat = DateTimeFormatter.ofPattern("EEE d MMM yyyy 'à' HH:mm", Locale.FRENCH)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    history: List<PlayedGame>,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onClear: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<PlayedGame?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📜 Historique", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        TextButton(onClick = { confirmClear = true }) {
                            Text("Tout effacer", color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("🏆", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Aucune partie terminée pour l'instant.\nLes parties gagnées apparaîtront ici.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Leaderboard(history) }
            items(history.asReversed(), key = { it.endedAt }) { game ->
                GameCard(
                    game,
                    Modifier
                        .animateItem()
                        .combinedClickable(onClick = {}, onLongClick = { toDelete = game }),
                )
            }
            item {
                Text(
                    "Appui long sur une partie pour la supprimer",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Effacer tout l'historique ?") },
            text = { Text("Les ${history.size} parties enregistrées seront supprimées.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    onClear()
                }) { Text("Effacer") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Annuler") } },
        )
    }

    toDelete?.let { game ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Supprimer cette partie ?") },
            text = { Text("${game.teamA.name} ${game.scoreA} – ${game.scoreB} ${game.teamB.name}") },
            confirmButton = {
                TextButton(onClick = {
                    toDelete = null
                    onDelete(game.endedAt)
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Garder") } },
        )
    }
}

/** Wins per team name across every recorded game, best first. */
@Composable
private fun Leaderboard(history: List<PlayedGame>) {
    val wins = history
        .groupingBy { it.team(it.winner).name }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "${history.size} partie${if (history.size > 1) "s" else ""} jouée${if (history.size > 1) "s" else ""}",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
            wins.forEachIndexed { index, (name, count) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(listOf("🥇", "🥈", "🥉").getOrElse(index) { "🏅" }, fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(name, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$count victoire${if (count > 1) "s" else ""}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GameCard(game: PlayedGame, modifier: Modifier) {
    val date = Instant.ofEpochMilli(game.endedAt).atZone(ZoneId.systemDefault()).format(DateFormat)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                date.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Team.entries.forEach { team ->
                val won = team == game.winner
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            (if (won) "🏆 " else "") + game.team(team).name,
                            color = teamColor(team),
                            fontWeight = if (won) FontWeight.Black else FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            game.team(team).players.joinToString(" & "),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Text(
                        "${game.score(team)}",
                        fontSize = 26.sp,
                        fontWeight = if (won) FontWeight.Black else FontWeight.Normal,
                        color = teamColor(team),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${game.rounds} manches · partie en ${game.target}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
