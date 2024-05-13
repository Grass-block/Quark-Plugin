package org.tbstcraft.quark.framework.packages.initializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.framework.config.Configuration;
import org.tbstcraft.quark.framework.config.Language;
import org.tbstcraft.quark.framework.module.providing.JsonModuleRegistry;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.service.providing.JsonServiceRegistry;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonPackageInitializer implements PackageInitializer {
    private final FeatureAvailability availability;
    private final String location;
    private JsonObject obj;

    public JsonPackageInitializer(FeatureAvailability availability, String location) {
        this.availability = availability;
        this.location = location;
    }

    @Override
    public void onInitialize(Plugin owner) {
        InputStream in = owner.getClass().getResourceAsStream(this.location);

        if (in == null) {
            throw new RuntimeException("cannot find json identifier: " + this.location);
        }

        this.obj = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        if (this.obj == null) {
            throw new RuntimeException("failed to load identifier: " + this.location);
        }
    }

    @Override
    public String getId(AbstractPackage pkg) {
        return this.obj.get("id").getAsString();
    }

    @Override
    public ModuleRegistry getModuleRegistry(AbstractPackage pkg) {
        return new JsonModuleRegistry(pkg, this.obj);
    }

    @Override
    public ServiceRegistry getServiceRegistry(AbstractPackage pkg) {
        if(!this.obj.has("services")){
            return null;
        }
        return new JsonServiceRegistry(pkg, this.obj);
    }

    @Override
    public FeatureAvailability getAvailability(AbstractPackage pkg) {
        return this.availability;
    }
}
