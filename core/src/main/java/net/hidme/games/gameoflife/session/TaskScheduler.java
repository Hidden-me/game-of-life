package net.hidme.games.gameoflife.session;

/**
 * Injectable timing boundary. Callbacks and session commands execute serially
 * (on the game thread in a libGDX adapter), never concurrently.
 */
public interface TaskScheduler {
    /**
     * Schedules at a fixed rate relative to the current time. The first callback
     * occurs after one interval, not immediately. The interval must be positive.
     */
    ScheduledTask scheduleAtFixedRate(long intervalMillis, Runnable callback);

    @FunctionalInterface
    interface ScheduledTask {
        /** Idempotently prevents all future callbacks, including the next due callback. */
        void cancel();
    }
}
