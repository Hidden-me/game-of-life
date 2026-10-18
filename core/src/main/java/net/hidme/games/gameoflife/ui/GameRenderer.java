package net.hidme.games.gameoflife.ui;

import java.util.EnumMap;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import net.hidme.games.gameoflife.session.SessionMode;
import net.hidme.games.gameoflife.session.SessionSnapshot;

/** Composes the grid, five transport controls, mode indicator and a single help page. */
public final class GameRenderer implements Disposable {
    public enum Control {
        START("Run / 500 ms"), PAUSE("Pause"), STEP("Step"),
        RESET("Reset to last edit"), CLEAR("Clear"), HELP("Help");

        private final String caption;

        Control(String caption) {
            this.caption = caption;
        }
    }

    private static final Color PANEL = Color.valueOf("101f2c");
    private static final Color BORDER = Color.valueOf("2b4558");
    private static final Color INK = Color.valueOf("ecede5");
    private static final Color MUTED = Color.valueOf("728b9d");
    private static final Color EDITING = Color.valueOf("83ced4");
    private static final Color RUNNING = Color.valueOf("f2c563");
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont(true);
    private final GlyphLayout textLayout = new GlyphLayout();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final EnumMap<Control, Rectangle> controls = new EnumMap<>(Control.class);
    private final GridViewport grid;
    private final Rectangle toolbar = new Rectangle();
    private int width;
    private int height;

    public GameRenderer(GridViewport grid) {
        this.grid = grid;
        for (Control control : Control.values()) {
            controls.put(control, new Rectangle());
        }
    }

