package net.hidme.games.gameoflife.support;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class Grids {
    // Kept independent of the implementation's constant to check STT-001.
    public static final int SIZE = 127;

    public enum Pattern {
        O, A, B, S
    }

    private Grids() {
    }

    public static int[][] grid(Pattern pattern) {
        return switch (pattern) {
            case O -> grid();
            case A -> grid(cell(62, 63), cell(63, 63), cell(64, 63));
            case B -> grid(cell(63, 62), cell(63, 63), cell(63, 64));
            case S -> grid(cell(10, 10), cell(11, 10), cell(10, 11), cell(11, 11));
        };
    }

    public static int[] cell(int x, int y) {
        return new int[]{x, y};
    }

    public static int[][] grid(int[]... liveCells) {
        int[][] result = new int[SIZE][SIZE];
        for (int[] cell : liveCells) {
            result[cell[1]][cell[0]] = 1;
        }
        return result;
    }

    public static int[][] fullGrid() {
        int[][] result = grid();
        for (int[] row : result) {
            Arrays.fill(row, 1);
        }
        return result;
    }

    public static int[][] copy(int[][] source) {
        return Arrays.stream(source).map(int[]::clone).toArray(int[][]::new);
    }

    public static void assertGridEquals(int[][] expected, int[][] actual) {
        assertNotNull(actual, "网格不能为空");
        assertEquals(SIZE, actual.length, "网格高度应为 127");
        for (int y = 0; y < SIZE; y++) {
            assertNotNull(actual[y], "网格行不能为空，y=" + y);
            assertEquals(SIZE, actual[y].length, "网格宽度应为 127，y=" + y);
            assertArrayEquals(expected[y], actual[y], "完整网格不匹配，y=" + y);
        }
    }
}
