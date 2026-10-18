package net.hidme.games.gameoflife.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import net.hidme.games.gameoflife.model.LifeModel;

/** A clipped, draggable grid. Coordinates use the screen's top-left origin. */
public final class GridViewport {
    private static final int CELL_SIZE = 24;
    private static final float GRID_PIXELS = LifeModel.GRID_SIZE * CELL_SIZE;
    private static final Color DEAD = Color.valueOf("102234");
    private static final Color ALIVE = Color.valueOf("f5e6bd");
    private static final Color LINES = Color.valueOf("253f53");
    private final Rectangle bounds = new Rectangle();
    private float offsetX;
    private float offsetY;
    private boolean positioned;

    public void resize(float x, float y, float width, float height) {
        if (!positioned) {
            offsetX = (width - GRID_PIXELS) / 2;
            offsetY = (height - GRID_PIXELS) / 2;
            positioned = true;
        } else {
            offsetX += (width - bounds.width) / 2;
            offsetY += (height - bounds.height) / 2;
        }
        bounds.set(x, y, width, height);
        drag(0, 0);
    }

    public void drag(float dx, float dy) {
        offsetX = clampOffset(offsetX + dx, bounds.width);
        offsetY = clampOffset(offsetY + dy, bounds.height);
    }

    public boolean contains(float x, float y) {
        return x >= bounds.x && y >= bounds.y
            && x < bounds.x + bounds.width && y < bounds.y + bounds.height;
    }

    public int cellX(float screenX) {
        return (int) Math.floor((screenX - bounds.x - offsetX) / CELL_SIZE);
    }

    public int cellY(float screenY) {
        return (int) Math.floor((screenY - bounds.y - offsetY) / CELL_SIZE);
    }

    public boolean hasCell(float screenX, float screenY) {
        return contains(screenX, screenY) && cellX(screenX) >= 0 && cellY(screenY) >= 0
            && cellX(screenX) < LifeModel.GRID_SIZE && cellY(screenY) < LifeModel.GRID_SIZE;
    }

    /** Called inside a filled-shapes pass; partial cells are clipped to the viewport. */
    public void drawCells(ShapeRenderer shapes, int[][] cells) {
        shapes.setColor(DEAD);
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
        shapes.setColor(ALIVE);
        int firstX = Math.max(0, cellX(bounds.x));
        int firstY = Math.max(0, cellY(bounds.y));
        int endX = Math.min(LifeModel.GRID_SIZE, cellX(bounds.x + bounds.width) + 1);
        int endY = Math.min(LifeModel.GRID_SIZE, cellY(bounds.y + bounds.height) + 1);
        for (int y = firstY; y < endY; y++) {
            for (int x = firstX; x < endX; x++) {
                if (cells[y][x] == 1) {
                    float left = Math.max(bounds.x, bounds.x + offsetX + x * CELL_SIZE + 2);
                    float top = Math.max(bounds.y, bounds.y + offsetY + y * CELL_SIZE + 2);
                    float right = Math.min(bounds.x + bounds.width,
                        bounds.x + offsetX + (x + 1) * CELL_SIZE - 2);
                    float bottom = Math.min(bounds.y + bounds.height,
                        bounds.y + offsetY + (y + 1) * CELL_SIZE - 2);
                    if (right > left && bottom > top) {
                        shapes.rect(left, top, right - left, bottom - top);
                    }
                }
            }
        }
    }

    /** Called inside a line-shapes pass. Every line belongs to the same grid. */
    public void drawLines(ShapeRenderer shapes, boolean editing, int mouseX, int mouseY) {
        shapes.setColor(LINES);
        float gridLeft = bounds.x + offsetX;
        float gridTop = bounds.y + offsetY;
        float left = Math.max(bounds.x, gridLeft);
        float top = Math.max(bounds.y, gridTop);
        float right = Math.min(bounds.x + bounds.width, gridLeft + GRID_PIXELS);
        float bottom = Math.min(bounds.y + bounds.height, gridTop + GRID_PIXELS);
        for (int i = 0; i <= LifeModel.GRID_SIZE; i++) {
            float x = gridLeft + i * CELL_SIZE;
            float y = gridTop + i * CELL_SIZE;
            if (x >= left && x <= right) {
                shapes.line(x, top, x, bottom);
            }
            if (y >= top && y <= bottom) {
                shapes.line(left, y, right, y);
            }
        }
        if (editing && hasCell(mouseX, mouseY)) {
            shapes.setColor(ALIVE);
            float x = gridLeft + cellX(mouseX) * CELL_SIZE;
            float y = gridTop + cellY(mouseY) * CELL_SIZE;
            float x1 = Math.max(bounds.x, x);
            float y1 = Math.max(bounds.y, y);
            float x2 = Math.min(bounds.x + bounds.width, x + CELL_SIZE);
            float y2 = Math.min(bounds.y + bounds.height, y + CELL_SIZE);
            shapes.rect(x1, y1, x2 - x1, y2 - y1);
        }
        shapes.setColor(Color.valueOf("3b5a70"));
        shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private static float clampOffset(float offset, float visibleSize) {
        if (visibleSize >= GRID_PIXELS) {
            return (visibleSize - GRID_PIXELS) / 2;
        }
        return Math.max(visibleSize - GRID_PIXELS, Math.min(0, offset));
    }
}