    public void resize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        camera.setToOrtho(true, this.width, this.height);
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);
        grid.resize(24, 58, Math.max(1, width - 48), Math.max(1, height - 164));
        float button = Math.min(54, Math.max(24, (width - 108) / 5f - 10));
        float total = button * 5 + 40 + 50;
        toolbar.set((width - total) / 2, height - 88, total, 66);
        for (int i = 0; i < 5; i++) {
            controls.get(Control.values()[i]).set(toolbar.x + 44 + i * (button + 10),
                toolbar.y + 6, button, 54);
        }
        controls.get(Control.HELP).set(width - 60, 12, 36, 36);
    }

    public Control controlAt(float x, float y) {
        for (Control control : Control.values()) {
            if (controls.get(control).contains(x, y)) {
                return control;
            }
        }
        return null;
    }

    public boolean enabled(Control control, SessionMode mode) {
        return switch (control) {
            case START, STEP -> mode == SessionMode.EDITING;
            case PAUSE -> mode == SessionMode.RUNNING;
            default -> true;
        };
    }

    public void draw(SessionSnapshot snapshot, int mouseX, int mouseY, Control pressed, boolean help) {
        boolean running = snapshot.mode() == SessionMode.RUNNING;
        Control hovered = controlAt(mouseX, mouseY);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        grid.drawCells(shapes, snapshot.cells());
        shapes.setColor(PANEL);
        shapes.rect(toolbar.x, toolbar.y, toolbar.width, toolbar.height);
        shapes.setColor(running ? RUNNING : EDITING);
        shapes.circle(toolbar.x + 21, toolbar.y + 33, 5, 20);
        for (Control control : Control.values()) {
            Rectangle box = controls.get(control);
            boolean active = enabled(control, snapshot.mode());
            boolean down = (control == Control.START && running)
                || (control == Control.STEP && running) || (active && control == pressed);
            shapes.setColor(down ? Color.valueOf("0a151f")
                : active && hovered == control ? Color.valueOf("20374a") : PANEL);
            shapes.rect(box.x, box.y, box.width, box.height);
            shapes.setColor(active ? INK : Color.valueOf("395366"));
            drawIcon(control, box, down ? 2 : 0);
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        grid.drawLines(shapes, !running && !help, mouseX, mouseY);
        shapes.setColor(BORDER);
        shapes.rect(toolbar.x, toolbar.y, toolbar.width, toolbar.height);
        for (Control control : Control.values()) {
            Rectangle box = controls.get(control);
            shapes.setColor(control == hovered && enabled(control, snapshot.mode()) ? EDITING : BORDER);
            shapes.rect(box.x, box.y, box.width, box.height);
        }
        shapes.end();

        batch.begin();
        font.setColor(INK);
        font.draw(batch, "L I F E", 24, 24);
        font.setColor(MUTED);
        font.draw(batch, "127 x 127", 126, 24);
        Rectangle helpBox = controls.get(Control.HELP);
        centered("?", helpBox.x + helpBox.width / 2, helpBox.y + 10);
        if (!help) {
            String caption = hovered == null
                ? running ? "RUNNING" : "CLICK TO EDIT   /   DRAG TO PAN" : hovered.caption;
            if (mouseX < toolbar.x + 42 && toolbar.contains(mouseX, mouseY)) {
                caption = running ? "RUNNING" : "EDITING";
            }
            centered(caption, width / 2f, height - 16);
        }
        batch.end();
        if (help) {
            drawHelp();
        }
    }

    private void drawIcon(Control control, Rectangle box, float shift) {
        float x = box.x + box.width / 2 + shift;
        float y = box.y + box.height / 2 + shift;
        switch (control) {
            case START -> shapes.triangle(x - 7, y - 11, x - 7, y + 11, x + 10, y);
            case PAUSE -> {
                shapes.rect(x - 8, y - 10, 5, 20);
                shapes.rect(x + 3, y - 10, 5, 20);
            }
            case STEP -> {
                shapes.triangle(x - 10, y - 10, x - 10, y + 10, x + 4, y);
                shapes.rect(x + 6, y - 10, 3, 20);
            }
            case RESET -> {
                for (int i = 0; i < 18; i++) {
                    double a = Math.toRadians(-65 + i * 17);
                    double b = Math.toRadians(-65 + (i + 1) * 17);
                    shapes.rectLine(x + 11 * (float) Math.cos(a), y + 11 * (float) Math.sin(a),
                        x + 11 * (float) Math.cos(b), y + 11 * (float) Math.sin(b), 2);
                }
                shapes.triangle(x + 1, y - 16, x + 1, y - 7, x + 9, y - 10);
            }
            case CLEAR -> {
                shapes.rectLine(x - 8, y - 6, x - 6, y + 12, 2);
                shapes.rectLine(x - 6, y + 12, x + 6, y + 12, 2);
                shapes.rectLine(x + 6, y + 12, x + 8, y - 6, 2);
                shapes.rect(x - 11, y - 10, 22, 2);
                shapes.rect(x - 4, y - 14, 8, 2);
                shapes.rect(x - 3, y - 5, 2, 12);
                shapes.rect(x + 2, y - 5, 2, 12);
            }
            case HELP -> { /* The question mark is drawn with the font. */ }
        }
    }

    private void drawHelp() {
        float cardWidth = Math.min(620, width - 48);
        float cardHeight = Math.min(390, height - 32);
        float x = (width - cardWidth) / 2;
        float y = (height - cardHeight) / 2;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(Color.valueOf("0b1723"));
        shapes.rect(0, 0, width, height);
        shapes.setColor(PANEL);
        shapes.rect(x, y, cardWidth, cardHeight);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(BORDER);
        shapes.rect(x, y, cardWidth, cardHeight);
        shapes.end();

        batch.begin();
        font.setColor(INK);
        centered("THE GAME OF LIFE", width / 2f, y + 28);
        font.setColor(EDITING);
        font.draw(batch, "Each cell has up to 8 neighbours.", x + 28, y + 78);
        font.setColor(INK);
        font.draw(batch, "A dead cell is born with exactly 3 live neighbours.", x + 28, y + 112);
        font.draw(batch, "A live cell survives with 2 or 3 live neighbours.", x + 28, y + 144);
        font.draw(batch, "All other live cells die. All cells change together.", x + 28, y + 176);
        font.setColor(MUTED);
        font.draw(batch, "The grid is finite: opposite edges are not connected.", x + 28, y + 220);
        font.draw(batch, "Click a cell to toggle it. Drag the grid to explore.", x + 28, y + 252);
        font.draw(batch, "Run advances every 500 ms. Pause lets you edit again.", x + 28, y + 284);
        font.draw(batch, "Reset restores the last edit. Clear empties the grid.", x + 28, y + 316);
        font.setColor(EDITING);
        centered("CLICK ANYWHERE TO RETURN", width / 2f, y + cardHeight - 28);
        batch.end();
    }

    private void centered(String text, float x, float y) {
        textLayout.setText(font, text);
        font.draw(batch, text, x - textLayout.width / 2, y);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
