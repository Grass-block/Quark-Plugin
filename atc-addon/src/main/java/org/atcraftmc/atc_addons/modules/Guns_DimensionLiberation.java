package org.atcraftmc.atc_addons.modules;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.gb2022.commons.math.MathHelper;
import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.nbt.NBTTagString;
import me.gb2022.commons.reflect.AutoRegister;
import me.gb2022.commons.reflect.Inject;
import me.gb2022.commons.reflect.method.MethodHandle;
import me.gb2022.commons.reflect.method.MethodHandleRO0;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.joml.Vector2i;
import org.atcraftmc.starlight.SharedObjects;
import org.atcraftmc.starlight.api.PluginMessages;
import org.atcraftmc.starlight.api.PluginStorage;
import org.atcraftmc.starlight.data.ModuleDataService;
import org.atcraftmc.starlight.foundation.ComponentSerializer;
import org.atcraftmc.starlight.foundation.TextExaminer;
import org.atcraftmc.starlight.foundation.TextSender;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.foundation.command.PluginCommandExecutor;
import org.atcraftmc.starlight.framework.module.PackageModule;
import org.atcraftmc.starlight.framework.module.SLModule;
import org.atcraftmc.starlight.framework.module.component.Components;
import org.atcraftmc.starlight.framework.module.component.ModuleComponent;
import org.atcraftmc.starlight.framework.module.services.Registers;
import org.atcraftmc.starlight.core.LocaleService;
import org.atcraftmc.starlight.internal.PlayerIdentificationService;
import org.atcraftmc.starlight.core.TaskService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SLModule
@AutoRegister(Registers.BUKKIT_EVENT)
@CommandProvider({Guns_DimensionLiberation.DimLiberationCommand.class})
@Components({Guns_DimensionLiberation.DataManager.class, Guns_DimensionLiberation.ConfigurationManager.class})
public final class Guns_DimensionLiberation extends PackageModule implements PluginCommandExecutor {
    public static final MethodHandleRO0<World, String> DIM_ID = MethodHandle.select((ctx) -> {
        ctx.attempt(() -> World.class.getMethod("getKey"), (w) -> {
            var origin = w.getKey().toString();
            return origin.replace(":", "-").replace("_", "-");
        });
        ctx.dummy((w) -> "minecraft-" + w.getName()
                .replace("world", "overworld")
                .replace("world-nether", "the-nether")
                .replace("world-the-end", "the-end")
                .replace("DIM0", "overworld")
                .replace("DIM1", "the-end")
                .replace("DIM-1", "the-nether"));
    });
    private final Map<String, BossBar> bossBars = new HashMap<>();
    @Inject("announce")
    private LanguageItem tip;

    @Override
    public void enable() {
        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_CUSTOM_ACTIVITY, (s) -> s.add(this.tip));

