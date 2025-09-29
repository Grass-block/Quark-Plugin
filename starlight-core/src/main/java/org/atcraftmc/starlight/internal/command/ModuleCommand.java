package org.atcraftmc.starlight.internal.command;

import me.gb2022.commons.TriState;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.ObjectOperationResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.MinecraftLocale;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CoreCommand;
import org.atcraftmc.starlight.framework.SLModuleManager;
import org.atcraftmc.starlight.framework.module.SLModuleHandle;
import org.atcraftmc.starlight.framework.packages.SLPackageManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@QuarkCommand(name = "module", permission = "-starlight.core.module")
public final class ModuleCommand extends CoreCommand {
    private final SLModuleManager handle = SLModuleManager.getInstance();

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
        var module = this.handle.get(mid).orElseThrow();
        this.getLanguage().item(id).send(sender, module.displayName(LocaleService.locale(sender)));
    }

    @Override
    public void suggest(CommandSuggestion suggestion) {
        suggestion.suggest(0, "list", "enable", "disable", "reload", "info");
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, "<search meta>"));
        suggestion.matchArgument(0, "list", (c) -> c.suggest(1, SLPackageManager.getInstance().getPackages().keySet()));
        suggestion.matchArgument(0, "enable", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.FALSE)));
        suggestion.matchArgument(0, "disable", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.TRUE)));
        suggestion.matchArgument(0, "reload", (c) -> c.suggest(1, this.handle.getIdsByStatus(TriState.TRUE)));
    }

    @Override
    public void execute(CommandExecution context) {
        var sender = context.getSender();
        var id = !context.hasArgumentAt(1) ? null : context.requireArgumentAt(1);

        switch (context.requireEnum(0, "list", "info", "enable", "disable", "reload", "enable-all", "disable-all", "reload-all")) {
            case "list" -> list(sender, !context.hasArgumentAt(1) ? "" : context.requireArgumentAt(1));
            case "enable-all" -> {
                this.handle.enableAll();
                this.getLanguage().item("enable-all").send(sender);
            }
            case "disable-all" -> {
                this.handle.disableAll();
                this.getLanguage().item("disable-all").send(sender);
            }
            case "reload-all" -> {
                this.handle.reloadAll();
                this.getLanguage().item("reload-all").send(sender);
            }
            case "enable" -> sendMessage(sender, messageId(this.handle.enable(id), "enable"), id);
            case "disable" -> sendMessage(sender, messageId(this.handle.disable(id), "disable"), id);
            case "reload" -> sendMessage(sender, messageId(this.handle.reload(id), "reload"), id);
        }
    }

    private Component buildModuleHoverInfo(SLModuleHandle m) {
        var statusColor = switch (m.getStatus()) {
            case UNKNOWN -> "&7";
            case REGISTER_FAILED, CONSTRUCT_FAILED, ENABLE_FAILED -> "&c";
            case REGISTER, DISABLED, CONSTRUCT -> "&f";
            case ENABLED -> "&a";
        };


        var hover = """
                &7ID: &b%s
                &7Status: %s%s
                &7Version: &d%s
                &7Description: &d%s
                """.formatted(
                m.getMetadata().key(),
                statusColor,
                m.getStatus().name(),
                m.getMetadata().version(),
                m.getMetadata().description()
        );

        return Component.text(ChatColor.translateAlternateColorCodes('&', hover));
    }

    private Component buildModuleInfo(SLModuleHandle m, MinecraftLocale locale) {
        var prefix = "&f[%s&f]".formatted(switch (m.getStatus()) {
            case UNKNOWN -> "&7U";
            case REGISTER_FAILED, CONSTRUCT_FAILED, ENABLE_FAILED -> "&cF";
            case REGISTER, DISABLED, CONSTRUCT -> "&7D";
            case ENABLED -> "&aE";
        });

        var info = Component.text(ChatColor.translateAlternateColorCodes('&', prefix + m.displayName(locale)));

        return info.hoverEvent(HoverEvent.showText(buildModuleHoverInfo(m)));
    }

    private void list(CommandSender sender, String prefix) {
        var nodes = SLModuleManager.getInstance().getModules().values().stream().filter((m) -> m.getMetadata()
                .key()
                .toString()
                .contains(prefix)).toList();

        getLanguage().item("list").send(sender, "");

        var groups = new HashMap<String, List<SLModuleHandle>>();

        for (var meta : nodes) {
            groups.computeIfAbsent(meta.getMetadata().fullId().split(":")[0], (k) -> new ArrayList<>()).add(meta);
        }

        for (var gid : groups.keySet()) {
            var exampleMeta = groups.get(gid).get(0);
            var group = groups.get(gid);

            var all = group.size();
            var enable = group.stream().filter((m) -> m.getStatus().equals(FunctionalComponentStatus.ENABLED)).count();
            var pack = "&6> &b%s&7[%s] (%d/%d)".formatted(gid, (exampleMeta.getParent()).getOwner().getName(), enable, all);

            Component msg1 = Component.text(ChatColor.translateAlternateColorCodes('&', pack));
            TextSender.sendMessage(sender, msg1);

            for (var meta : groups.get(gid)) {
                Component msg = Component.text("  ").append(buildModuleInfo(meta, LocaleService.locale(sender)));
                TextSender.sendMessage(sender, msg);
            }
        }
    }

    @Override
    public String getLanguageNamespace() {
        return "module";
    }
}
