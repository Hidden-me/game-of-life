package net.hidme.games.gameoflife.session;

/** The sole business interface used by the UI. New sessions start in editing mode. */
public interface GameSession {
    /** Queries never advance time, change state, or reschedule automatic steps. */
    SessionSnapshot snapshot();

    /** In editing mode, advances once and remembers the result; otherwise does nothing. */
    void step();

    /**
     * In editing mode, remembers the current grid and enters running mode.
     * The first automatic step is after 500ms, then every 500ms.
     * Repeated starts while running have no effect.
     */
    void start();

    /** Stops automatic steps and enters editing mode without recording the grid. */
    void pause();

    /** Stops automatic steps, enters editing mode, and restores the last edited grid. */
    void reset();

    /** Stops automatic steps, enters editing mode, empties and remembers the grid. */
    void clear();

    /** In editing mode, toggles a cell and remembers the result; otherwise does nothing. */
    void toggleCell(int x, int y);
}
