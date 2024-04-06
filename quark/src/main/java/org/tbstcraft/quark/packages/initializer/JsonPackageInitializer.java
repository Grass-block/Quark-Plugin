package org.tbstcraft.quark.packages.initializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.config.Configuration;
import org.tbstcraft.quark.config.Language;
import org.tbstcraft.quark.module.providing.JsonModuleRegistry;
import org.tbstcraft.quark.module.providing.ModuleRegistry;
import org.tbstcraft.quark.packages.AbstractPackage;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonPackageInitializer implements PackageInitializer {
    private JsonObject obj;
    private final FeatureAvailability availability;
    private final String location;

    public JsonPackageInitializer(FeatureAvailability availability, String location) {
        this.availability = availability;
        this.location = location;
    }

    @Override
    public void onInitialize(Plugin owner) {
        InputStream in=owner.getClass().getResourceAsStream(this.location);

        if (in == null) {
            throw new RuntimeException("cannot find json identifier: " + this.location);
        }

        this.obj = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
        if(this.obj==null){
            throw new RuntimeException("failed to load identifier: " + this.location);
        }
    }

    @Override
    public Configuration createConfig(AbstractPackage pkg) {
        if (this.obj.has("config")&&!this.obj.get("config").getAsBoolean()) {
            //return null;
        }
        return PackageInitializer.super.createConfig(pkg);
    }

    @Override
    public Language createLanguage(AbstractPackage pkg) {
        if (this.obj.has("language")&&!this.obj.get("language").getAsBoolean()) {
            //return null;
        }
        return PackageInitializer.super.createLanguage(pkg);
    }

    @Override
    public String getId(AbstractPackage pkg) {
        return this.obj.get("id").getAsString();
    }

    @Override
    public ModuleRegistry getRegistry(AbstractPackage pkg) {
        return new JsonModuleRegistry(pkg, this.obj);
    }

    @Override
    public FeatureAvailability getAvailability(AbstractPackage pkg) {
        if (this.availability != FeatureAvailability.INHERIT) {
            return this.availability;
        }
        if (!this.obj.has("feature")) {
            return FeatureAvailability.PREMIUM;
        }
        return FeatureAvailability.fromId(this.obj.get("feature").getAsString());
    }
}
