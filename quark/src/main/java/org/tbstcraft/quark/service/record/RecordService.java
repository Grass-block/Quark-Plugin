package org.tbstcraft.quark.service.record;

import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.service.ServiceImplementation;
import org.tbstcraft.quark.util.container.ObjectContainer;

import java.util.HashMap;

@ServiceImplementation(impl = RecordService.ServiceImplementation.class)
public interface RecordService extends Service {
    ObjectContainer<RecordService> INSTANCE = new ObjectContainer<>();

    static RecordService create() {
        return new ServiceImplementation();
    }

    static RecordEntry create(String id, String[] recordFormat) {
        RecordEntry entry = INSTANCE.get().createEntry(id, recordFormat);
        entry.open();
        return entry;
    }

    RecordEntry createEntry(String id, String[] recordFormat);

    void save();

    final class ServiceImplementation implements RecordService {
        private final HashMap<String, RecordEntry> entries = new HashMap<>();

        @Override
        public void onDisable() {
            for (RecordEntry entry : this.entries.values()) {
                entry.close();
            }
        }

        @Override
        public RecordEntry createEntry(String id, String[] recordFormat) {
            if (this.entries.containsKey(id)) {
                return this.entries.get(id);
            }
            RecordEntry entry = new SimpleRecordEntry(id, recordFormat);
            this.entries.put(id, entry);
            return entry;
        }

        @Override
        public void save() {
            for (RecordEntry entry : this.entries.values()) {
                entry.close();
            }
        }
    }
}
