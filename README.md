# Tic Tac Toe 3 — Android

A native Kotlin + Jetpack Compose strategy game. Each player has three pieces: place all pieces, then slide one piece to an adjacent empty cell (including diagonals) to make three in a row.

## Build

Open this repository in Android Studio and sync Gradle. Or use GitHub Actions: **Actions → Android APK → Run workflow**. The workflow builds the debug APK and uploads it as an artifact.

## Install

Download the `tic-tac-toe-3-debug-apk` artifact from a successful Actions run, unzip it, and install `app-debug.apk` on an Android device. You may need to allow installation from your browser or file manager.

## Rules

- X starts. Players alternate placing exactly three pieces on any empty cells.
- After six placements, players move one of their pieces to any adjacent empty cell (8 directions; no jumping).
- Three in a row horizontally, vertically, or diagonally wins.
- Repeating the same board and player-to-move state three times is a draw.
- A player with no legal move loses.

## Project

- Application ID: `com.atomstream.tictactoe3`
- Kotlin + Jetpack Compose + Material 3
- Local two-player and three AI difficulty levels
- Persistent win/draw statistics
- Unit tests for core rules

The debug APK is suitable for testing. A signed release build requires a signing key; do not commit private signing credentials.
