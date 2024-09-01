package org.tbstcraft.quark.framework.module.providing;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.tbstcraft.quark.Quark;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.packages.IPackage;
import org.tbstcraft.quark.util.ExceptionUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

public final class JsonModuleRegistry extends ModuleRegistry {
    private final JsonObject json;

    public JsonModuleRegistry(IPackage pkg, JsonObject json) {
        super(pkg);
        this.json = json;
        if(this.json==null){
            throw new RuntimeException("illegal package registry!");
        }
        this.create(this.getModules());
    }

    public static JsonModuleRegistry create(IPackage pkg, String path) {
        try {
            InputStream stream = pkg.getClass().getResourceAsStream(path);
            if (stream == null) {
                Quark.getInstance().getLogger().warning("failed to load package descriptor.");
                return null;
            }
            stream.close();
            return new JsonModuleRegistry(pkg, ((JsonObject) JsonParser.parseReader(new InputStreamReader(stream))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                ExceptionUtil.log(e);
                this.getPackage().getLogger().warning("failed to construct module %s: %s".formatted(entry.getKey(), e.getMessage()));
            }catch (NoClassDefFoundError e){
                this.getPackage().getLogger().warning("failed to construct module %s: missing API class %s".formatted(entry.getKey(), e.getMessage()));
            }
        }
        Quark.getInstance().getLogger().info("created modules from package %s(%s).".formatted(this.getPackage().getClass().getName(), this.getPackage().getId()));
    }
}
