package net.hidme.games.gameoflife.session;

/**
 * A point-in-time view. The session supplies a detached 127 by 127 grid,
 * addressed as {@code cells[y][x]}. The caller may edit this copy; neither
 * the model nor any other snapshot may share its rows.
 */
public record SessionSnapshot(int[][] cells, SessionMode mode) {
}
