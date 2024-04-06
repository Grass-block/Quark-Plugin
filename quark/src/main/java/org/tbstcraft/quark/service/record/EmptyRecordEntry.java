package org.tbstcraft.quark.service.record;

public final class EmptyRecordEntry implements RecordEntry {
    @Override
    public void record(String str, Object... format) {
    }

    @Override
    public void save() {
    }

    @Override
    public void close() {
    }
}
