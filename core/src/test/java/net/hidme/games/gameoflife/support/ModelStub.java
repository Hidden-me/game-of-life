package net.hidme.games.gameoflife.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.hidme.games.gameoflife.model.LifeModel;

import static net.hidme.games.gameoflife.support.Grids.Pattern.*;
import static net.hidme.games.gameoflife.support.Grids.copy;
import static net.hidme.games.gameoflife.support.Grids.grid;

/** Scripted session dependency; deliberately does not implement Life's rules. */
public final class ModelStub implements LifeModel {
    private int[][] current;
    private int[][] lastEdited;
    private final List<int[][]> transitionInputs = new ArrayList<>();

    public ModelStub(int[][] current, int[][] lastEdited) {
        this.current = copy(current);
        this.lastEdited = copy(lastEdited);
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
        transitionInputs.add(copy(current));
        if (Arrays.deepEquals(current, grid(A))) {
            overwrite(grid(B));
        } else if (Arrays.deepEquals(current, grid(B))) {
            overwrite(grid(A));
        } else if (!Arrays.deepEquals(current, grid(O)) && !Arrays.deepEquals(current, grid(S))) {
            throw new AssertionError("模型替身只支持测试文档中 A↔B、O→O、S→S 的脚本迁移。");
        }
    }

    @Override
    public void rememberCurrentState() {
        lastEdited = copy(current);
    }

    @Override
    public void reset() {
        overwrite(lastEdited);
    }

    @Override
    public void clear() {
        overwrite(grid(O));
    }

    public int transitionCount() {
        return transitionInputs.size();
    }

    public int[][] transitionInput(int index) {
        return copy(transitionInputs.get(index));
    }

    private void overwrite(int[][] source) {
        for (int y = 0; y < current.length; y++) {
            System.arraycopy(source[y], 0, current[y], 0, current[y].length);
        }
    }
}
