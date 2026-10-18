package net.hidme.games.gameoflife.session;

import java.util.Objects;

import net.hidme.games.gameoflife.model.LifeModel;
import net.hidme.games.gameoflife.session.TaskScheduler.ScheduledTask;

/** Serial game-thread session; all model changes pass through this boundary. */
public final class DefaultGameSession implements GameSession {
    private static final long STEP_INTERVAL_MILLIS = 500;

    private final LifeModel model;
    private final TaskScheduler scheduler;
    private SessionMode mode = SessionMode.EDITING;
    private ScheduledTask automaticSteps;

    public DefaultGameSession(LifeModel model, TaskScheduler scheduler) {
        this.model = Objects.requireNonNull(model);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    @Override
    public SessionSnapshot snapshot() {
        return new SessionSnapshot(model.currentState(), mode);
    }

    @Override
    public void step() {
        if (mode == SessionMode.EDITING) {
            model.advance();
            model.rememberCurrentState();
        }
    }

    @Override
    public void start() {
        if (mode == SessionMode.RUNNING) {
            return;
        }
        model.rememberCurrentState();
        automaticSteps = scheduler.scheduleAtFixedRate(STEP_INTERVAL_MILLIS, () -> {
            if (mode == SessionMode.RUNNING) {
                model.advance();
            }
        });
        mode = SessionMode.RUNNING;
    }

    @Override
    public void pause() {
        if (automaticSteps != null) {
            automaticSteps.cancel();
            automaticSteps = null;
        }
        mode = SessionMode.EDITING;
    }

    @Override
    public void reset() {
        pause();
        model.reset();
    }

    @Override
    public void clear() {
        pause();
        model.clear();
        model.rememberCurrentState();
    }

    @Override
    public void toggleCell(int x, int y) {
        if (mode == SessionMode.EDITING) {
            model.toggleCell(x, y);
            model.rememberCurrentState();
        }
    }
}
