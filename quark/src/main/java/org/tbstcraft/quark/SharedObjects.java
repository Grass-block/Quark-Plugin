package org.tbstcraft.quark;

import org.tbstcraft.quark.util.FilePath;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public interface SharedObjects {
    Logger LOGGER = LogManager.getLogManager().getLogger("Quark");
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Properties GLOBAL_VARS = new Properties();

    static void loadGlobalVars() {
        try {
            SharedObjects.GLOBAL_VARS.clear();
            SharedObjects.GLOBAL_VARS.load(new FileInputStream(FilePath.tryReleaseAndGetFile(
                    "/global_vars.properties",
                    FilePath.pluginFolder() + "/global_vars.properties")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
