package net.hidme.games.gameoflife.support;

import net.hidme.games.gameoflife.model.LifeModel;
import net.hidme.games.gameoflife.session.GameSession;
import net.hidme.games.gameoflife.session.TaskScheduler;

/**
 * The only implementation binding points in the contract tests.
 * Replace these failures with real construction/fixture setup when implementing
 * the modules. Never return the session tests' model stub as the tested model.
 */
public final class TestSubjects {
    private TestSubjects() {
    }

    public static LifeModel newModel() {
        return new net.hidme.games.gameoflife.model.DefaultLifeModel();
    }

    /** Prepare both grids directly, without depending on a cell-editing command. */
    public static LifeModel modelWithState(int[][] current, int[][] lastEdited) {
        return new net.hidme.games.gameoflife.model.DefaultLifeModel(current, lastEdited);
    }

    public static GameSession newSession(LifeModel model, TaskScheduler scheduler) {
        return new net.hidme.games.gameoflife.session.DefaultGameSession(model, scheduler);
    }
}
