package org.tbstcraft.quark.service.record;

public interface RecordEntry {
    void close();

    void open();

    void addLine(Object... components);
}
