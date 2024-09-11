package org.atcraftmc.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tbstcraft.quark.data.assets.AssetGroup;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.framework.module.CommandModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.task.TaskService;
import org.tbstcraft.quark.util.CachedInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-filter", version = "1.0.0")
@QuarkCommand(name = "chat-filter", permission = "-quark.config.chatfilter")
public final class ChatFilter extends CommandModule {
    private final Set<Pattern> patterns = new HashSet<>();
    private final Set<String> flagged = new HashSet<>();

    @Inject
    private LanguageEntry language;

    @Inject("chat-filter;false")
    private AssetGroup rules;

    public static CheckResult filter(String msg, boolean cover, char coverChar, Set<Pattern> pattern) {
        var coverSource = String.valueOf(coverChar);

        var buf = new StringBuilder();
        var res = new StringBuilder();
        var keywords = new HashSet<String>();

        for (char c : msg.toCharArray()) {
            if (c == '\ufffb') {
                res.append(buf);
                buf.delete(0, buf.length());
                continue;
            }
            if (c != '\ufffa') {
                buf.append(c);
                continue;
            }

            var block = buf.toString();
            buf.delete(0, buf.length());

            for (var p : pattern) {
                var m = p.matcher(block);

                while (m.find()) {
                    var match = m.group();

                    keywords.add(match);

                    if (!cover) {
                        continue;
                    }

                    block = block.replace(match, coverSource.repeat(match.length()));
                }
            }

            res.append(block);
        }

        return new CheckResult(keywords, msg.replace("\ufffa", "").replace("\ufffb", ""), res.toString());
    }

    @Override
    public void enable() {
        super.enable();
        this.load();
    }

    public void load() {
        this.patterns.clear();
        if (!this.rules.existFolder()) {
            this.rules.save("default-rule.txt");
            this.rules.save("default-rule.split.txt");
        }
        for (String s : this.rules.list()) {
            String exp = this.rules.asText(s).replace("\n", "");
            if (s.endsWith(".split.txt")) {
                String[] parts = exp.split("#");

                StringBuilder regexBuilder = new StringBuilder();

                for (String part : parts) {
                    if (!part.isEmpty()) {
                        var p = part.replace("+", "");
                        if (p.isEmpty() || p.isBlank()) {
                            continue;
                        }
                        regexBuilder.append(p).append("+").append("|");
                    }
                }
                regexBuilder.deleteCharAt(regexBuilder.length() - 1);
                exp = regexBuilder.toString();
            }

            this.patterns.add(Pattern.compile(exp));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(AsyncPlayerChatEvent event) {
        event.setMessage(this.process(event.getMessage()));
    }

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent event) {
        var commands = getConfig().getList("handled-commands");

        var process = false;
        for (var c : commands) {
            if (event.getMessage().startsWith('/' + c)) {
                process = true;
                break;
            }
        }

        if (!process) {
            return;
        }

        event.setMessage(this.process(event.getMessage()));
    }

    @EventHandler
    public void onChatReported(ChatReport.ChatReportedEvent event) {
        if (this.flagged.contains(event.getUuid())) {
            this.getLanguage().sendMessage(Bukkit.getPlayerExact(event.getSender()), "reported-warn", event.getShorted());

            if (!this.getConfig().getBoolean("punish")) {
                event.setOutcome(this.getLanguage().item("outcome-warn"));
                return;
            }

            var command = getConfig().getString("punish-command").replace("{player}", event.getSender());
            TaskService.global().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

            event.setOutcome(this.getLanguage().item("outcome-punished"));
        }
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        switch (args[0]) {
            case "reload" -> {
                this.load();
                this.language.sendMessage(sender, "reload");
            }
            case "save" -> {
                this.rules.save("default-rule.txt");
                this.rules.save("default-rule.split.txt");

                this.language.sendMessage(sender, "save");
            }
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.add("reload");
            tabList.add("save");
        }
    }


    public String process(String msg) {
        var exceptPlayer = getConfig().getBoolean("except-player");
        var cover = getConfig().getBoolean("cover");
        var coverChar = getConfig().getString("cover-char").charAt(0);

        msg = msg.replace("\ufffa", "").replace("\ufffb", "");

        if (exceptPlayer) {
            for (String name : CachedInfo.getAllPlayerNames()) {
                msg = msg.replaceAll("(?<!\ufffa[^\ufffb])" + name + "+(?![^\ufffa]*?\ufffb)", "\ufffa" + name + "\ufffb");
            }

            msg = "\ufffb" + msg + "\ufffa";
        }

        var result = filter(msg, cover, coverChar, this.patterns);
        var out = result.processed();

        if (result.flagged()) {
            this.flagged.add(ChatReport.hash(out));
        }

        return out;
    }


    public record CheckResult(Set<String> matched, String origin, String processed) {
        public boolean flagged() {
            return !this.matched.isEmpty();
        }
    }
}
