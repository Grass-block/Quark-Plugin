package org.tbstcraft.quark.record;

import org.tbstcraft.quark.SharedObjects;

import java.util.Date;

public class RecordEntry {
    public void record(String str, Object...format) {
        String date = SharedObjects.DATE_FORMAT.format(new Date());
    }
}
