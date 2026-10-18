package net.hidme.games.gameoflife.support;

import java.util.Comparator;
import java.util.PriorityQueue;

import net.hidme.games.gameoflife.session.TaskScheduler;

/** A manual clock and scheduler: advances to each deadline and runs due tasks. */
public final class ManualScheduler implements TaskScheduler {
    private final PriorityQueue<Entry> tasks = new PriorityQueue<>(
        Comparator.comparingLong((Entry entry) -> entry.dueMillis)
            .thenComparingLong(entry -> entry.sequence));
    private long nowMillis;
    private long nextSequence;

    @Override
    public ScheduledTask scheduleAtFixedRate(long intervalMillis, Runnable callback) {
        if (intervalMillis <= 0) {
            throw new IllegalArgumentException("intervalMillis must be positive");
        }
        Entry entry = new Entry(nowMillis + intervalMillis, intervalMillis, nextSequence++, callback);
        tasks.add(entry);
        return () -> entry.cancelled = true;
    }

    public void advanceBy(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Cannot move time backwards");
        }
        long target = Math.addExact(nowMillis, millis);
        while (!tasks.isEmpty() && tasks.peek().dueMillis <= target) {
            Entry entry = tasks.remove();
            if (entry.cancelled) {
                continue;
            }
            nowMillis = entry.dueMillis;
            entry.callback.run();
            if (!entry.cancelled) {
                entry.dueMillis += entry.intervalMillis;
                tasks.add(entry);
            }
        }
        nowMillis = target;
    }

    private static final class Entry {
        private long dueMillis;
        private final long intervalMillis;
        private final long sequence;
        private final Runnable callback;
        private boolean cancelled;

        private Entry(long dueMillis, long intervalMillis, long sequence, Runnable callback) {
            this.dueMillis = dueMillis;
            this.intervalMillis = intervalMillis;
            this.sequence = sequence;
            this.callback = callback;
        }
    }
}
