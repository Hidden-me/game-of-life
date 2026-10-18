package net.hidme.games.gameoflife.model;

/**
 * Stores the current grid and the last edited grid, initially both empty.
 * Grids are 127 by 127, addressed as {@code cells[y][x]}, with values 0 or 1.
 * The session owns this model; the UI only accesses the session.
 */
public interface LifeModel {
    int GRID_SIZE = 127;

    /** Returns a detached copy of the current grid. */
    int[][] currentState();

    /** Returns a detached copy of the last edited grid. */
    int[][] lastEditedState();

    /**
     * Advances all cells simultaneously using the finite-grid Life rules.
     * Missing neighbours are dead; boundaries do not wrap.
     * This operation never changes the last edited grid.
     */
    void advance();

    /** Saves an independent copy of the current grid as the last edited grid. */
    void rememberCurrentState();

    /** Restores the complete current grid from the last edited grid. */
    void reset();

    /** Empties the current grid without changing the last edited grid. */
    void clear();

    /**
     * Toggles one cell of an editable model. Read-only model substitutes may
     * leave this optional operation unsupported.
     */
    default void toggleCell(int x, int y) {
        throw new UnsupportedOperationException("This model does not support cell editing");
    }
}
