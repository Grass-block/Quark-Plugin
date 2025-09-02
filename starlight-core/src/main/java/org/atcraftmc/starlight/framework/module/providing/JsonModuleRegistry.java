package org.atcraftmc.starlight.framework.module.providing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.framework.module.AbstractModule;
import org.atcraftmc.starlight.framework.module.ModuleMeta;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.packages.IPackage;
import org.atcraftmc.starlight.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public final class JsonModuleRegistry extends ModuleRegistry {
    private final JsonObject json;

    public JsonModuleRegistry(IPackage pkg, JsonObject json) {
        super(pkg);
        this.json = json;
        if (this.json == null) {
            throw new RuntimeException("illegal package registry!");
        }
        this.create(this.metas);
    }

    public static JsonModuleRegistry create(IPackage pkg, String path) {
        try {
            InputStream stream = pkg.getClass().getResourceAsStream(path);
            if (stream == null) {
                Starlight.instance().getLogger().warning("failed to load package descriptor.");
                return null;
            }
            stream.close();
            return new JsonModuleRegistry(pkg, ((JsonObject) JsonParser.parseReader(new InputStreamReader(stream))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(Collection<ModuleMeta> list) {
        var modules = this.json.get("modules").getAsJsonObject();
        var logger = this.getPackage().getLogger();
        var classPath = this.json.get("package_namespace").getAsString();

        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            var path = entry.getValue().getAsString();
            var id = entry.getKey();

            if (path.startsWith(".")) {
                path = classPath + path;
            }

            try {
                var clazz = this.getLoader().loadClass(path);
                var meta = ModuleMeta.create((Class<AbstractModule>) clazz, this.getPackage(), id);

                list.add(meta);
            } catch (ClassNotFoundException e) {
                logger.warning("failed to load class: %s -> %s".formatted(id, e.getMessage()));
            } catch (NoClassDefFoundError e) {
                logger.warning("failed to load class %s: missing API class %s".formatted(id, e.getMessage()));
            }
        }

        logger.info("created modules metas from package %s.".formatted(this.getPackage().getId()));
    }

    @Override
    public void create(Set<AbstractModule> moduleList) {
        JsonObject modules = this.json.get("modules").getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : modules.entrySet()) {
            JsonElement entryElement = entry.getValue();
            String path = entryElement.getAsString();

            if (path.startsWith(".")) {
                path = this.json.get("package_namespace").getAsString() + path;
            }

            try {
                Class<?> clazz = this.getLoader().loadClass(path);
                PackageModule m = (PackageModule) clazz.getDeclaredConstructor().newInstance();
                m.init(entry.getKey(), this.getPackage());
                moduleList.add(m);
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                ExceptionUtil.log(e);
                this.getPackage().getLogger().warning("failed to construct module %s: %s".formatted(entry.getKey(), e.getMessage()));
            } catch (NoClassDefFoundError e) {
                this.getPackage()
                        .getLogger()
                        .warning("failed to construct module %s: missing API class %s".formatted(entry.getKey(), e.getMessage()));
            }
        }
        Starlight.instance()
                .getLogger()
                .info("created modules from package %s(%s).".formatted(this.getPackage().getClass().getName(), this.getPackage().getId()));
    }


}
