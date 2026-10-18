package net.hidme.games.gameoflife.session;

import java.util.Comparator;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.LongSupplier;

/** Uses a monotonic clock, dispatching due callbacks only when the game thread updates. */
public final class FrameTaskScheduler implements TaskScheduler {
    private final LongSupplier nanoClock;
    private final PriorityQueue<Entry> tasks = new PriorityQueue<>(
        Comparator.comparingLong((Entry entry) -> entry.dueNanos)
            .thenComparingLong(entry -> entry.sequence));
    private long nextSequence;

    public FrameTaskScheduler() {
        this(System::nanoTime);
    }

    public FrameTaskScheduler(LongSupplier nanoClock) {
        this.nanoClock = Objects.requireNonNull(nanoClock);
    }

    @Override
    public ScheduledTask scheduleAtFixedRate(long intervalMillis, Runnable callback) {
        if (intervalMillis <= 0) {
            throw new IllegalArgumentException("Interval must be positive");
        }
        long intervalNanos = Math.multiplyExact(intervalMillis, 1_000_000L);
        Entry entry = new Entry(nanoClock.getAsLong() + intervalNanos,
            intervalNanos, nextSequence++, Objects.requireNonNull(callback));
        tasks.add(entry);
        return () -> {
            entry.cancelled = true;
            tasks.remove(entry);
        };
    }

    /** Runs every elapsed interval, in deadline order; queries never drive this clock. */
    public void update() {
        long now = nanoClock.getAsLong();
        while (!tasks.isEmpty() && tasks.peek().dueNanos <= now) {
            Entry entry = tasks.remove();
            entry.callback.run();
            if (!entry.cancelled) {
                entry.dueNanos += entry.intervalNanos;
                tasks.add(entry);
            }
        }
    }

    private static final class Entry {
        private long dueNanos;
        private final long intervalNanos;
        private final long sequence;
        private final Runnable callback;
        private boolean cancelled;

        private Entry(long dueNanos, long intervalNanos, long sequence, Runnable callback) {
            this.dueNanos = dueNanos;
            this.intervalNanos = intervalNanos;
            this.sequence = sequence;
            this.callback = callback;
        }
    }
}
