package net.hidme.games.gameoflife.session;

import net.hidme.games.gameoflife.support.Grids.Pattern;
import net.hidme.games.gameoflife.support.ManualScheduler;
import net.hidme.games.gameoflife.support.ModelStub;
import net.hidme.games.gameoflife.support.TestSubjects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static net.hidme.games.gameoflife.session.SessionMode.*;
import static net.hidme.games.gameoflife.support.Grids.*;
import static net.hidme.games.gameoflife.support.Grids.Pattern.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameSessionTest {
    @Test
    @DisplayName("SNAP-001 新建会话为空网格、编辑模式")
    void snap001() {
        Fixture f = fixture(O);
        expect(f, O, EDITING, 0);
    }

    @Test
    @DisplayName("SNAP-002 查询完整当前网格且不迁移")
    void snap002() {
        Fixture f = fixture(A);
        expect(f, A, EDITING, 0);
    }

    @Test
    @DisplayName("SNAP-003 运行中查询反映最新状态和模式")
    void snap003() {
        Fixture f = fixture(A);
        f.session.start();
        expect(f, A, RUNNING, 0);
        f.scheduler.advanceBy(500);
        expect(f, B, RUNNING, 1);
    }

    @Test
    @DisplayName("SNAP-004 修改快照的网格不影响源状态或其他快照")
    void snap004() {
        Fixture f = fixture(A);
        SessionSnapshot first = f.session.snapshot();
        SessionSnapshot second = f.session.snapshot();
        first.cells()[63][62] = 0;
        first.cells()[0][0] = 1;
        assertGridEquals(grid(A), second.cells());
        expect(f, A, EDITING, 0);
        assertGridEquals(grid(A), f.model.currentState());
    }

    @Test
    @DisplayName("SNAP-005 旧快照不随步进改变")
    void snap005() {
        Fixture f = fixture(A);
        SessionSnapshot previous = f.session.snapshot();
        f.session.step();
        expect(f, B, EDITING, 1);
        assertGridEquals(grid(A), previous.cells());
        assertEquals(EDITING, previous.mode());
    }

    @Test
    @DisplayName("SNAP-006 连续查询不覆盖上次编辑状态")
    void snap006() {
        Fixture f = fixture(B, A);
        expect(f, B, EDITING, 0);
        expect(f, B, EDITING, 0);
        f.session.reset();
        expect(f, A, EDITING, 0);
    }

    @Test
    @DisplayName("SNAP-007 查询不改变自动步进时刻")
    void snap007() {
        Fixture f = runningFor(A, 250);
        expect(f, A, RUNNING, 0);
        expect(f, A, RUNNING, 0);
        f.scheduler.advanceBy(250);
        expect(f, B, RUNNING, 1);
    }

    @Test
    @DisplayName("STEP-001 编辑模式立即步进一次且不启动定时任务")
    void step001() {
        Fixture f = fixture(A);
        f.session.step();
        expect(f, B, EDITING, 1);
        f.scheduler.advanceBy(1000);
        expect(f, B, EDITING, 1);
    }

    @Test
    @DisplayName("STEP-002 连续步进使用最新状态")
    void step002() {
        Fixture f = fixture(A);
        f.session.step();
        expect(f, B, EDITING, 1);
        f.session.step();
        expect(f, A, EDITING, 2);
        assertGridEquals(grid(A), f.model.transitionInput(0));
        assertGridEquals(grid(B), f.model.transitionInput(1));
    }

    @Test
    @DisplayName("STEP-003 运行中忽略手动步进且不推迟自动步进")
    void step003() {
        Fixture f = runningFor(A, 250);
        f.session.step();
        expect(f, A, RUNNING, 0);
        f.scheduler.advanceBy(250);
        expect(f, B, RUNNING, 1);
    }

    @Test
    @DisplayName("STEP-004 步进结果成为新的重置基准")
    void step004() {
        Fixture f = fixture(A, S);
        f.session.step();
        expect(f, B, EDITING, 1);
        f.session.reset();
        expect(f, B, EDITING, 1);
    }

    @ParameterizedTest(name = "STEP-005 {0}")
    @EnumSource(value = Pattern.class, names = {"O", "S"})
    @DisplayName("STEP-005 图案不变时也调用一次模型迁移")
    void step005(Pattern pattern) {
        Fixture f = fixture(pattern);
        f.session.step();
        expect(f, pattern, EDITING, 1);
    }

    @Test
    @DisplayName("START-001 运行指令和前 499ms 不迁移")
    void start001() {
        Fixture f = fixture(A);
        f.session.start();
        expect(f, A, RUNNING, 0);
        f.scheduler.advanceBy(499);
        expect(f, A, RUNNING, 0);
    }

    @Test
    @DisplayName("START-002 每满 500ms 恰好迁移一次")
    void start002() {
        Fixture f = fixture(A);
        f.session.start();
        f.scheduler.advanceBy(499);
        expect(f, A, RUNNING, 0);
        f.scheduler.advanceBy(1);
        expect(f, B, RUNNING, 1);
        f.scheduler.advanceBy(499);
        expect(f, B, RUNNING, 1);
        f.scheduler.advanceBy(1);
        expect(f, A, RUNNING, 2);
        f.scheduler.advanceBy(500);
        expect(f, B, RUNNING, 3);
    }

    @Test
    @DisplayName("START-003 重复运行既不重计时也不增加任务或覆盖重置基准")
    void start003() {
        Fixture f = runningFor(A, 250);
        f.session.start();
        f.scheduler.advanceBy(250);
        expect(f, B, RUNNING, 1);
        f.scheduler.advanceBy(250);
        expect(f, B, RUNNING, 1);
        f.scheduler.advanceBy(250);
        expect(f, A, RUNNING, 2);
        f.scheduler.advanceBy(500);
        expect(f, B, RUNNING, 3);
        f.session.start();
        f.session.reset();
        expect(f, A, EDITING, 3);
    }

    @Test
    @DisplayName("START-004 进入运行模式前保存当前状态")
    void start004() {
        Fixture f = fixture(B, S);
        f.session.start();
        f.scheduler.advanceBy(500);
        expect(f, A, RUNNING, 1);
        f.session.reset();
        expect(f, B, EDITING, 1);
    }

    @Test
    @DisplayName("START-005 再次运行重新计时并更新重置基准")
    void start005() {
        Fixture f = runningFor(A, 500);
        f.session.pause();
        expect(f, B, EDITING, 1);
        f.scheduler.advanceBy(200);
        f.session.start();
        f.scheduler.advanceBy(499);
        expect(f, B, RUNNING, 1);
        f.scheduler.advanceBy(1);
        expect(f, A, RUNNING, 2);
        f.session.reset();
        expect(f, B, EDITING, 2);
    }

    @ParameterizedTest(name = "START-006 {0}")
    @EnumSource(value = Pattern.class, names = {"O", "S"})
    @DisplayName("START-006 空网格和稳定图案不自行暂停")
    void start006(Pattern pattern) {
        Fixture f = runningFor(pattern, 1500);
        expect(f, pattern, RUNNING, 3);
    }

    @Test
    @DisplayName("PAUSE-001 暂停保留当前状态并停止迁移")
    void pause001() {
        Fixture f = runningFor(A, 500);
        f.session.pause();
        expect(f, B, EDITING, 1);
        f.scheduler.advanceBy(1500);
        expect(f, B, EDITING, 1);
    }

    @Test
    @DisplayName("PAUSE-002 到期前暂停取消下一次迁移")
    void pause002() {
        Fixture f = runningFor(A, 499);
        f.session.pause();
        expect(f, A, EDITING, 0);
        f.scheduler.advanceBy(1);
        expect(f, A, EDITING, 0);
        f.scheduler.advanceBy(1000);
        expect(f, A, EDITING, 0);
    }

    @Test
    @DisplayName("PAUSE-003 编辑模式下暂停不覆盖上次编辑状态")
    void pause003() {
        Fixture f = fixture(B, A);
        f.session.pause();
        expect(f, B, EDITING, 0);
        f.scheduler.advanceBy(1000);
        expect(f, B, EDITING, 0);
        f.session.reset();
        expect(f, A, EDITING, 0);
    }

    @Test
    @DisplayName("PAUSE-004 重复暂停后仍可手动步进")
    void pause004() {
        Fixture f = runningFor(A, 500);
        f.session.pause();
        f.session.pause();
        expect(f, B, EDITING, 1);
        f.session.step();
        expect(f, A, EDITING, 2);
    }

    @Test
    @DisplayName("PAUSE-005 立即重新运行不复用旧到期时刻")
    void pause005() {
        Fixture f = runningFor(A, 499);
        f.session.pause();
        f.session.start();
        expectFreshRun(f, A, B);
    }

    @Test
    @DisplayName("RESET-001 新建会话重置仍为空网格")
    void reset001() {
        Fixture f = fixture(O);
        f.session.reset();
        expect(f, O, EDITING, 0);
    }

    @Test
    @DisplayName("RESET-002 重复重置恢复整个网格且不迁移")
    void reset002() {
        Fixture f = fixture(B, A);
        f.session.reset();
        expect(f, A, EDITING, 0);
        f.session.reset();
        expect(f, A, EDITING, 0);
    }

    @Test
    @DisplayName("RESET-003 运行中重置恢复基准并停止自动步进")
    void reset003() {
        Fixture f = runningFor(A, 500);
        f.session.reset();
        expect(f, A, EDITING, 1);
        f.scheduler.advanceBy(1500);
        expect(f, A, EDITING, 1);
    }

    @Test
    @DisplayName("RESET-004 暂停不污染运行前保存的状态")
    void reset004() {
        Fixture f = runningFor(A, 500);
        f.session.pause();
        expect(f, B, EDITING, 1);
        f.session.reset();
        expect(f, A, EDITING, 1);
    }

    @Test
    @DisplayName("RESET-005 重置后重新运行不触发旧任务")
    void reset005() {
        Fixture f = runningFor(A, 499);
        f.session.reset();
        expect(f, A, EDITING, 0);
        f.session.start();
        expectFreshRun(f, A, B);
    }

    @Test
    @DisplayName("RESET-006 自动迁移不污染基准，编辑步进会更新基准")
    void reset006() {
        Fixture f = runningFor(A, 1500);
        expect(f, B, RUNNING, 3);
        f.session.reset();
        expect(f, A, EDITING, 3);
        f.session.step();
        expect(f, B, EDITING, 4);
        f.session.reset();
        expect(f, B, EDITING, 4);
    }

    @Test
    @DisplayName("CLEAR-001 清空四角及中心在内的全部网格")
    void clear001() {
        Fixture f = fixture(grid(cell(0, 0), cell(126, 0), cell(0, 126), cell(126, 126), cell(63, 63)),
            grid(A));
        f.session.clear();
        expect(f, O, EDITING, 0);
    }

    @Test
    @DisplayName("CLEAR-002 运行中清空并停止自动迁移")
    void clear002() {
        Fixture f = runningFor(A, 500);
        f.session.clear();
        expect(f, O, EDITING, 1);
        f.scheduler.advanceBy(1500);
        expect(f, O, EDITING, 1);
    }

    @Test
    @DisplayName("CLEAR-003 编辑模式清空后重置仍为空网格")
    void clear003() {
        Fixture f = fixture(B, A);
        f.session.clear();
        expect(f, O, EDITING, 0);
        f.session.reset();
        expect(f, O, EDITING, 0);
    }

    @Test
    @DisplayName("CLEAR-004 运行中清空先退出运行并记录空网格")
    void clear004() {
        Fixture f = runningFor(A, 500);
        f.session.clear();
        expect(f, O, EDITING, 1);
        f.session.reset();
        expect(f, O, EDITING, 1);
    }

    @Test
    @DisplayName("CLEAR-005 重复清空不创建迁移或定时任务")
    void clear005() {
        Fixture f = fixture(O);
        f.session.clear();
        expect(f, O, EDITING, 0);
        f.session.clear();
        expect(f, O, EDITING, 0);
        f.session.reset();
        expect(f, O, EDITING, 0);
        f.scheduler.advanceBy(1500);
        expect(f, O, EDITING, 0);
    }

    @Test
    @DisplayName("CLEAR-006 清空后重新运行从新时刻开始计时")
    void clear006() {
        Fixture f = runningFor(A, 499);
        f.session.clear();
        expect(f, O, EDITING, 0);
        f.session.start();
        expectFreshRun(f, O, O);
        assertGridEquals(grid(O), f.model.transitionInput(0));
    }

    /** Checks both the old deadline and the new run's 499/500ms boundary. */
    private static void expectFreshRun(Fixture f, Pattern before, Pattern after) {
        f.scheduler.advanceBy(1);
        expect(f, before, RUNNING, 0);
        f.scheduler.advanceBy(498);
        expect(f, before, RUNNING, 0);
        f.scheduler.advanceBy(1);
        expect(f, after, RUNNING, 1);
    }

    private static Fixture runningFor(Pattern pattern, long millis) {
        Fixture f = fixture(pattern);
        f.session.start();
        f.scheduler.advanceBy(millis);
        return f;
    }

    private static Fixture fixture(Pattern current) {
        return fixture(current, current);
    }

    private static Fixture fixture(Pattern current, Pattern lastEdited) {
        return fixture(grid(current), grid(lastEdited));
    }

    private static Fixture fixture(int[][] current, int[][] lastEdited) {
        ModelStub model = new ModelStub(current, lastEdited);
        ManualScheduler scheduler = new ManualScheduler();
        return new Fixture(TestSubjects.newSession(model, scheduler), model, scheduler);
    }

    private static void expect(Fixture f, Pattern pattern, SessionMode mode, int transitions) {
        SessionSnapshot snapshot = f.session.snapshot();
        assertGridEquals(grid(pattern), snapshot.cells());
        assertEquals(mode, snapshot.mode(), "会话模式");
        assertEquals(transitions, f.model.transitionCount(), "模型状态迁移调用次数");
    }

    private record Fixture(GameSession session, ModelStub model, ManualScheduler scheduler) {
    }
}
