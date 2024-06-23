package org.tbstcraft.quark.framework.record;

public interface RecordEntry {
    void close();

    void open();

    void addLine(Object... components);
}
