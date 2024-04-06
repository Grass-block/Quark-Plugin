package org.tbstcraft.quark.service.record;

public interface RecordEntry {
    void record(String str, Object... format);

    void save();

    void close();
}
