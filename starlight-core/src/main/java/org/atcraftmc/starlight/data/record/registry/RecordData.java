package org.atcraftmc.starlight.data.record.registry;

public final class RecordData {
    private final String id;
    private final long timestamp;
    private final Object[] args;

    public RecordData(String id, long time, Object... args) {
        this.id = id;
        this.timestamp = time;
        this.args = args;
    }


    public String id() {
        return id;
    }

    public long timestamp() {
        return timestamp;
    }

    public Object[] args() {
        return args;
    }
}
