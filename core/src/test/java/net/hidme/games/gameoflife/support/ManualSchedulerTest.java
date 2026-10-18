package net.hidme.games.gameoflife.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.hidme.games.gameoflife.session.TaskScheduler.ScheduledTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies the timing substitute on which the session's boundary assertions rely. */
class ManualSchedulerTest {
    @Test
    void firstCallbackIsDelayedAndEveryDueIntervalIsExecuted() {
        ManualScheduler scheduler = new ManualScheduler();
        AtomicInteger calls = new AtomicInteger();
        scheduler.scheduleAtFixedRate(500, calls::incrementAndGet);
        assertEquals(0, calls.get());
        scheduler.advanceBy(499);
        assertEquals(0, calls.get());
        scheduler.advanceBy(1);
        assertEquals(1, calls.get());
        scheduler.advanceBy(1000);
        assertEquals(3, calls.get());
    }

    @Test
    void cancellingAnOldTaskDoesNotChangeTheNewTasksDeadline() {
        ManualScheduler scheduler = new ManualScheduler();
        AtomicInteger calls = new AtomicInteger();
        ScheduledTask old = scheduler.scheduleAtFixedRate(500, calls::incrementAndGet);
        scheduler.advanceBy(499);
        old.cancel();
        old.cancel();
        scheduler.scheduleAtFixedRate(500, calls::incrementAndGet);
        scheduler.advanceBy(1);
        assertEquals(0, calls.get());
        scheduler.advanceBy(498);
        assertEquals(0, calls.get());
        scheduler.advanceBy(1);
        assertEquals(1, calls.get());
    }

    @Test
    void cancellationInsideCallbackStopsRemainingDueCallbacks() {
        ManualScheduler scheduler = new ManualScheduler();
        AtomicInteger calls = new AtomicInteger();
        ScheduledTask[] task = new ScheduledTask[1];
        task[0] = scheduler.scheduleAtFixedRate(500, () -> {
            calls.incrementAndGet();
            task[0].cancel();
        });
        scheduler.advanceBy(1500);
        assertEquals(1, calls.get());
    }

    @Test
    void dueCallbacksRunInDeadlineOrderEvenAcrossALargeTimeAdvance() {
        ManualScheduler scheduler = new ManualScheduler();
        List<String> events = new ArrayList<>();
        scheduler.scheduleAtFixedRate(500, () -> events.add("500"));
        scheduler.scheduleAtFixedRate(750, () -> events.add("750"));
        scheduler.advanceBy(1500);
        assertEquals(List.of("500", "750", "500", "500", "750"), events);
    }
}
