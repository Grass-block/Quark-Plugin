package org.tbstcraft.quark.framework.record;

import org.tbstcraft.quark.framework.service.QuarkService;
import org.tbstcraft.quark.framework.service.Service;
import org.tbstcraft.quark.framework.service.ServiceHolder;
import org.tbstcraft.quark.framework.service.ServiceInject;

import java.util.HashMap;

@QuarkService(id = "record", impl = RecordService.ServiceImplementation.class)
public interface RecordService extends Service {
    @ServiceInject
    ServiceHolder<RecordService> INSTANCE = new ServiceHolder<>();

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
