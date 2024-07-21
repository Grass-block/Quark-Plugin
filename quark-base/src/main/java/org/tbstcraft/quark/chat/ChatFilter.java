package org.tbstcraft.quark.chat;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoRegister(ServiceType.EVENT_LISTEN)
@QuarkModule(id = "chat-filter", version = "1.0.0")
@QuarkCommand(name = "chat-filter", permission = "-quark.config.chatfilter")
public final class ChatFilter extends CommandModule {
    private final Set<Pattern> patterns = new HashSet<>();

    @Inject
    private LanguageEntry language;

    @Inject("chat-filter;false")
    private AssetGroup rules;

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
                        regexBuilder.append(Pattern.quote(part)).append("|");
                    }
                }
                exp = regexBuilder.toString();
            }

            this.patterns.add(Pattern.compile(exp));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatting(AsyncPlayerChatEvent event) {
        String msg = this.filter(event.getMessage());
        event.setMessage(msg);
    }

    @EventHandler
    public void detectCommand(PlayerCommandPreprocessEvent event) {
        if (!(event.getMessage().contains("say") || event.getMessage().contains("tell"))) {
            return;
        }
        event.setMessage(this.filter(event.getMessage()));
    }

    public String filter(String msg) {
        for (Pattern pattern : this.patterns) {
            Matcher matcher = pattern.matcher(msg);

            while (matcher.find()) {
                String match = matcher.group();
                msg = msg.replace(match, "*".repeat(match.length()));
            }
        }
        return msg;
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
}
