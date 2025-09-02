package org.atcraftmc.quark.chat;

import me.gb2022.commons.reflect.Inject;
import org.apache.logging.log4j.Logger;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.texts.TextBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.atcraftmc.starlight.migration.ConfigAccessor;
import org.atcraftmc.starlight.migration.MessageAccessor;
import org.atcraftmc.starlight.data.assets.AssetGroup;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.framework.module.CommandModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.util.BukkitSound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@QuarkCommand(name = "npc-chat")
@SLModule(version = "1.0.0")
public final class NPCChat extends CommandModule {
    private final Map<String, ConfigurationSection> contexts = new HashMap<>();
    private final Map<String, Integer> audienceIndexes = new HashMap<>();
    private final Map<String, Long> audienceChatRecord = new HashMap<>();

    @Inject("npc-conversation;false")
    private AssetGroup conversations;

    @Inject
    private Logger logger;

    @Override
    public void enable() {
        super.enable();
        if (!this.conversations.existFolder()) {
            this.conversations.save("gb.yml");
            this.conversations.save("tbs.yml");
        }
        for (String k : conversations.list()) {
            String name = k.replace(".yml", "");
            YamlConfiguration dom = YamlConfiguration.loadConfiguration(this.conversations.getFile(k));

            this.contexts.put(name, dom.getConfigurationSection("conversation"));

            this.logger.info("loaded conversation %s".formatted(k));
        }
    }

    private int getConversationIndex(String sender, int limit) {
        long current = System.currentTimeMillis();

        if (!this.audienceChatRecord.containsKey(sender)) {
            this.audienceChatRecord.put(sender, System.currentTimeMillis());
            this.audienceIndexes.put(sender, 0);
            return 0;
        }
        if (current - this.audienceChatRecord.get(sender) > ConfigAccessor.getInt(this.getConfig(), "inactive-time")) {
            this.audienceChatRecord.put(sender, System.currentTimeMillis());
            this.audienceIndexes.put(sender, 0);
            return 0;
        }
        int index = audienceIndexes.get(sender) + 1;

        if (index >= limit) {
            index = 0;
        }

        this.audienceChatRecord.put(sender, System.currentTimeMillis());
        this.audienceIndexes.put(sender, index);
        return index;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        ConfigurationSection conversation = this.contexts.get(args[0]);
        if (conversation == null) {
            MessageAccessor.send(this.getLanguage(), sender, "not-found");
            return;
        }

        var template = Objects.requireNonNull(this.getConfig().value("template").string());
        var locale = LocaleService.locale(sender).minecraft();
        var name = Objects.requireNonNull(conversation.getString("name." + locale));
        var texts = conversation.getStringList(locale);

        var text = texts.get(getConversationIndex(sender.getName() + ":" + args[0], texts.size()));


        TextSender.sendBlock(sender, TextBuilder.build(template.replace("{name}", name).replace("{text}", text)));

        if (ConfigAccessor.getBool(this.getConfig(), "sound") && sender instanceof Player) {
            BukkitSound.ANNOUNCE.play(((Player) sender));
        }
    }

    @Override
    public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
        if (buffer.length == 1) {
            tabList.addAll(this.contexts.keySet());
        }
    }
}
