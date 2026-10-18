package net.hidme.games.gameoflife.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import net.hidme.games.gameoflife.session.GameSession;
import net.hidme.games.gameoflife.ui.GameRenderer.Control;

/** Converts completed clicks into session commands; dragging never edits cells. */
public final class GameInputProcessor extends InputAdapter {
    private final GameSession session;
    private final GridViewport grid;
    private final GameRenderer renderer;
    private int mouseX = -1;
    private int mouseY = -1;
    private int downX;
    private int downY;
    private int lastX;
    private int lastY;
    private boolean gridGesture;
    private boolean dragging;
    private boolean helpVisible;
    private Control pressed;

    public GameInputProcessor(GameSession session, GridViewport grid, GameRenderer renderer) {
        this.session = session;
        this.grid = grid;
        this.renderer = renderer;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mouseX = screenX;
        mouseY = screenY;
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || button != Input.Buttons.LEFT) {
            return false;
        }
        mouseMoved(screenX, screenY);
        cancelGesture();
        if (helpVisible) {
            helpVisible = false;
            return true;
        }
        pressed = renderer.controlAt(screenX, screenY);
        if (pressed != null) {
            if (!renderer.enabled(pressed, session.snapshot().mode())) {
                pressed = null;
            }
            return true;
        }
        gridGesture = grid.contains(screenX, screenY);
        downX = lastX = screenX;
        downY = lastY = screenY;
        return gridGesture;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer != 0) {
            return false;
        }
        mouseMoved(screenX, screenY);
        if (!gridGesture) {
            return pressed != null;
        }
        int dx = screenX - downX;
        int dy = screenY - downY;
        dragging |= dx * dx + dy * dy > 16;
        if (dragging) {
            grid.drag(screenX - lastX, screenY - lastY);
            lastX = screenX;
            lastY = screenY;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0 || button != Input.Buttons.LEFT) {
            return false;
        }
        mouseMoved(screenX, screenY);
        boolean handled = pressed != null || gridGesture;
        if (pressed != null && pressed == renderer.controlAt(screenX, screenY)
            && renderer.enabled(pressed, session.snapshot().mode())) {
            switch (pressed) {
                case START -> session.start();
                case PAUSE -> session.pause();
                case STEP -> session.step();
                case RESET -> session.reset();
                case CLEAR -> session.clear();
                case HELP -> helpVisible = true;
            }
        } else if (gridGesture && !dragging && grid.hasCell(screenX, screenY)) {
            session.toggleCell(grid.cellX(screenX), grid.cellY(screenY));
        }
        cancelGesture();
        return handled;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            cancelGesture();
        }
        return true;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE && helpVisible) {
            helpVisible = false;
            return true;
        }
        return false;
    }

    private void cancelGesture() {
        pressed = null;
        gridGesture = false;
        dragging = false;
    }

    public int mouseX() {
        return mouseX;
    }

    public int mouseY() {
        return mouseY;
    }

    public Control pressedControl() {
        return pressed;
    }

    public boolean helpVisible() {
        return helpVisible;
    }
}
