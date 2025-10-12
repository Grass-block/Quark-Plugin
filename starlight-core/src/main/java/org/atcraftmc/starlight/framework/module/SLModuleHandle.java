package org.atcraftmc.starlight.framework.module;

import me.gb2022.commons.reflect.Annotations;
import me.gb2022.modular.ComponentMetadata;
import me.gb2022.modular.module.ModuleHandle;
import org.atcraftmc.qlib.command.AbstractCommand;
import org.atcraftmc.qlib.config.ConfigContainer;
import org.atcraftmc.qlib.config.ConfigEntry;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.Starlight;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.StarlightCommandManager;
import org.atcraftmc.starlight.foundation.platform.APIProfile;
import org.atcraftmc.starlight.framework.packages.SLPackage;

import java.util.HashSet;
import java.util.Set;

public final class SLModuleHandle extends ModuleHandle<SLModule, SLModuleHandle, SLPackage> {
    private final LanguageItem displayName;
    private final Set<AbstractCommand> commands = new HashSet<>();
    private APIProfile[] compatBlackList = new APIProfile[0];
    private LanguageEntry language;
    private ConfigEntry config;

    public SLModuleHandle(SLPackage pack, Class<? extends SLModule> reference) {
        super(pack, reference, ComponentMetadata.fromModule(pack.getId(), reference));
        this.displayName = Starlight.lang().access(pack.getId()).item("-module-name:" + getMetadata().key().id());
        Annotations.matchAnnotation(reference, BlacklistPlatform.class, (b) -> this.compatBlackList = b.value());
    }

    @Override
    public void preEnable(SLModule module) {
        var ns = this.getParent().getId();
        var id = this.getMetadata().key().id();

        this.language = Starlight.instance().language().entry(ns, id);
        this.config = ConfigContainer.getInstance().entry(ns, id);

        ModuleServices.onEnable(this);
    }

    @Override
    public void postDisable(SLModule module) {
        ModuleServices.onDisable(this);
    }

    @Override
    public String getLoggerName() {
        return "Starlight";
    }

    public String displayName(MinecraftLocale locale) {
        var ns = this.getMetadata().key().namespace();
        var id = this.getMetadata().key().id();

        if (this.displayName != null && this.displayName.handle().hasAny(ns, "-module-name", id)) {
            return "%s&7(&f%s&7)".formatted(id, this.displayName.message(locale));
        } else {
            return id;
        }
    }

    public ConfigEntry getConfig() {
        return config;
    }

    public LanguageEntry getLanguage() {
        return language;
    }

    public APIProfile[] getCompatBlackList() {
        return compatBlackList;
    }

    public Set<AbstractCommand> getCommands() {
        return this.commands;
    }

    public void registerCommand(AbstractCommand c) {
        this.commands.add(c);
        if (c instanceof ModuleCommand mc) {
            mc.initContext(getModule(SLModule.class).orElseThrow());
        }
        StarlightCommandManager.getInstance().register(c);
    }

    public void unregisterCommand(AbstractCommand c) {
        this.commands.remove(c);
        StarlightCommandManager.getInstance().unregister(c);
    }

    public AbstractCommand getCommand(String id) {
        return StarlightCommandManager.getInstance().getCommand(id);
    }
}
