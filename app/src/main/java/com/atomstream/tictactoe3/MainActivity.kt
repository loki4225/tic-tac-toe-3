package com.atomstream.tictactoe3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.atomstream.tictactoe3.game.AiDifficulty
import com.atomstream.tictactoe3.game.GamePhase
import com.atomstream.tictactoe3.game.GameState
import com.atomstream.tictactoe3.game.Player

private val Navy = Color(0xFF101827)
private val Panel = Color(0xFF1B2940)
private val Blue = Color(0xFF54A6FF)
private val Red = Color(0xFFFF6B78)
private val Muted = Color(0xFF9AAAC0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(16, 24, 39)
        window.navigationBarColor = android.graphics.Color.rgb(16, 24, 39)
        setContent { TicTacToeTheme { GameApp() } }
    }
}

@Composable
private fun TicTacToeTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Blue, secondary = Red, background = Navy, surface = Panel,
            onBackground = Color.White, onSurface = Color.White
        ),
        content = content
    )
}

@Composable
private fun GameApp(vm: GameViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val vsComputer by vm.againstComputer.collectAsState()
    val difficulty by vm.difficulty.collectAsState()
    val stats by vm.stats.collectAsState()
    var showRules by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Navy) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("TIC TAC TOE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
                    Text("THREE-PIECE STRATEGY", style = MaterialTheme.typography.labelSmall, color = Muted, letterSpacing = 2.sp)
                }
                TextButton(onClick = { showRules = true }) { Text("HOW TO PLAY") }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeCard("2 PLAYERS", !vsComputer, Modifier.weight(1f)) { vm.setComputer(false) }
                ModeCard("VS COMPUTER", vsComputer, Modifier.weight(1f)) { vm.setComputer(true) }
            }
            if (vsComputer) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text("AI LEVEL", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(12.dp))
                    AiDifficulty.values().forEach { level ->
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { vm.setDifficulty(level) },
                            label = { Text(level.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                        Spacer(Modifier.width(5.dp))
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PlayerCard("PLAYER X", "BLUE", state.currentPlayer == Player.X && !isFinished(state), Blue, Modifier.weight(1f))
                PlayerCard(if (vsComputer) "COMPUTER O" else "PLAYER O", "RED", state.currentPlayer == Player.O && !isFinished(state), Red, Modifier.weight(1f))
            }

            val status = when {
                state.winner != null -> "PLAYER ${state.winner} WINS!"
                state.draw -> "IT'S A DRAW"
                state.phase == GamePhase.PLACEMENT -> "PLAYER ${state.currentPlayer}: PLACE A PIECE"
                else -> "PLAYER ${state.currentPlayer}: MOVE A PIECE"
            }
            Text(status, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (state.winner == Player.X) Blue else if (state.winner == Player.O) Red else Color.White)
            Text(
                if (state.phase == GamePhase.PLACEMENT) "Place 3 pieces each on any empty cell"
                else "Select your piece, then a glowing adjacent cell",
                style = MaterialTheme.typography.bodySmall, color = Muted
            )

            Board(state, vm)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PieceCount("X", state.xPlaced, Blue)
                Text(if (state.phase == GamePhase.PLACEMENT) "PLACEMENT" else "MOVEMENT", color = Muted, fontWeight = FontWeight.Bold)
                PieceCount("O", state.oPlaced, Red)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = vm::newGame, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("NEW GAME") }
                OutlinedButton(onClick = { showRules = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)) { Text("RULES") }
            }

            Spacer(Modifier.weight(1f))
            Card(colors = CardDefaults.cardColors(containerColor = Panel), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Stat("X WINS", stats.xWins, Blue)
                    Stat("O WINS", stats.oWins, Red)
                    Stat("DRAWS", stats.draws, Muted)
                }
            }
        }
    }
    if (showRules) {
        AlertDialog(
            onDismissRequest = { showRules = false },
            title = { Text("HOW TO PLAY") },
            text = {
                Text("Each player has 3 pieces. First, take turns placing all six pieces on empty cells. Then take turns moving one piece to an adjacent empty cell. Adjacent includes horizontal, vertical and diagonal neighbours. No jumping. Make a straight line of three to win. Repeating a position three times is a draw; having no legal move means you lose.")
            },
            confirmButton = { TextButton(onClick = { showRules = false }) { Text("GOT IT") } }
        )
    }
}

@Composable
private fun ModeCard(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val borderColor = if (selected) Blue else Color(0xFF33445D)
    Card(modifier.clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = if (selected) Color(0xFF203B5D) else Panel), shape = RoundedCornerShape(14.dp)) {
        Box(Modifier.fillMaxWidth().padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun PlayerCard(title: String, subtitle: String, active: Boolean, tint: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = if (active) tint.copy(alpha = 0.18f) else Panel), shape = RoundedCornerShape(14.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(tint))
            Spacer(Modifier.width(9.dp))
            Column {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun Board(state: GameState, vm: GameViewModel) {
    val selectedCell by vm.selectedCell.collectAsState()
    val targets = vm.validTargets()
    Column(
        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(22.dp)).background(Panel).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in 0..2) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (col in 0..2) {
                    val index = row * 3 + col
                    val player = state.board[index]
                    val selected = selectedCell == index
                    val target = index in targets
                    val tint = when (player) { Player.X -> Blue; Player.O -> Red; null -> Color(0xFF263750) }
                    val cellColor by animateColorAsState(if (selected) Color(0xFF355B82) else if (target) Color(0xFF294A43) else Color(0xFF142238), label = "cell")
                    val borderColor = if (target) Color(0xFF65D6A4) else if (selected) Blue else Color(0xFF31445E)
                    Box(
                        Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(16.dp))
                            .background(cellColor).border(if (target || selected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable(enabled = !isFinished(state), onClick = { vm.tapCell(index) }),
                        contentAlignment = Alignment.Center
                    ) {
                        if (player != null) {
                            Text(player.name, color = tint, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black)
                        } else if (target) {
                            Box(Modifier.size(16.dp).clip(CircleShape).background(Color(0xFF65D6A4)))
                        } else {
                            Text((index + 1).toString(), color = Color(0xFF52647C), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceCount(player: String, count: Int, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(player, color = tint, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(6.dp))
        repeat(3) { i ->
            Box(Modifier.padding(horizontal = 2.dp).size(9.dp).clip(CircleShape).background(if (i < count) tint else Color(0xFF3A4B61)))
        }
    }
}

@Composable
private fun Stat(label: String, value: Int, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), color = tint, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text(label, color = Muted, style = MaterialTheme.typography.labelSmall)
    }
}

private fun isFinished(state: GameState) = state.winner != null || state.draw
