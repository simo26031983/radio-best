package com.touti.score.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touti.score.model.GameState
import com.touti.score.model.Round
import com.touti.score.model.Team
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val game = viewModel.state
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmNewGame by remember { mutableStateOf(false) }
    var roundToDelete by remember { mutableStateOf<Int?>(null) }
    val winner = game.winner

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🃏 Touti ⭐", fontWeight = FontWeight.Black) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    actions = {
                        TextButton(onClick = { viewModel.toggleMuted() }) {
                            Text(if (game.rules.muted) "🔇" else "🔊", fontSize = 20.sp)
                        }
                        if (game.rounds.isNotEmpty()) {
                            TextButton(onClick = { viewModel.undoLastRound() }) {
                                Text("↩ Annuler", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("✏️ Équipes et règles") },
                                    onClick = {
                                        menuOpen = false
                                        viewModel.editSetup()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("📜 Historique des parties") },
                                    onClick = {
                                        menuOpen = false
                                        viewModel.openHistory()
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("🔄 Nouvelle partie") },
                                    onClick = {
                                        menuOpen = false
                                        confirmNewGame = true
                                    },
                                )
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            floatingActionButton = {
                if (winner == null) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSheet = true
                        },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Nouvelle manche", fontWeight = FontWeight.Bold) },
                        containerColor = Gold,
                        contentColor = Color.Black,
                    )
                }
            },
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Team.entries.forEach { team ->
                        ScoreCard(game, team, Modifier.weight(1f))
                    }
                }
                StatusBanner(game)
                History(
                    game = game,
                    onLongPress = { roundToDelete = it },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        AnimatedVisibility(visible = winner != null, enter = fadeIn(), exit = fadeOut()) {
            if (winner != null) {
                WinnerOverlay(
                    game = game,
                    winner = winner,
                    onRematch = viewModel::rematch,
                    onUndo = viewModel::undoLastRound,
                )
            }
        }
    }

    if (showSheet) {
        RoundSheet(
            game = game,
            onDismiss = { showSheet = false },
            onConfirm = { round ->
                showSheet = false
                viewModel.addRound(round)
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    val result = snackbar.showSnackbar(
                        message = roundSummary(game, round),
                        actionLabel = "Annuler",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoLastRound()
                }
            },
        )
    }

    roundToDelete?.let { index ->
        AlertDialog(
            onDismissRequest = { roundToDelete = null },
            title = { Text("Supprimer la manche n°${index + 1} ?") },
            text = { Text("Les points de cette manche seront retirés du score.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeRound(index)
                    roundToDelete = null
                }) { Text("Supprimer") }
            },
            dismissButton = { TextButton(onClick = { roundToDelete = null }) { Text("Garder") } },
        )
    }

    if (confirmNewGame) {
        AlertDialog(
            onDismissRequest = { confirmNewGame = false },
            title = { Text("Nouvelle partie ?") },
            text = { Text("Le score et le compteur de victoires seront remis à zéro.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmNewGame = false
                    viewModel.newGame()
                }) { Text("C'est parti") }
            },
            dismissButton = { TextButton(onClick = { confirmNewGame = false }) { Text("Annuler") } },
        )
    }
}

private fun roundSummary(game: GameState, round: Round): String =
    "🎉 +${round.bid} pour ${game.team(round.winner).name}"

@Composable
private fun ScoreCard(game: GameState, team: Team, modifier: Modifier) {
    val color = teamColor(team)
    val total = game.total(team)
    val shown by animateIntAsState(total, animationSpec = tween(700), label = "total")
    val leading = game.leader == team
    val bump by animateFloatAsState(
        if (leading) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bump",
    )
    val progress by animateFloatAsState(
        (total.toFloat() / game.rules.target).coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progress",
    )

    Card(
        modifier = modifier.scale(bump),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color, contentColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (leading) 8.dp else 2.dp),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (leading) Text("👑 ", fontSize = 18.sp)
                Text(
                    game.team(team).name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                game.team(team).players.joinToString(" & "),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text("$shown", fontSize = 56.sp, fontWeight = FontWeight.Black, lineHeight = 60.sp)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Gold,
                trackColor = Color.White.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "encore ${(game.rules.target - total).coerceAtLeast(0)} · 🏆 ${game.wins(team)}",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun StatusBanner(game: GameState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("🃏", fontSize = 22.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("${game.dealer} distribue", fontWeight = FontWeight.Bold)
                Text(
                    "Partie en ${game.rules.target} · manche n°${game.rounds.size + 1}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun History(game: GameState, onLongPress: (Int) -> Unit, modifier: Modifier) {
    if (game.rounds.isEmpty()) {
        Column(
            modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🂡 🂱 🃁 🃑", fontSize = 44.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Aucune manche pour l'instant.\nDistribuez, jouez, puis appuyez sur « Nouvelle manche » !",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Row(Modifier.padding(horizontal = 14.dp)) {
                Text("Manche", Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(game.teamA.name, Modifier.width(76.dp), fontSize = 12.sp, color = teamColor(Team.A), textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(game.teamB.name, Modifier.width(76.dp), fontSize = 12.sp, color = teamColor(Team.B), textAlign = TextAlign.End, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        // Latest deal on top: that's the one people want to check.
        val indexed = game.rounds.withIndex().reversed()
        items(indexed, key = { it.index }) { (index, round) ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .animateItem()
                    .combinedClickable(onClick = {}, onLongClick = { onLongPress(index) }),
            ) {
                Row(
                    Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("n°${index + 1}", fontWeight = FontWeight.Bold)
                        Text(describe(game, round), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(round.points(Team.A).let { if (it > 0) "+$it" else "—" }, Modifier.width(76.dp), textAlign = TextAlign.End, fontSize = 20.sp, fontWeight = FontWeight.Black, color = teamColor(Team.A))
                    Text(round.points(Team.B).let { if (it > 0) "+$it" else "—" }, Modifier.width(76.dp), textAlign = TextAlign.End, fontSize = 20.sp, fontWeight = FontWeight.Black, color = teamColor(Team.B))
                }
            }
        }
        item {
            Text(
                "Appui long sur une manche pour la supprimer",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun describe(game: GameState, round: Round): String =
    "${game.team(round.winner).name} · ${round.bid}"

@Composable
private fun WinnerOverlay(game: GameState, winner: Team, onRematch: () -> Unit, onUndo: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(winner) { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            // Swallow taps so the board underneath stays untouched.
            .clickable(interactionSource = null, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Confetti(Modifier.fillMaxSize())
        val appear = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(visibleState = appear, enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy))) {
            Card(
                modifier = Modifier.padding(24.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("🏆", fontSize = 72.sp)
                    Text("Mabrouk !", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Gold)
                    Text("مبروك", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(
                        game.team(winner).name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = teamColor(winner),
                        textAlign = TextAlign.Center,
                    )
                    Text(game.team(winner).players.joinToString(" & "), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${game.total(winner)} à ${game.total(winner.other)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onRematch, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(18.dp)) {
                        Text("Revanche ! 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = onUndo, modifier = Modifier.fillMaxWidth()) {
                        Text("↩ Annuler la dernière manche")
                    }
                }
            }
        }
    }
}
