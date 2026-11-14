package com.atomstream.tictactoe3.game

import kotlin.random.Random

enum class AiDifficulty { EASY, MEDIUM, HARD }

object AiPlayer {
    /** Returns the resulting state after the computer's legal action. */
    fun play(state: GameState, difficulty: AiDifficulty): GameState {
        if (state.winner != null || state.draw) return state
        val candidates = if (state.phase == GamePhase.PLACEMENT) {
            state.board.indices.filter { state.board[it] == null }.map { GameEngine.place(state, it) }
        } else {
            state.board.indices.filter { state.board[it] == state.currentPlayer }
                .flatMap { from -> GameEngine.legalMoves(state, from).map { to -> GameEngine.move(state, from, to) } }
        }
        if (candidates.isEmpty()) return state
        if (difficulty == AiDifficulty.EASY) return candidates.random()

        val scored = candidates.map { it to score(it, state.currentPlayer) }
        val best = scored.maxOf { it.second }
        val top = scored.filter { it.second == best }.map { it.first }
        if (difficulty == AiDifficulty.MEDIUM) return top.random()

        // Hard mode adds a shallow opponent-response search to the positional evaluation.
        return top.maxByOrNull { candidate ->
            val replyStates = legalReplies(candidate)
            val worstReplyScore: Double =
                replyStates.maxOfOrNull { score(it, state.currentPlayer.other()) } ?: 0.0
            score(candidate, state.currentPlayer) - worstReplyScore * 0.65
        } ?: top.first()
    }

    private fun legalReplies(state: GameState): List<GameState> {
        if (state.winner != null || state.draw) return emptyList()
        return if (state.phase == GamePhase.PLACEMENT) {
            state.board.indices.filter { state.board[it] == null }.map { GameEngine.place(state, it) }
        } else {
            state.board.indices.filter { state.board[it] == state.currentPlayer }
                .flatMap { from -> GameEngine.legalMoves(state, from).map { to -> GameEngine.move(state, from, to) } }
        }
    }

    private fun score(state: GameState, perspective: Player): Double {
        if (state.winner == perspective) return 10_000.0
        if (state.winner == perspective.other()) return -10_000.0
        if (state.draw) return 0.0
        var value = 0.0
        GameEngine.winningLines.forEach { line ->
            val own = line.count { state.board[it] == perspective }
            val enemy = line.count { state.board[it] == perspective.other() }
            if (enemy == 0) value += when (own) { 2 -> 8.0; 1 -> 1.5; else -> 0.0 }
            if (own == 0) value -= when (enemy) { 2 -> 9.0; 1 -> 1.7; else -> 0.0 }
        }
        if (state.board[4] == perspective) value += 1.2
        if (state.board[4] == perspective.other()) value -= 1.2
        return value
    }
}
