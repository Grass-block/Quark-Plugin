package org.atcraftmc.starlight.data.storage;

import me.gb2022.commons.nbt.NBTTagCompound;
import org.atcraftmc.starlight.core.data.FlexibleMapService;
import org.atcraftmc.starlight.core.data.flex.NBTColumn;

import java.util.UUID;

public class DataEntry extends StorageTable {
    public DataEntry(NBTTagCompound dom, DataReference reference) {
        super(dom, reference);
    }

    public static final class LegacyDataEntry extends DataEntry {
        private final String id;
        private final StorageContext context;

        public LegacyDataEntry(NBTTagCompound tag, StorageContext context, String id) {
            super(tag, null);
            setReference(this);
            this.id = id;
            this.context = context;
        }

        public String getId() {
            return id;
        }

        @Override
        public void save() {
            this.context.save(this);
        }
    }

    public static final class DBDataEntry extends DataEntry {
        private NBTColumn column;
        private FlexibleMapService service;
        private UUID uuid;

        public DBDataEntry(NBTTagCompound dom, NBTColumn column, FlexibleMapService service, UUID uuid) {
            super(dom, null);
            setReference(this);
            this.column = column;
            this.service = service;
            this.uuid = uuid;
        }

        @Override
        public void save() {
            this.column.set(this.service, this.uuid, this);
        }

        public void setContext(NBTColumn nbtColumn, FlexibleMapService ds, UUID uuid) {
            this.column = nbtColumn;
            this.service = ds;
            this.uuid = uuid;
        }
    }
}
