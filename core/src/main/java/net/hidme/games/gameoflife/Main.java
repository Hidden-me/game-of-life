package net.hidme.games.gameoflife;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import net.hidme.games.gameoflife.model.DefaultLifeModel;
import net.hidme.games.gameoflife.session.DefaultGameSession;
import net.hidme.games.gameoflife.session.FrameTaskScheduler;
import net.hidme.games.gameoflife.session.GameSession;
import net.hidme.games.gameoflife.ui.GameInputProcessor;
import net.hidme.games.gameoflife.ui.GameRenderer;
import net.hidme.games.gameoflife.ui.GridViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private FrameTaskScheduler scheduler;
    private GameSession session;
    private GameRenderer renderer;
    private GameInputProcessor input;

    @Override
    public void create() {
        scheduler = new FrameTaskScheduler();
        session = new DefaultGameSession(new DefaultLifeModel(), scheduler);
        GridViewport grid = new GridViewport();
        renderer = new GameRenderer(grid);
        renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        input = new GameInputProcessor(session, grid, renderer);
        Gdx.input.setInputProcessor(input);
    }

    @Override
    public void render() {
        scheduler.update();
        ScreenUtils.clear(0.035f, 0.065f, 0.1f, 1f);
        renderer.draw(session.snapshot(), input.mouseX(), input.mouseY(),
            input.pressedControl(), input.helpVisible());
    }

    @Override
    public void resize(int width, int height) {
        if (renderer != null) {
            renderer.resize(width, height);
        }
    }

    @Override
    public void dispose() {
        session.pause();
        Gdx.input.setInputProcessor(null);
        renderer.dispose();
    }
}
