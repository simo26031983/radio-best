package com.touti.score.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touti.score.model.GameState
import com.touti.score.model.Round
import com.touti.score.model.Team
import com.touti.score.model.Touti

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundSheet(game: GameState, onDismiss: () -> Unit, onConfirm: (Round) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var freeEntry by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Manche n°${game.rounds.size + 1}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
            )
            PrimaryTabRow(selectedTabIndex = if (freeEntry) 1 else 0) {
                Tab(selected = !freeEntry, onClick = { freeEntry = false }, text = { Text("🎯 Contrat") })
                Tab(selected = freeEntry, onClick = { freeEntry = true }, text = { Text("✍️ Saisie libre") })
            }
            if (freeEntry) FreeEntry(game, onConfirm) else ContractEntry(game, onConfirm)
        }
    }
}

@Composable
private fun ContractEntry(game: GameState, onConfirm: (Round) -> Unit) {
    val haptics = LocalHapticFeedback.current
    var taker by rememberSaveable { mutableStateOf<Team?>(null) }
    var bid by rememberSaveable { mutableIntStateOf(Touti.MIN_BID) }
    var cards by rememberSaveable { mutableIntStateOf(Touti.MIN_BID) }
    var announceA by rememberSaveable { mutableIntStateOf(0) }
    var announceB by rememberSaveable { mutableIntStateOf(0) }

    Label("1. Qui a pris ?")
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Team.entries.forEach { team ->
            val selected = taker == team
            val color = teamColor(team)
            Button(
                onClick = {
                    taker = team
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected) color else color.copy(alpha = 0.12f),
                    contentColor = if (selected) Color.White else color,
                ),
            ) {
                Text(
                    (if (selected) "✓ " else "") + game.team(team).name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }

    Label("2. Contrat annoncé")
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { listState.scrollToItem((Touti.bids.indexOf(bid) - 1).coerceAtLeast(0)) }
    LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(Touti.bids) { value ->
            FilterChip(
                selected = bid == value,
                onClick = {
                    bid = value
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                label = { Text("$value", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Gold,
                    selectedLabelColor = Color.Black,
                ),
            )
        }
    }

    val takerTeam = taker
    val takerColor = takerTeam?.let(::teamColor) ?: MaterialTheme.colorScheme.outline
    Label(
        if (takerTeam == null) "3. Points des cartes du preneur" else "3. Points des cartes de ${game.team(takerTeam).name}",
    )
    PointsPicker(
        value = cards,
        onValueChange = { cards = it },
        max = Touti.DEAL_POINTS,
        color = takerColor,
    )
    if (takerTeam != null) {
        Text(
            "→ ${game.team(takerTeam.other).name} : ${Touti.DEAL_POINTS - cards} pts de cartes",
            color = teamColor(takerTeam.other),
            fontWeight = FontWeight.SemiBold,
        )
    }

    Label("4. Annonces (ghna) 👑")
    val announcesLeft = Touti.MAX_ANNOUNCES - announceA - announceB
    AnnounceRow(game.teamA.name, Team.A, announceA, announcesLeft) { announceA = it }
    AnnounceRow(game.teamB.name, Team.B, announceB, announcesLeft) { announceB = it }

    if (takerTeam != null) {
        val preview = Touti.scoreDeal(takerTeam, bid, cards, announceA, announceB, game.rules)
        PreviewCard(game, preview)
        Button(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onConfirm(preview)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Valider la manche", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    } else {
        Text(
            "👆 Choisis d'abord l'équipe qui a pris",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FreeEntry(game: GameState, onConfirm: (Round) -> Unit) {
    var pointsA by rememberSaveable { mutableIntStateOf(0) }
    var pointsB by rememberSaveable { mutableIntStateOf(0) }
    Text(
        "Pour une manche spéciale ou une correction : entre directement les points de chaque équipe.",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Label(game.teamA.name)
    PointsPicker(pointsA, { pointsA = it }, max = Touti.MAX_BID, color = teamColor(Team.A))
    Label(game.teamB.name)
    PointsPicker(pointsB, { pointsB = it }, max = Touti.MAX_BID, color = teamColor(Team.B))
    Button(
        onClick = { onConfirm(Touti.freeEntry(pointsA, pointsB)) },
        enabled = pointsA > 0 || pointsB > 0,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Text("Valider", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

/** Big number with ±1/±10 buttons for precision and a slider for speed. */
@Composable
private fun PointsPicker(value: Int, onValueChange: (Int) -> Unit, max: Int, color: Color) {
    val haptics = LocalHapticFeedback.current
    fun set(v: Int) {
        val clamped = v.coerceIn(0, max)
        if (clamped != value) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onValueChange(clamped)
        }
    }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StepButton("−10") { set(value - 10) }
            StepButton("−1") { set(value - 1) }
            Text(
                "$value",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = color,
            )
            StepButton("+1") { set(value + 1) }
            StepButton("+10") { set(value + 10) }
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { set(it.toInt()) },
            valueRange = 0f..max.toFloat(),
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color),
        )
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(width = 58.dp, height = 48.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(label, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AnnounceRow(name: String, team: Team, value: Int, left: Int, onChange: (Int) -> Unit) {
    val color = teamColor(team)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(name, Modifier.weight(1f), color = color, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(if (value > 0) "+$value" else "—", fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Spacer(Modifier.width(2.dp))
        OutlinedButton(onClick = { onChange(value + Touti.ANNOUNCE) }, enabled = left >= Touti.ANNOUNCE) {
            Text("+20")
        }
        OutlinedButton(onClick = { onChange(value + Touti.TRUMP_ANNOUNCE) }, enabled = left >= Touti.TRUMP_ANNOUNCE) {
            Text("+40")
        }
        if (value > 0) {
            OutlinedButton(onClick = { onChange(0) }, contentPadding = PaddingValues(0.dp)) {
                Text("✕")
            }
        }
    }
}

@Composable
private fun PreviewCard(game: GameState, round: Round) {
    val background by animateColorAsState(
        if (round.made) MarocGreen.copy(alpha = 0.15f) else MarocRed.copy(alpha = 0.15f),
        label = "preview",
    )
    Card(colors = CardDefaults.cardColors(containerColor = background), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (round.made) "Contrat réussi ! 🎉" else "Chuté ! 💥",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                Team.entries.forEach { team ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(game.team(team).name, color = teamColor(team), fontWeight = FontWeight.SemiBold, maxLines = 1)
                        Text("+${round.points(team)}", fontSize = 28.sp, fontWeight = FontWeight.Black, color = teamColor(team))
                    }
                }
            }
        }
    }
}
