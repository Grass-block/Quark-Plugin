package org.atcraftmc.starlight.data.record;

public interface RecordEntry {
    void close();

    void open();

    void addLine(Object... components);
}
