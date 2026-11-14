package com.atomstream.tictactoe3.game

import org.junit.Assert.*
import org.junit.Test

class GameEngineTest {
    @Test fun placementAlternatesAndTransitionsAfterSixPieces() {
        var s = GameState()
        s = GameEngine.place(s, 0)
        assertEquals(Player.O, s.currentPlayer)
        s = GameEngine.place(s, 1)
        s = GameEngine.place(s, 3)
        s = GameEngine.place(s, 4)
        s = GameEngine.place(s, 6)
        s = GameEngine.place(s, 8)
        assertEquals(GamePhase.MOVEMENT, s.phase)
        assertEquals(3, s.xPlaced)
        assertEquals(3, s.oPlaced)
    }

    @Test fun occupiedCellCannotBeUsed() {
        val s = GameEngine.place(GameState(), 0)
        assertEquals(s, GameEngine.place(s, 0))
    }

    @Test fun detectsAllEightWinningLines() {
        assertEquals(8, GameEngine.winningLines.size)
        GameEngine.winningLines.forEach { line ->
            val board = MutableList<Player?>(9) { null }
            line.forEach { board[it] = Player.X }
            assertEquals(Player.X, GameEngine.winner(board))
        }
    }

    @Test fun movementIncludesAdjacentDiagonalAndRejectsNonAdjacent() {
        // Complete a placement without creating a line:
        // X: 0, 4, 7; O: 2, 5, 8
        var s = GameState()
        listOf(0, 2, 4, 5, 7, 8).forEach { s = GameEngine.place(s, it) }
        assertEquals(GamePhase.MOVEMENT, s.phase)
        assertTrue(1 in GameEngine.legalMoves(s, 4))
        assertTrue(6 in GameEngine.legalMoves(s, 4))
        assertFalse(8 in GameEngine.legalMoves(s, 0)) // empty but not adjacent to cell 0
    }

    @Test fun winnerEndsGame() {
        var s = GameState()
        s = GameEngine.place(s, 0)
        s = GameEngine.place(s, 3)
        s = GameEngine.place(s, 1)
        s = GameEngine.place(s, 4)
        s = GameEngine.place(s, 2)
        assertEquals(Player.X, s.winner)
        assertEquals(s, GameEngine.place(s, 5))
    }
}
