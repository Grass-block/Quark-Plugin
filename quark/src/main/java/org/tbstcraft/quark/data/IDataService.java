package org.tbstcraft.quark.data;

import org.tbstcraft.quark.data.storage.DataEntry;
import org.tbstcraft.quark.data.storage.StorageContext;
import org.tbstcraft.quark.framework.service.Service;

public interface IDataService extends Service, StorageContext {
    DataEntry getData(String player);

    void saveData(String player);

    int getEntryCount();
}
