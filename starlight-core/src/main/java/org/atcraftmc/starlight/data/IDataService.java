package org.atcraftmc.starlight.data;

import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.data.storage.StorageContext;
import org.atcraftmc.starlight.framework.SLService;

public interface IDataService extends SLService, StorageContext {
    DataEntry getData(String player);

    void saveData(String player);

    int getEntryCount();
}
