package org.tbstcraft.quark.framework.packages.initializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.FeatureAvailability;
import org.tbstcraft.quark.data.config.Configuration;
import org.tbstcraft.quark.data.language.LanguagePack;
import org.tbstcraft.quark.framework.module.providing.JsonModuleRegistry;
import org.tbstcraft.quark.framework.module.providing.ModuleRegistry;
import org.tbstcraft.quark.framework.packages.AbstractPackage;
import org.tbstcraft.quark.framework.service.providing.JsonServiceRegistry;
import org.tbstcraft.quark.framework.service.providing.ServiceRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")//JsonParser.parse[static] does not exist in older GSON.
public final class JsonPackageInitializer implements PackageInitializer {
    public static final JsonParser PARSER = new JsonParser();

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

        this.obj = PARSER.parse(new InputStreamReader(in)).getAsJsonObject();
        if (this.obj == null) {
            throw new RuntimeException("failed to load identifier: " + this.location);
        }
    }

    @Override
    public Set<LanguagePack> createLanguagePack(AbstractPackage pkg) {
        Set<LanguagePack> packs = new HashSet<>();

        JsonArray languages = this.obj.getAsJsonArray("languages");
        if (languages == null) {
            return packs;
        }

        for (JsonElement element : languages) {
            String[] item = element.getAsString().split(":");
            packs.add(new LanguagePack(item[0], item[1], pkg.getOwner()));
        }

        return packs;
    }

    @Override
    public Set<Configuration> createConfig(AbstractPackage pkg) {
        Set<Configuration> packs = new HashSet<>();

        JsonArray arr = this.obj.getAsJsonArray("configs");
        if (arr == null) {
            return packs;
        }

        for (JsonElement element : arr) {
            String item = element.getAsString();
            packs.add(new Configuration(pkg.getOwner(), item));
        }

        return packs;
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
        if (!this.obj.has("services")) {
            return null;
        }
        return new JsonServiceRegistry(pkg, this.obj);
    }

    @Override
    public boolean isEnableByDefault() {
        if (!this.obj.has("default-enable")) {
            return true;
        }
        return this.obj.get("default-enable").getAsBoolean();
    }

    @Override
    public FeatureAvailability getAvailability(AbstractPackage pkg) {
        return this.availability;
    }
}
