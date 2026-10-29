package com.touti.score.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touti.score.model.Announce
import com.touti.score.model.GameState
import com.touti.score.model.Rules
import com.touti.score.model.Team
import com.touti.score.model.TeamInfo

private val Targets = listOf(300, 500, 600, 1000)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    initial: GameState,
    editing: Boolean,
    onCancel: () -> Unit,
    arabicVoiceAvailable: Boolean?,
    onTestVoice: (TeamInfo, TeamInfo, Announce) -> Unit,
    onConfirm: (TeamInfo, TeamInfo, Rules, Int) -> Unit,
) {
    var nameA by rememberSaveable { mutableStateOf(initial.teamA.name) }
    var nameB by rememberSaveable { mutableStateOf(initial.teamB.name) }
    var a1 by rememberSaveable { mutableStateOf(initial.teamA.players[0]) }
    var a2 by rememberSaveable { mutableStateOf(initial.teamA.players[1]) }
    var b1 by rememberSaveable { mutableStateOf(initial.teamB.players[0]) }
    var b2 by rememberSaveable { mutableStateOf(initial.teamB.players[1]) }
    var target by rememberSaveable { mutableIntStateOf(initial.rules.target) }
    var firstDealer by rememberSaveable { mutableIntStateOf(initial.firstDealer) }
    var announce by rememberSaveable { mutableStateOf(initial.rules.announce) }

    fun orDefault(value: String, fallback: String) = value.trim().ifEmpty { fallback }
    val seats = remember(a1, a2, b1, b2) {
        listOf(orDefault(a1, "Joueur 1"), orDefault(b1, "Joueur 2"), orDefault(a2, "Joueur 3"), orDefault(b2, "Joueur 4"))
    }

    fun teamA() = TeamInfo(orDefault(nameA, "Équipe A"), listOf(seats[0], seats[2]))
    fun teamB() = TeamInfo(orDefault(nameB, "Équipe B"), listOf(seats[1], seats[3]))

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "🃏 Touti ⭐",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                if (editing) "Modifier la partie" else "2 contre 2, à la marocaine. Qui joue ?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )

            TeamCard(Team.A, "🦁", nameA, { nameA = it }, a1, { a1 = it }, a2, { a2 = it })
            Text("— VS —", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            TeamCard(Team.B, "🦅", nameB, { nameB = it }, b1, { b1 = it }, b2, { b2 = it })

            Section("🎯 Partie en") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Targets.forEach { t ->
                        FilterChip(
                            selected = target == t,
                            onClick = { target = t },
                            label = { Text("$t pts", fontWeight = FontWeight.Bold) },
                        )
                    }
                }
            }

            Section("🃏 Qui distribue en premier ?") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    seats.forEachIndexed { i, name ->
                        FilterChip(
                            selected = firstDealer == i,
                            onClick = { firstDealer = i },
                            label = { Text(name) },
                        )
                    }
                }
            }

            Section("🔊 Annonce du score à voix haute") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Announce.BOTH to "Français + العربية",
                        Announce.FRENCH to "Français",
                        Announce.ARABIC to "العربية",
                    ).forEach { (value, label) ->
                        FilterChip(
                            selected = announce == value,
                            onClick = { announce = value },
                            label = { Text(label) },
                        )
                    }
                }
                TextButton(onClick = { onTestVoice(teamA(), teamB(), announce) }) { Text("▶ Écouter un exemple") }
                if (arabicVoiceAvailable == false && announce != Announce.FRENCH) {
                    Text(
                        "⚠️ Pas de voix arabe sur ce téléphone : installe-la dans Paramètres › Synthèse vocale " +
                            "(moteur Google › Installer des données vocales › العربية).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Text(
                    "Astuce : écris le nom d'une équipe en arabe pour que la voix arabe le prononce ; " +
                        "sinon elle dit « l'équipe verte » ou « l'équipe rouge ».",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    onConfirm(
                        teamA(),
                        teamB(),
                        Rules(target, announce, initial.rules.muted),
                        firstDealer,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(if (editing) "Enregistrer" else "Yallah, on joue ! 🎉", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            if (editing) {
                TextButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) { Text("Annuler") }
            }
        }
    }
}

@Composable
private fun TeamCard(
    team: Team,
    emoji: String,
    name: String,
    onName: (String) -> Unit,
    p1: String,
    onP1: (String) -> Unit,
    p2: String,
    onP2: (String) -> Unit,
) {
    val color = teamColor(team)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 30.sp)
                Spacer(Modifier.padding(4.dp))
                NameField(name, onName, "Nom de l'équipe", Modifier.weight(1f), color)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NameField(p1, onP1, "Joueur", Modifier.weight(1f), color)
                NameField(p2, onP2, "Partenaire", Modifier.weight(1f), color)
            }
        }
    }
}

@Composable
private fun NameField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier, color: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(20)) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = color),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
    )
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        content()
    }
}
