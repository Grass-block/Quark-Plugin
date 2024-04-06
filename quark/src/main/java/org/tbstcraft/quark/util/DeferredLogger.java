package org.tbstcraft.quark.util;

import java.util.ArrayDeque;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class DeferredLogger extends Logger {
    private final ArrayDeque<LogRecord> queue = new ArrayDeque<>();
    private final Logger delegated;

    public DeferredLogger(Logger logger) {
        super(logger.getName(), logger.getResourceBundleName());
        this.delegated = logger;
    }

    @Override
    public void log(LogRecord record) {
        this.queue.add(record);
    }

    public void batch() {
        while (!this.queue.isEmpty()) {
            this.delegated.log(this.queue.poll());
        }
    }
}
