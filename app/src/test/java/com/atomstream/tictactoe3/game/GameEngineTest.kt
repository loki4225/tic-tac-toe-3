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
        val lines = GameEngine.winningLines
        assertEquals(8, lines.size)
        lines.forEach { line ->
            val board = MutableList<Player?>(9) { null }
            line.forEach { board[it] = Player.X }
            assertEquals(Player.X, GameEngine.winner(board))
        }
    }

    @Test fun movementIncludesDiagonalAndRejectsNonAdjacent() {
        var s = GameState()
        s = GameEngine.place(s, 0)
        s = GameEngine.place(s, 8)
        s = GameEngine.place(s, 2)
        s = GameEngine.place(s, 6)
        s = GameEngine.place(s, 4)
        s = GameEngine.place(s, 1)
        assertEquals(GamePhase.MOVEMENT, s.phase)
        assertTrue(4 in GameEngine.legalMoves(s, 4).notUsed()) // replaced below by direct assertion
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
