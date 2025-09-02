package org.atcraftmc.starlight.management;

import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.ahocorasick.trie.Trie;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.language.LanguageEntry;
import org.atcraftmc.starlight.Configurations;
import org.atcraftmc.starlight.api.ChatReportedEvent;
import org.atcraftmc.starlight.core.TaskService;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.services.ServiceType;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.util.CachedInfo;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

@AutoRegister(ServiceType.EVENT_LISTEN)
@SLModule(id = "chat-filter", version = "1.0.0")
public final class ChatFilter extends PackageModule {
    public static final char WRAP_START = '\ufffa';
    public static final char WRAP_END = '\ufffb';
    public static final String WRAP_START_S = String.valueOf(WRAP_START);
    public static final String WRAP_END_S = String.valueOf(WRAP_END);

    public static final Pattern GET_ALL = Pattern.compile(".+");

    private final Set<Pattern> patterns = new HashSet<>();
    private final Set<String> flagged = new HashSet<>();
    private Trie trie;

    @Inject
    private Logger logger;

    @Inject
    private LanguageEntry language;

    public static CheckResult filter(String msg, boolean cover, char coverChar, Set<Pattern> pattern, Trie ahoCorasick) {
        var coverSource = String.valueOf(coverChar);

        var buf = new StringBuilder();
        var res = new StringBuilder();
        var keywords = new HashSet<String>();

        for (char c : msg.toCharArray()) {
            if (c == WRAP_END) {
                res.append(buf);
                buf.delete(0, buf.length());
                continue;
            }
            if (c != WRAP_START) {
                buf.append(c);
                continue;
            }

            final String[] block = {buf.toString()};
            buf.delete(0, buf.length());

            ahoCorasick.parseText(block[0], emit -> {
                keywords.add(emit.getKeyword());
                block[0] = block[0].replace(emit.getKeyword(), coverSource.repeat(emit.getKeyword().length()));
                return true;
            });

            for (var p : pattern) {
                var m = p.matcher(block[0]);

                while (m.find()) {
                    var match = m.group();

                    keywords.add(match);

                    if (!cover) {
                        continue;
                    }

                    block[0] = block[0].replace(match, coverSource.repeat(match.length()));
                }
            }

            res.append(block[0]);
        }

        return new CheckResult(keywords, msg.replace(WRAP_START_S, "").replace(WRAP_END_S, ""), res.toString());
    }

    @Override
    public void enable() {
        var builder = Trie.builder().ignoreCase();
        var counter = new AtomicInteger(0);
        var localCounter = new AtomicInteger();
        this.patterns.clear();

        TaskService.async().run(()->{
            Configurations.groupedJson("chat-filter-rules", Set.of()).forEach((k, v) -> {
                localCounter.set(0);
                v.getAsJsonArray("words").forEach((e) -> {
                    builder.addKeyword(e.getAsString());
                    counter.incrementAndGet();
                    localCounter.getAndIncrement();
                });

                this.logger.info("loaded json rules {}({} words).", k, localCounter.get());
            });

            var txtDefaults = Set.of(
                    "chat-filter-defaults/cn-ads.txt",
                    "chat-filter-defaults/cn-bribery.txt",
                    "chat-filter-defaults/cn-covid19.txt",
                    "chat-filter-defaults/cn-extra.txt",
                    "chat-filter-defaults/cn-extra-more.txt",
                    "chat-filter-defaults/cn-gfw-extra.txt",
                    "chat-filter-defaults/cn-guns.txt",
                    "chat-filter-defaults/cn-illegal-urls.txt",
                    "chat-filter-defaults/cn-politics.txt",
                    "chat-filter-defaults/cn-reactionary.txt",
                    "chat-filter-defaults/cn-sex.txt",
                    "chat-filter-defaults/cn-society.txt",
                    "chat-filter-defaults/cn-terror.txt",
                    "chat-filter-defaults/cn-yellow.txt",
                    "chat-filter-defaults/other-defaults.txt"
            );

            Configurations.grouped("chat-filter-rules", txtDefaults, "#", "txt").forEach((k, v) -> {
                localCounter.set(0);

                for (var s : v.split("\n")) {
                    if (s.isEmpty()) {
                        continue;
                    }

                    localCounter.incrementAndGet();

                    if (s.matches("\\+|\\|+|\\[+|]+\\*+")) {
                        this.patterns.add(Pattern.compile(s));
                        continue;
                    }

                    counter.incrementAndGet();
                    builder.addKeyword(s);
                }

                this.logger.info("loaded txt rules {}({} words).", k, localCounter.get());
            });

            this.logger.info("done, {} pattern compiled, {} keywords added to aho-corasick.", this.patterns.size(), counter.get());

            this.trie = builder.build();
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void handle(AsyncPlayerChatEvent event) {
        event.setMessage(this.process(event.getMessage()));
    }

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent event) {
        var commands = ConfigAccessor.configList(getConfig(), "handled-commands", String.class);

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
    public void handle(SignChangeEvent event) {
        if (!getConfig().value("filter-sign").bool()) {
            return;
        }

        try {
            event.getClass().getMethod("line", int.class, Component.class);

            for (var i = 0; i < event.lines().size(); i++) {
                var origin = event.line(i);

                if (origin == null) {
                    continue;
                }

                var repl = TextReplacementConfig.builder()
                        .match(GET_ALL)
                        .replacement((result, builder) -> builder.content(process(result.group())))
                        .build();

                event.line(i, origin.replaceText(repl));
            }
        } catch (NoSuchMethodException ex) {
            for (var i = 0; i < event.getLines().length; i++) {
                event.setLine(i, process(event.getLine(i)));
            }
        }
    }

    @EventHandler
    public void onChatReported(ChatReportedEvent event) {
        if (this.flagged.contains(event.getUuid())) {
            MessageAccessor.send(this.language, Bukkit.getPlayerExact(event.getSender()), "reported-warn", event.getShorted());

            if (!ConfigAccessor.getBool(this.getConfig(), "punish")) {
                event.setOutcome(this.language.item("outcome-warn"));
                return;
            }

            var command = getConfig().value("punish-command").string().replace("{player}", event.getSender());
            TaskService.global().run(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

            event.setOutcome(this.language.item("outcome-punished"));
        }
    }

    public String process(String msg) {
        var exceptPlayer = getConfig().value("except-player").bool();
        var cover = getConfig().value("cover").bool();
        var coverChar = getConfig().value("cover-char").string().charAt(0);

        msg = msg.replace(WRAP_START_S, "").replace(WRAP_END_S, "");

        if (exceptPlayer) {
            for (String name : CachedInfo.getAllPlayerNames()) {
                msg = msg.replaceAll("(?<!\ufffa[^\ufffb])" + name + "+(?![^\ufffa]*?\ufffb)", WRAP_START + name + WRAP_END);
            }

            msg = WRAP_END + msg + WRAP_START;
        }

        var result = filter(msg, cover, coverChar, this.patterns, this.trie);
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
