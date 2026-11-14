package com.atomstream.tictactoe3.game

enum class Player { X, O;
    fun other(): Player = if (this == X) O else X
}

enum class GamePhase { PLACEMENT, MOVEMENT }

data class GameState(
    val board: List<Player?> = List(9) { null },
    val currentPlayer: Player = Player.X,
    val phase: GamePhase = GamePhase.PLACEMENT,
    val xPlaced: Int = 0,
    val oPlaced: Int = 0,
    val winner: Player? = null,
    val draw: Boolean = false,
    val repetitions: Map<String, Int> = mapOf(positionKey(List(9) { null }, Player.X) to 1)
) {
    companion object {
        fun positionKey(board: List<Player?>, turn: Player): String =
            board.joinToString("") { it?.name ?: "-" } + turn.name
    }
}

object GameEngine {
    val winningLines = listOf(
        listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
        listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
        listOf(0, 4, 8), listOf(2, 4, 6)
    )

    fun winner(board: List<Player?>): Player? = winningLines.firstNotNullOfOrNull { line ->
        val p = board[line[0]]
        if (p != null && line.all { board[it] == p }) p else null
    }

    fun legalMoves(state: GameState, from: Int): List<Int> {
        if (state.phase != GamePhase.MOVEMENT || state.winner != null || state.draw) return emptyList()
        if (from !in 0..8 || state.board[from] != state.currentPlayer) return emptyList()
        // Any empty cell is a valid destination; pieces are not restricted by adjacency.
        return state.board.indices.filter { state.board[it] == null }
    }

    fun hasAnyLegalMove(state: GameState, player: Player = state.currentPlayer): Boolean {
        if (state.phase != GamePhase.MOVEMENT) return true
        return state.board.indices.any { index ->
            state.board[index] == player && state.board.indices.any { state.board[it] == null }
        }
    }

    fun place(state: GameState, cell: Int): GameState {
        if (state.phase != GamePhase.PLACEMENT || state.winner != null || state.draw) return state
        if (cell !in 0..8 || state.board[cell] != null) return state
        val nextBoard = state.board.toMutableList()
        nextBoard[cell] = state.currentPlayer
        val x = state.xPlaced + if (state.currentPlayer == Player.X) 1 else 0
        val o = state.oPlaced + if (state.currentPlayer == Player.O) 1 else 0
        val win = winner(nextBoard)
        val nextPlayer = state.currentPlayer.other()
        val nextPhase = if (x == 3 && o == 3) GamePhase.MOVEMENT else GamePhase.PLACEMENT
        return finishTurn(state, nextBoard, nextPlayer, nextPhase, x, o, win)
    }

    fun move(state: GameState, from: Int, to: Int): GameState {
        if (to !in legalMoves(state, from)) return state
        val nextBoard = state.board.toMutableList()
        nextBoard[to] = state.currentPlayer
        nextBoard[from] = null
        val nextPlayer = state.currentPlayer.other()
        return finishTurn(state, nextBoard, nextPlayer, GamePhase.MOVEMENT,
            state.xPlaced, state.oPlaced, winner(nextBoard))
    }

    private fun finishTurn(
        old: GameState, board: List<Player?>, next: Player, phase: GamePhase,
        x: Int, o: Int, win: Player?
    ): GameState {
        if (win != null) return GameState(board, next, phase, x, o, winner = win, repetitions = old.repetitions)
        if (phase == GamePhase.MOVEMENT) {
            val probe = GameState(board, next, phase, x, o)
            if (!hasAnyLegalMove(probe, next)) {
                return GameState(board, next, phase, x, o, winner = next.other(), repetitions = old.repetitions)
            }
        }
        val key = GameState.positionKey(board, next)
        val history = old.repetitions.toMutableMap()
        history[key] = (history[key] ?: 0) + 1
        val isDraw = history[key]!! >= 3
        return GameState(board, next, phase, x, o, draw = isDraw, repetitions = history)
    }
}
