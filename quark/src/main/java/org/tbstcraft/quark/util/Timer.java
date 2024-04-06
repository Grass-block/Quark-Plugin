package org.tbstcraft.quark.util;

import java.util.HashMap;

public interface Timer {
    HashMap<Thread, Timer> INSTANCES = new HashMap<>();

    @SuppressWarnings("UnusedReturnValue")
    static long restartTiming() {
        return getTimer().restart();
    }

    static long passedTime() {
        return getTimer().passed();
    }

    static Timer getTimer() {
        Thread t = Thread.currentThread();
        if (!INSTANCES.containsKey(t)) {
            INSTANCES.put(t, new TimerInstance());
        }
        return INSTANCES.get(t);
    }

    long restart();

    long passed();

    class TimerInstance implements Timer {
        private long start = -1L;

        @Override
        public long restart() {
            long now = System.currentTimeMillis();
            long passed = now - this.start;
            this.start = now;
            return passed;
        }

        @Override
        public long passed() {
            return System.currentTimeMillis() - this.start;
        }
    }
}
