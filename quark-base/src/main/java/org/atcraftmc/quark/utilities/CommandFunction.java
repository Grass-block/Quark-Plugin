package org.atcraftmc.quark.utilities;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.foundation.command.AbstractCommand;
import org.tbstcraft.quark.foundation.command.CommandManager;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@QuarkModule(version = "1.0.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
public final class CommandFunction extends PackageModule {
    private final Set<AdapterCommand> commands = new HashSet<>();

    @Inject("function;false")
    private AssetGroup functionFiles;

    @Override
    public void enable() {
        if (!this.functionFiles.existFolder()) {
            this.functionFiles.save("worldedit.yml");
        }

        for (String cfg : this.functionFiles.list()) {
            ConfigurationSection dom = YamlConfiguration.loadConfiguration(this.functionFiles.getFile(cfg)).getConfigurationSection("functions");

            assert dom != null;

            for (String tagName : dom.getKeys(false)) {
                AdapterCommand adapter = new AdapterCommand(tagName, dom.getStringList(tagName));
                this.commands.add(adapter);
                CommandManager.registerQuarkCommand(adapter);
            }

            getLogger().info("loaded function provider file %s.".formatted(cfg));
        }
    }

    @Override
    public void disable() {
        for (AdapterCommand command : this.commands) {
            CommandManager.unregister(command);
        }
    }

    @QuarkCommand(name = "__dummy")
    public static class AdapterCommand extends AbstractCommand {
        private final String trigger;
        private final List<String> triggerList;

        public AdapterCommand(String trigger, List<String> triggerList) {
            this.trigger = trigger;
            this.triggerList = triggerList;
        }

        public @NotNull String getName() {
            return this.trigger;
        }

        @Override
        public boolean isOP() {
            return false;
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            for (String pattern : triggerList) {
                for (int i = 0; i < args.length; i++) {
                    pattern = pattern.replace("{arg%d}".formatted(i), args[i]);
                }
                ((Player) sender).performCommand(pattern);
            }
        }
    }
}
