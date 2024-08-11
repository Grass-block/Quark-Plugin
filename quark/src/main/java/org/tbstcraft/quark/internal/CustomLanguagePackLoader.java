package org.tbstcraft.quark.internal;

import com.google.gson.JsonObject;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.plugin.Plugin;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.data.language.LanguageContainer;
import org.tbstcraft.quark.data.language.LanguagePack;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@QuarkModule(version = "1.0")
public final class CustomLanguagePackLoader extends PackageModule {
    public static final String PACK_SCHEME = "CB723E2A-873D-5435-8CF2-9DEC5D09BDBD";

    private final Set<LanguagePack> registeredPacks = new HashSet<>();

    @Inject("language-packs;false")
    private AssetGroup languagePacks;

    @Override
    public void enable() {
        if (this.languagePacks.getFolder().mkdirs()) {
            getLogger().info("created language pack container directory.");
        }

        for (File f : Objects.requireNonNull(this.languagePacks.getFolder().listFiles())) {
            try (var file = new ZipFile(f)) {
                var packs = load(file);

                this.registeredPacks.addAll(packs);

                for (LanguagePack pack : packs) {
                    LanguageContainer.getInstance().register(pack);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        LanguageContainer.getInstance().refresh(true);
    }

    @Override
    public void disable() {
        for (LanguagePack pack : this.registeredPacks) {
            LanguageContainer.getInstance().unregister(pack);
        }
        LanguageContainer.getInstance().refresh(true);
        this.registeredPacks.clear();
    }


    public Set<LanguagePack> load(ZipFile file) {
        var packs = new HashSet<LanguagePack>();

        try (InputStream stream = file.getInputStream(file.getEntry("pack.json"))) {
            JsonObject desc = SharedObjects.JSON_PARSER.parse(new InputStreamReader(stream)).getAsJsonObject();

            var scheme = desc.get("scheme_uid").getAsString();
            var uuid = desc.get("id").getAsString();


            if (!scheme.equals(PACK_SCHEME)) {
                getLogger().warning("Invalid scheme uid %s at pack %s".formatted(scheme, file.getName()));
                return packs;
            }

            for (Iterator<? extends ZipEntry> it = file.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (entry.getName().endsWith(".json")) {
                    continue;
                }

                var packInfos = entry.getName().replace(".yml", "").split("_");
                var packInput = file.getInputStream(entry);

                packs.add(new ThirdPartyLanguagePack(packInfos[0], packInfos[1], this.getOwnerPlugin(), uuid, packInput));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        getLogger().info("loaded pack %s, %s packs created".formatted(file.getName(), packs.size()));

        return packs;
    }


    public static final class ThirdPartyLanguagePack extends LanguagePack {
        private final String uuid;//provider

        public ThirdPartyLanguagePack(String id, String locale, Plugin owner, String uuid, InputStream stream) throws Exception {
            super(id, locale, owner);
            this.uuid = uuid;
            this.dom.load(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
            stream.close();
        }

        @Override
        public void load() {
            //unsupported
        }

        @Override
        public void restore() {
            //unsupported
        }

        @Override
        public void sync(boolean clean) {
            //unsupported
        }

        @Override
        public String toString() {
            return super.toString() + "/" + this.uuid;
        }
    }
}
