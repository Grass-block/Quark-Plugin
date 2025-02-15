package org.tbstcraft.quark.internal.command;

import me.gb2022.commons.TriState;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.atcraftmc.qlib.language.Language;
import org.tbstcraft.quark.foundation.command.CoreCommand;
import org.tbstcraft.quark.foundation.TextSender;
import org.tbstcraft.quark.framework.FunctionalComponentStatus;
import org.tbstcraft.quark.framework.module.AbstractModule;
import org.tbstcraft.quark.framework.module.ModuleManager;
import org.tbstcraft.quark.framework.module.ModuleMeta;
import org.tbstcraft.quark.framework.packages.PackageManager;
import org.tbstcraft.quark.util.ObjectOperationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@QuarkCommand(name = "module", permission = "-quark.module")
public final class ModuleCommand extends CoreCommand {
    private final ModuleManager handle = ModuleManager.getInstance();

    static String messageId(ObjectOperationResult result, String success) {
        return switch (result) {
            case SUCCESS -> success;
            case NOT_FOUND -> "not-found";
            case ALREADY_OPERATED -> "already-op";
            case INTERNAL_ERROR -> "internal-error";
            case BLOCKED_INTERNAL -> "blocked-internal";
        };
    }

    private void sendMessage(CommandSender sender, String id, String mid) {
        AbstractModule module = this.handle.get(mid);
        this.getLanguage().sendMessage(sender, id, module.getDisplayName(Language.locale(sender)));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "list", "enable", "disable", "reload", "info");
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, "<search meta>"));
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, PackageManager.getInstance().getPackages().keySet()));
        suggestion.matchArgument(0, "enable", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.TRUE)));
        suggestion.matchArgument(0, "disable", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.FALSE)));
        suggestion.matchArgument(0, "reload", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.FALSE)));
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();
        var id = !context.hasArgumentAt(1) ? null : context.requireArgumentAt(1);

        switch (context.requireEnum(0, "list", "info", "enable", "disable", "reload", "enable-all", "disable-all", "reload-all")) {
            case "list" -> list(sender, !context.hasArgumentAt(1) ? "" : context.requireArgumentAt(1));
            case "enable-all" -> {
                this.handle.enableAll();
                this.getLanguage().sendMessage(sender, "enable-all");
            }
            case "disable-all" -> {
                this.handle.disableAll();
                this.getLanguage().sendMessage(sender, "disable-all");
            }
            case "reload-all" -> {
                this.handle.reloadAll();
                this.getLanguage().sendMessage(sender, "reload-all");
            }
            case "enable" -> sendMessage(sender, messageId(this.handle.enable(id), "enable"), id);
            case "disable" -> sendMessage(sender, messageId(this.handle.disable(id), "disable"), id);
            case "reload" -> sendMessage(sender, messageId(this.handle.reload(id), "reload"), id);
        }
    }

    private Component buildModuleHoverInfo(ModuleMeta m) {
        var statusColor = switch (m.status()) {
            case UNKNOWN -> "&7";
            case REGISTER_FAILED, CONSTRUCT_FAILED, ENABLE_FAILED -> "&c";
            case REGISTER, DISABLED, CONSTRUCT -> "&f";
            case ENABLE -> "&a";
        };


        var hover = """
                &7ID: &b%s
                &7Status: %s%s
                &7Version: &d%s
                &7Info: &f%s
                """.formatted(m.fullId(), statusColor, m.status().name(), m.version(), m.additional());

        return Component.text(ChatColor.translateAlternateColorCodes('&', hover));
    }

    private Component buildModuleInfo(ModuleMeta m, Locale locale) {
        var prefix = "&f[%s&f]".formatted(switch (m.status()) {
            case UNKNOWN -> "&7U";
            case REGISTER_FAILED, CONSTRUCT_FAILED, ENABLE_FAILED -> "&cF";
            case REGISTER, DISABLED, CONSTRUCT -> "&7D";
            case ENABLE -> "&aE";
        });

        var info = Component.text(ChatColor.translateAlternateColorCodes('&', prefix + m.displayName(locale)));

        return info.hoverEvent(HoverEvent.showText(buildModuleHoverInfo(m)));
    }

    private void list(CommandSender sender, String prefix) {
        var nodes = ModuleManager.getInstance().getKnownModuleMetas().values().stream().filter((m) -> m.fullId().contains(prefix)).toList();

        getLanguage().sendMessage(sender, "list", "");

        var groups = new HashMap<String, List<ModuleMeta>>();

        for (var meta : nodes) {
            groups.computeIfAbsent(meta.fullId().split(":")[0], (k) -> new ArrayList<>()).add(meta);
        }

        for (var gid : groups.keySet()) {
            var exampleMeta = groups.get(gid).get(0);
            var group = groups.get(gid);

            var all = group.size();
            var enable = group.stream().filter((m) -> m.status().equals(FunctionalComponentStatus.ENABLE)).count();
            var pack = "&6> &b%s&7[%s] (%d/%d)".formatted(gid, exampleMeta.parent().getOwner().getName(), enable, all);

            Component msg1 = Component.text(ChatColor.translateAlternateColorCodes('&', pack));
            TextSender.sendMessage(sender, msg1);

            for (var meta : groups.get(gid)) {
                Component msg = Component.text("  ").append(buildModuleInfo(meta, Language.locale(sender)));
                TextSender.sendMessage(sender, msg);
            }
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "module";
    }
}