        TaskService.global().timer(5, 5, () -> {
            for (var player : Bukkit.getOnlinePlayers()) {
                this.renderProgressBar(player);
            }
        });
    }

    @Override
    public void disable() {
        for (var player : Bukkit.getOnlinePlayers()) {
            this.destroyProgressBar(player);
        }

        PluginStorage.set(PluginMessages.CHAT_ANNOUNCE_CUSTOM_ACTIVITY, (s) -> s.remove(this.tip));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        destroyProgressBar(event.getPlayer());
        if (!getDataManager().loaded) {
            return;
        }

        getDataManager().save();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var dm = getDataManager();
        if (!dm.loaded) {
            return;
        }

        if (dm.move(event.getPlayer())) {
            dm.assign(event.getPlayer());
        }
    }

    public Vector2i getStatForWorld(World world) {
        return new Vector2i(getDataManager().getExploredData(world).size(), getConfigManager().required(DIM_ID.invoke(world)));
    }

    public void renderProgressBar(Player player) {
        var world = player.getWorld();
        var pid = PlayerIdentificationService.transformPlayer(player);
        var wid = DIM_ID.invoke(player.getWorld());
        var locale = LocaleService.locale(player);
        var worldLK = "{msg#common:world:" + wid + "}";

        var config = getConfigManager();

        var stat = getStatForWorld(player.getWorld());

        var explored = stat.x();
        var required = stat.y();
        var process = MathHelper.clamp(explored / ((float) required), 0, 1);
        var formattedProcess = SharedObjects.NUMBER_FORMAT.format(process * 100) + "%";

        var title = getLanguage().getMessageComponent(locale, "bossbar", worldLK, explored, required, formattedProcess).toSingleLine();
        var color = config.color(world);
        var titleText = ComponentSerializer.legacy(TextExaminer.examine(title, locale));

        if (!this.bossBars.containsKey(pid)) {
            var bossbar = Bukkit.createBossBar(titleText, color, BarStyle.SEGMENTED_20);
            bossbar.addPlayer(player);

            this.bossBars.put(pid, bossbar);
        }

        var bossBar = this.bossBars.get(pid);

        bossBar.setTitle(titleText);
        bossBar.setColor(color);
        bossBar.setProgress(process);
    }

    public void destroyProgressBar(Player player) {
        var pid = PlayerIdentificationService.transformPlayer(player);

        if (!this.bossBars.containsKey(pid)) {
            return;
        }

        var bossBar = this.bossBars.get(pid);

        bossBar.removeAll();

        this.bossBars.remove(pid);
    }

    public void announce(Player player, int rx, int rz) {
        var world = player.getWorld();
        var dimId = DIM_ID.invoke(world);
        var worldLK = "{msg#common:world:" + dimId + "}";

        getLanguage().broadcastMessage(false, false, "region-discovered", worldLK, rx, rz, player.getName());

        var locale = LocaleService.locale(player);
        var title = getLanguage().getMessageComponent(locale, "region-discovered-title");
        var subtitle = getLanguage().getMessageComponent(locale, "region-discovered-subtitle", worldLK, rx, rz).toSingleLine();

        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.7f);


        TextSender.sendTitle(player, title, TextExaminer.examine(subtitle, locale), 5, 20, 5);
    }


    public DataManager getDataManager() {
        return getComponent(DataManager.class);
    }

    public ConfigurationManager getConfigManager() {
        return getComponent(ConfigurationManager.class);
    }

    interface RegionPos {
        static long hash(int x, int z) {
            return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
        }

        static long hash(Location loc) {
            var x = loc.getBlockX() >> 8;
            var z = loc.getBlockZ() >> 8;

            return hash(x, z);
        }

        static int regionX(long hash) {
            return (int) (hash & 0xFFFFFFFFL);
        }

        static int regionZ(long hash) {
            return (int) (hash >>> 32 & 0xFFFFFFFFL);
        }
    }

    @QuarkCommand(name = "dim-liberation")
    public static final class DimLiberationCommand extends ModuleCommand<Guns_DimensionLiberation> {
        @Override
        public void execute(CommandExecution context) {
            var sender = context.getSender();
            var template = getConfig().value("template").string();
            var list = queryRanks(sender.getName(), template, context.hasArgumentAt(0)).toString();

            var sb = new StringBuilder();
            var locale = LocaleService.locale(sender);

            var worlds = Bukkit.getWorlds();
            for (var i = 0; i < worlds.size(); i++) {
                if (i % 2 == 0 && i != 0) {
                    sb.append("\n");
                }
                sb.append(queryWorldStats(worlds.get(i), locale)).append("   ");
            }

            var temp = getLanguage().buildTemplate(LocaleService.locale(sender), getConfig().value("ui").string())
                    .formatted(sb.toString(), list);

            this.getLanguage().sendTemplate(sender, temp);
        }

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "-full");
        }

        private StringBuilder queryRanks(String target, String template, boolean full) {
            var sb = new StringBuilder();

            var sorted = new ArrayList<>(this.getModule().getDataManager().counts.entrySet());
            sorted.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            int limit = sorted.size();
            if (!full) {
                limit = Math.min(limit, 5);
            }

            for (int i = 0; i < limit; i++) {
                Map.Entry<String, Integer> entry = sorted.get(i);
                sb.append(template.formatted(i + 1, entry.getKey(), entry.getValue())).append('\n');
            }

            int position = -1;
            int score = -1;
            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Integer> entry = sorted.get(i);
                if (entry.getKey().equals(target)) {
                    position = i + 1;
                    score = entry.getValue();
                    break;
                }
            }

            sb.append("{;}\n");

            if (position != -1) {
                sb.append(template.formatted(position, target + "(您)", score));
            } else {
                sb.append(template.formatted("99+", "您", 0));
            }

            return sb;
        }

        private String queryWorldStats(World world, Locale locale) {
            var wid = DIM_ID.invoke(world);
            var worldLK = "{msg#common:world:" + wid + "}";

            var explored = getModule().getDataManager().getExploredData(world).size();
            var required = getModule().getConfigManager().required(DIM_ID.invoke(world));
            var process = MathHelper.clamp(explored / ((float) required), 0, 1);
            var formattedProcess = SharedObjects.NUMBER_FORMAT.format(process * 100) + "%";

            var title = getLanguage().getMessageComponent(locale, "stats", worldLK, explored, required, formattedProcess).toSingleLine();

            return ComponentSerializer.legacy(TextExaminer.examine(title, locale));

        }
    }


    public static final class ConfigurationManager extends ModuleComponent<Guns_DimensionLiberation> {
        public int required(String world) {
            return getConfig().getSection("goals").getInt(world, 1024);
        }

        public BarColor color(String world) {
            try {
                return BarColor.valueOf(getConfig().getSection("progress-colors").getString(world));
            } catch (Throwable e) {
                return BarColor.BLUE;
            }
        }

        public BarColor color(World world) {
            return color(DIM_ID.invoke(world));
        }

        public int multiplier(World world) {
            return getConfig().getSection("score-multiplier").getInt(DIM_ID.invoke(world), 1);
        }
    }


    public static final class DataManager extends ModuleComponent<Guns_DimensionLiberation> {
        private final Map<String, Long2ObjectOpenHashMap<String>> exploredData = new HashMap<>();
        private final Map<String, Long> playerPositionState = new HashMap<>();
        private final Map<String, Integer> counts = new HashMap<>();
        private boolean loaded = false;

        public Long2ObjectOpenHashMap<String> getExploredData(World world) {
            var wid = DIM_ID.invoke(world);
            return this.exploredData.computeIfAbsent(
                    wid,
                    k -> new Long2ObjectOpenHashMap<>(this.parent.getConfigManager()
                                                              .required(DIM_ID.invoke(world)))
            );
        }


        public boolean move(Player player) {
            var id = PlayerIdentificationService.transformPlayer(player);

            var hash = RegionPos.hash(player.getLocation());
            var lastHash = this.playerPositionState.computeIfAbsent(id, (s) -> -1L);

            if (hash == lastHash) {
                return false;
            }

            this.playerPositionState.put(id, lastHash);

            return true;

        }

        public void assign(Player player) {
            var hash = RegionPos.hash(player.getLocation());
            var world = player.getWorld();
            var wid = DIM_ID.invoke(world);
            var map = this.exploredData.computeIfAbsent(
                    wid,
                    k -> new Long2ObjectOpenHashMap<>(this.parent.getConfigManager().required(wid))
            );

            if (map.containsKey(hash)) {
                return;
            }

            var rx = RegionPos.regionX(hash);
            var rz = RegionPos.regionZ(hash);

            map.put(hash, player.getName());
            this.counts.put(player.getName(), this.counts.getOrDefault(player.getName(), 0) + this.parent.getConfigManager().multiplier(world)
            );

            this.parent.announce(player, rx, rz);
        }

        private void load() {
            this.counts.clear();
            this.exploredData.clear();
            NBTTagCompound entry = ModuleDataService.getEntry(this.parent.getFullId());
            for (String s : entry.getTagMap().keySet()) {
                this.counts.put(s, entry.getInteger(s));
            }

            var data = ModuleDataService.get(this.parent.getFullId() + ":data");
            for (String s : data.getTagMap().keySet()) {
                var map = data.getTable(s);
                var container = new Long2ObjectOpenHashMap<String>(this.parent.getConfigManager().required(s));

                map.getTagMap().forEach((k, v) -> container.put(Long.valueOf(k), ((NBTTagString) v).stringValue));

                this.exploredData.put(s, container);
            }

            loaded = true;
        }

        private void save() {
            var entry = ModuleDataService.getEntry(this.parent.getFullId());
            for (var s : this.counts.keySet()) {
                entry.setInteger(s, this.counts.get(s));
            }
            ModuleDataService.save(this.parent.getFullId());

            var data = ModuleDataService.get(this.parent.getFullId() + ":data");
            for (var s : this.exploredData.keySet()) {
                var map = data.getTable(s);
                var container = this.exploredData.get(s);

                container.forEach((k, v) -> map.setString(String.valueOf(k), v));

                data.setTable(s, map);
            }

            data.save();
        }

        @Override
        public void enable() {
            load();
        }

        @Override
        public void disable() {
            save();
        }
    }
}
