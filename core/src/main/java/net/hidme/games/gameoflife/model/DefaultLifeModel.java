package net.hidme.games.gameoflife.model;

/** Finite, non-wrapping Life grid with an independent reset baseline. */
public final class DefaultLifeModel implements LifeModel {
    private int[][] current;
    private int[][] lastEdited;

    public DefaultLifeModel() {
        this(new int[GRID_SIZE][GRID_SIZE], new int[GRID_SIZE][GRID_SIZE]);
    }

    /** Initializes both grids from detached copies, for loading prepared states. */
    public DefaultLifeModel(int[][] current, int[][] lastEdited) {
        this.current = validatedCopy(current);
        this.lastEdited = validatedCopy(lastEdited);
    }

    @Override
    public int[][] currentState() {
        return copy(current);
    }

    @Override
    public int[][] lastEditedState() {
        return copy(lastEdited);
    }

    @Override
    public void advance() {
        int[][] next = new int[GRID_SIZE][GRID_SIZE];
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                int neighbours = 0;
                for (int ny = Math.max(0, y - 1); ny <= Math.min(GRID_SIZE - 1, y + 1); ny++) {
                    for (int nx = Math.max(0, x - 1); nx <= Math.min(GRID_SIZE - 1, x + 1); nx++) {
                        if (nx != x || ny != y) {
                            neighbours += current[ny][nx];
                        }
                    }
                }
                next[y][x] = neighbours == 3 || (current[y][x] == 1 && neighbours == 2) ? 1 : 0;
            }
        }
        current = next;
    }

    @Override
    public void rememberCurrentState() {
        lastEdited = copy(current);
    }

    @Override
    public void reset() {
        current = copy(lastEdited);
    }

    @Override
    public void clear() {
        current = new int[GRID_SIZE][GRID_SIZE];
    }

    @Override
    public void toggleCell(int x, int y) {
        if (x < 0 || x >= GRID_SIZE || y < 0 || y >= GRID_SIZE) {
            throw new IndexOutOfBoundsException("Cell outside the 127 by 127 grid: " + x + ", " + y);
        }
        current[y][x] = 1 - current[y][x];
    }

    private static int[][] copy(int[][] cells) {
        int[][] result = new int[GRID_SIZE][];
        for (int y = 0; y < GRID_SIZE; y++) {
            result[y] = cells[y].clone();
        }
        return result;
    }

    private static int[][] validatedCopy(int[][] cells) {
        if (cells == null || cells.length != GRID_SIZE) {
            throw new IllegalArgumentException("Grid must have 127 rows");
        }
        for (int[] row : cells) {
            if (row == null || row.length != GRID_SIZE) {
                throw new IllegalArgumentException("Grid must have 127 columns");
            }
            for (int value : row) {
                if (value != 0 && value != 1) {
                    throw new IllegalArgumentException("Cell values must be 0 or 1");
                }
            }
        }
        return copy(cells);
    }
}
