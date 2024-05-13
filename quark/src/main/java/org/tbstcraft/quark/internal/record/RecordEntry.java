package org.tbstcraft.quark.internal.record;

public interface RecordEntry {
    void close();

    void open();

    void addLine(Object... components);
}
