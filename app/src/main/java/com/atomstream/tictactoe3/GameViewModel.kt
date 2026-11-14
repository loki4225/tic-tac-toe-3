package com.atomstream.tictactoe3

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.atomstream.tictactoe3.game.AiDifficulty
import com.atomstream.tictactoe3.game.AiPlayer
import com.atomstream.tictactoe3.game.GameEngine
import com.atomstream.tictactoe3.game.GamePhase
import com.atomstream.tictactoe3.game.GameState
import com.atomstream.tictactoe3.game.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScoreStats(val xWins: Int = 0, val oWins: Int = 0, val draws: Int = 0)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("tic_tac_toe_3", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state.asStateFlow()
    private val _againstComputer = MutableStateFlow(false)
    val againstComputer: StateFlow<Boolean> = _againstComputer.asStateFlow()
    private val _difficulty = MutableStateFlow(AiDifficulty.MEDIUM)
    val difficulty: StateFlow<AiDifficulty> = _difficulty.asStateFlow()
    private val _stats = MutableStateFlow(loadStats())
    val stats: StateFlow<ScoreStats> = _stats.asStateFlow()
    private var selectedCell: Int? = null

    fun setComputer(enabled: Boolean) {
        _againstComputer.value = enabled
        newGame()
    }

    fun setDifficulty(value: AiDifficulty) { _difficulty.value = value }

    fun newGame() {
        _state.value = GameState()
        selectedCell = null
    }

    fun tapCell(index: Int) {
        val current = _state.value
        if (current.winner != null || current.draw) return
        if (_againstComputer.value && current.currentPlayer == Player.O) return
        if (current.phase == GamePhase.PLACEMENT) {
            if (current.board.getOrNull(index) != null) return
            _state.value = GameEngine.place(current, index)
            afterTurn()
        } else {
            val piece = current.board.getOrNull(index)
            if (selectedCell == null) {
                if (piece == current.currentPlayer) selectedCell = index
            } else {
                val from = selectedCell!!
                if (piece == current.currentPlayer) selectedCell = index
                else if (index in GameEngine.legalMoves(current, from)) {
                    _state.value = GameEngine.move(current, from, index)
                    selectedCell = null
                    afterTurn()
                } else if (piece == null) {
                    selectedCell = null
                } else selectedCell = null
            }
        }
    }

    fun isSelected(index: Int): Boolean = selectedCell == index
    fun validTargets(): Set<Int> {
        val s = _state.value
        return selectedCell?.let { GameEngine.legalMoves(s, it).toSet() } ?: emptySet()
    }

    private fun afterTurn() {
        val s = _state.value
        if (s.winner != null || s.draw) {
            recordResult(s)
            return
        }
        if (_againstComputer.value && s.currentPlayer == Player.O) {
            _state.value = AiPlayer.play(s, _difficulty.value)
            val result = _state.value
            if (result.winner != null || result.draw) recordResult(result)
        }
    }

    private fun loadStats() = ScoreStats(
        prefs.getInt("x_wins", 0), prefs.getInt("o_wins", 0), prefs.getInt("draws", 0)
    )

    private fun recordResult(s: GameState) {
        val old = _stats.value
        val updated = when {
            s.winner == Player.X -> old.copy(xWins = old.xWins + 1)
            s.winner == Player.O -> old.copy(oWins = old.oWins + 1)
            s.draw -> old.copy(draws = old.draws + 1)
            else -> old
        }
        if (updated != old) {
            _stats.value = updated
            prefs.edit().putInt("x_wins", updated.xWins)
                .putInt("o_wins", updated.oWins).putInt("draws", updated.draws).apply()
        }
    }
}
