package org.tbstcraft.quark.service.record;

import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.service.Service;
import org.tbstcraft.quark.util.FilePath;
import org.tbstcraft.quark.util.ObjectContainer;

import java.util.HashMap;

public interface RecordService extends Service {
    ObjectContainer<RecordService> INSTANCE = new ObjectContainer<>();

    static void init() {
        INSTANCE.set(create(FilePath.recordFolder(Quark.PLUGIN_ID)));
        INSTANCE.get().onEnable();
    }

    static void stop() {
        INSTANCE.get().onDisable();
    }

    static ServiceImplementation create(String folder) {
        return new ServiceImplementation(folder);
    }

    static RecordEntry create(String namespace, String module, String recordFormat) {
        return INSTANCE.get().createEntry(namespace, module, recordFormat);
    }


    RecordEntry createEntry(String namespace, String module, String recordFormat);

    void save();

    final class ServiceImplementation implements RecordService {
        private final HashMap<String, RecordEntry> entries = new HashMap<>();
        private final String folder;

        public ServiceImplementation(String folder) {
            this.folder = folder;
        }

        @Override
        public void onDisable() {
            for (RecordEntry entry : this.entries.values()) {
                entry.close();
            }
        }

        @Override
        public RecordEntry createEntry(String namespace, String module, String recordFormat) {
            String key = namespace + ":" + module;
            if (this.entries.containsKey(key)) {
                return this.entries.get(key);
            }
            RecordEntry entry = new SimpleRecordEntry(this.folder, namespace, module, recordFormat);
            this.entries.put(key, entry);
            return entry;
        }

        @Override
        public void save() {
            for (RecordEntry entry : this.entries.values()) {
                entry.save();
            }
        }
    }
}
