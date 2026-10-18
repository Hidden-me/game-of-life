package net.hidme.games.gameoflife.model;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.hidme.games.gameoflife.support.TestSubjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import static net.hidme.games.gameoflife.support.Grids.*;
import static net.hidme.games.gameoflife.support.Grids.Pattern.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeModelTest {
    @Test
    @DisplayName("MODEL-001 初始当前状态和上次编辑状态均为空，迁移后仍为空")
    void model001() {
        LifeModel model = TestSubjects.newModel();
        assertGridEquals(grid(O), model.currentState());
        assertGridEquals(grid(O), model.lastEditedState());
        model.advance();
        assertGridEquals(grid(O), model.currentState());
        assertGridEquals(grid(O), model.lastEditedState());
    }

    @ParameterizedTest(name = "{0} 中心初值={1}，活邻格={2}，预期={3}")
    @MethodSource("ruleCases")
    @DisplayName("MODEL-002 / MODEL-003 死活中心格与 0..8 个活邻格的 18 个组合")
    void model002And003(String id, int center, int neighbours, int expected) {
        int[][] neighbourCells = {
            cell(62, 62), cell(63, 62), cell(64, 62), cell(62, 63),
            cell(64, 63), cell(62, 64), cell(63, 64), cell(64, 64)
        };
        int[][] current = grid();
        current[63][63] = center;
        for (int i = 0; i < neighbours; i++) {
            int[] cell = neighbourCells[i];
            current[cell[1]][cell[0]] = 1;
        }
        LifeModel model = TestSubjects.modelWithState(current, grid(O));
        model.advance();
        assertEquals(expected, model.currentState()[63][63], id + " 中心格状态");
    }

    static Stream<Arguments> ruleCases() {
        int[] deadExpected = {0, 0, 0, 1, 0, 0, 0, 0, 0};
        int[] aliveExpected = {0, 0, 1, 1, 0, 0, 0, 0, 0};
        return Stream.concat(
            IntStream.rangeClosed(0, 8).mapToObj(n -> Arguments.of("MODEL-002", 0, n, deadExpected[n])),
            IntStream.rangeClosed(0, 8).mapToObj(n -> Arguments.of("MODEL-003", 1, n, aliveExpected[n])));
    }

    @Test
    @DisplayName("MODEL-004 水平三连变为竖直三连，所有单元格同时迁移")
    void model004() {
        assertNextState(grid(A), grid(B));
    }

    @Test
    @DisplayName("MODEL-005 竖直三连变为水平三连")
    void model005() {
        assertNextState(grid(B), grid(A));
    }

    @Test
    @DisplayName("MODEL-006 稳定方块不变且外围不额外出生")
    void model006() {
        assertNextState(grid(S), grid(S));
    }

    @Test
    @DisplayName("MODEL-007 三个对角邻格令中心出生，原有孤立格死亡")
    void model007() {
        assertNextState(grid(cell(62, 62), cell(64, 62), cell(62, 64)), grid(cell(63, 63)));
    }

    @ParameterizedTest(name = "MODEL-008 孤立格 ({0},{1})")
    @CsvSource({"63,63", "0,0", "126,0", "0,126", "126,126",
        "63,0", "63,126", "0,63", "126,63"})
    @DisplayName("MODEL-008 中心、四角、四边中点的孤立格均死亡")
    void model008(int x, int y) {
        assertNextState(grid(cell(x, y)), grid(O));
    }

    @ParameterizedTest(name = "MODEL-009 水平镜像={0}，竖直镜像={1}")
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    @DisplayName("MODEL-009 四个角均能由三个有效邻格出生")
    void model009(boolean mirrorX, boolean mirrorY) {
        int[][] initial = grid(mirror(cell(1, 0), mirrorX, mirrorY),
            mirror(cell(0, 1), mirrorX, mirrorY), mirror(cell(1, 1), mirrorX, mirrorY));
        int[][] expected = grid(mirror(cell(0, 0), mirrorX, mirrorY),
            mirror(cell(1, 0), mirrorX, mirrorY), mirror(cell(0, 1), mirrorX, mirrorY),
            mirror(cell(1, 1), mirrorX, mirrorY));
        assertNextState(initial, expected);
    }

    @ParameterizedTest(name = "MODEL-010 {0}")
    @MethodSource("edgeCases")
    @DisplayName("MODEL-010 四条边的邻居不绕到对侧")
    void model010(String edge, int[][] initial, int[][] expected) {
        assertNextState(initial, expected);
    }

    static Stream<Arguments> edgeCases() {
        return Stream.of(
            Arguments.of("顶部", grid(cell(62, 0), cell(63, 0), cell(64, 0)),
                grid(cell(63, 0), cell(63, 1))),
            Arguments.of("底部", grid(cell(62, 126), cell(63, 126), cell(64, 126)),
                grid(cell(63, 125), cell(63, 126))),
            Arguments.of("左侧", grid(cell(0, 62), cell(0, 63), cell(0, 64)),
                grid(cell(0, 63), cell(1, 63))),
            Arguments.of("右侧", grid(cell(126, 62), cell(126, 63), cell(126, 64)),
                grid(cell(125, 63), cell(126, 63))));
    }

    @Test
    @DisplayName("MODEL-011 不通过对角方向跨边界计算邻居")
    void model011() {
        assertNextState(grid(cell(126, 126), cell(126, 0), cell(0, 126)), grid(O));
    }

    @Test
    @DisplayName("MODEL-012 全活网格迁移后仅四角存活，网格大小和二值约束不变")
    void model012() {
        assertNextState(fullGrid(), grid(cell(0, 0), cell(126, 0), cell(0, 126), cell(126, 126)));
    }

    @Test
    @DisplayName("MODEL-013 模型迁移不覆盖上次编辑状态")
    void model013() {
        LifeModel model = TestSubjects.modelWithState(grid(A), grid(S));
        assertGridEquals(grid(A), model.currentState());
        assertGridEquals(grid(S), model.lastEditedState());
        model.advance();
        assertGridEquals(grid(B), model.currentState());
        assertGridEquals(grid(S), model.lastEditedState());
    }

    private static void assertNextState(int[][] initial, int[][] expected) {
        LifeModel model = TestSubjects.modelWithState(initial, grid(O));
        assertGridEquals(initial, model.currentState());
        model.advance();
        assertGridEquals(expected, model.currentState());
    }

    private static int[] mirror(int[] cell, boolean mirrorX, boolean mirrorY) {
        return cell(mirrorX ? 126 - cell[0] : cell[0], mirrorY ? 126 - cell[1] : cell[1]);
    }
}
