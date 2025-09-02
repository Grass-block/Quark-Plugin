package org.atcraftmc.starlight.framework.service.providing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.packages.IPackage;
import org.atcraftmc.starlight.framework.service.Service;
import org.atcraftmc.starlight.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public final class JsonServiceRegistry extends ServiceRegistry {
    private final JsonObject json;

    public JsonServiceRegistry(IPackage pkg, JsonObject json) {
        super(pkg);
        this.json = json;
        if (this.json == null) {
            throw new RuntimeException("illegal package registry!");
        }
        this.create(this.getServices());
    }

    public static JsonServiceRegistry create(IPackage pkg, String path) {
        try {
            InputStream stream = pkg.getClass().getResourceAsStream(path);
            if (stream == null) {
                Starlight.instance().getLogger().warning("failed to load package descriptor.");
                return null;
            }
            stream.close();
            return new JsonServiceRegistry(pkg, ((JsonObject) JsonParser.parseReader(new InputStreamReader(stream))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void create(Set<Class<? extends Service>> services) {
        JsonArray modules = this.json.get("services").getAsJsonArray();
        for (JsonElement entry : modules) {
            String path = entry.getAsString();

            if (path.startsWith(".")) {
                path = this.json.get("package_namespace").getAsString() + path;
            }

            try {
                Class<? extends Service> clazz = (Class<? extends Service>) this.getLoader().loadClass(path);
                services.add(clazz);
            } catch (ClassNotFoundException e) {
                ExceptionUtil.log(e);
                this.getPackage().getLogger().warning("failed to construct module %s: %s".formatted(path, e.getMessage()));
            }
        }
        Starlight.instance().getLogger().info("created service from package %s(%s).".formatted(this.getPackage().getClass().getName(), this.getPackage().getId()));
    }
}
