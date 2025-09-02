package org.atcraftmc.starlight.data;

import org.atcraftmc.starlight.data.storage.DataEntry;
import org.atcraftmc.starlight.data.storage.StorageContext;
import org.atcraftmc.starlight.framework.service.Service;

public interface IDataService extends Service, StorageContext {
    DataEntry getData(String player);

    void saveData(String player);

    int getEntryCount();
}
