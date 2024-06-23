package org.tbstcraft.quark.automatic;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.language.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.internal.task.TaskService;

import java.util.*;

//todo:config countdown
@QuarkModule(version = "1.0.0")
@CommandProvider({GarbageCleaner.CleanCommand.class})
@SuppressWarnings("ClassCanBeRecord")
public final class GarbageCleaner extends PackageModule implements Runnable {
    private static final int[] REMAIN = new int[]{120, 60, 30, 20, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
    private final Set<Cleaner> cleaners = new HashSet<>(4);
    private List<String> whitelistedWorlds;

    @Override
    public void enable() {
        TaskService.cancelTask("cleaner::timer");
        ConfigurationSection config = this.getConfig();
        this.whitelistedWorlds = config.getStringList("world-whitelist");
        this.cleaners.clear();
        if (config.getBoolean("clean-drops")) {
            boolean b1 = config.getBoolean("ignore-enchant-item");
            this.addCleaner(new DropCleaner(config.getStringList("item-whitelist"), b1));

        }
        if (config.getBoolean("clean-dense-entity")) {
            this.addCleaner(new DenseEntityCleaner(config.getInt("dense-entity-max-count")));
        }
        TaskService.timerTask("cleaner::timer", 0, this.getConfig().getInt("clean-interval"), this);
    }

    @Override
    public void disable() {
        TaskService.cancelTask("cleaner::timer");
    }

    private void addCleaner(Cleaner c) {
        if (this.cleaners.contains(c)) {
            return;
        }
        this.cleaners.add(c);
    }

    @Override
    public void run() {
        this.submit();
    }

    public void submit() {
        for (int i : REMAIN) {
            TaskService.laterTask((120 - i) * 20, new WarningCallback(i, this.getLanguage()));
        }
        TaskService.laterTask(120 * 20, cleanTask());
    }

    private Runnable cleanTask() {
        return () -> {
            this.getLanguage().broadcastMessage(false,false, "restart");
            for (World world : Bukkit.getWorlds()) {
                if (this.whitelistedWorlds.contains(world.getName())) {
                    continue;
                }

                for (Cleaner cleaner : this.cleaners) {
                    cleaner.clean(world);
                }
            }
            this.getLanguage().broadcastMessage(false,false, "complete");
        };
    }


    //cleaner
    private abstract static class Cleaner {
        public abstract void clean(World world);

        public int hashCode() {
            return this.toString().hashCode();
        }

        @Override
        public String toString() {
            return this.getClass().getName();
        }

        @Override
        public boolean equals(Object obj) {
            return this.getClass().isInstance(obj);
        }
    }

    private static final class DropCleaner extends Cleaner {
        private final List<String> whiteList;
        private final boolean ignoreEnchanted;

        DropCleaner(List<String> whiteList, boolean ignoreEnchanted) {
            this.whiteList = whiteList;
            this.ignoreEnchanted = ignoreEnchanted;
        }

        @Override
        public void clean(World world) {
            for (Entity e : world.getEntities()) {
                if (!(e instanceof Item item)) {
                    continue;
                }
                String type = item.getItemStack().getType().getKey().getKey();
                if (this.whiteList.contains(type)) {
                    continue;
                }
                if (this.ignoreEnchanted && !item.getItemStack().getEnchantments().isEmpty()) {
                    continue;
                }
                e.remove();
            }
        }
    }

    private static final class DenseEntityCleaner extends Cleaner {
        private final int maxCount;

        DenseEntityCleaner(int maxCount) {
            this.maxCount = maxCount;
        }

        @Override
        public void clean(World world) {
            for (Chunk chunk : world.getLoadedChunks()) {
                Map<EntityType, Integer> map = new HashMap<>();
                for (Entity entity : chunk.getEntities()) {
                    if (entity instanceof Item) {
                        continue;
                    }
                    if (entity instanceof Player) {
                        continue;
                    }
                    EntityType type = entity.getType();
                    int count = map.getOrDefault(type, 0);
                    if (count > this.maxCount) {
                        entity.remove();
                        map.put(type, count);
                        continue;
                    }
                    map.put(type, count + 1);
                }
            }
        }
    }

    //other
    private static final class WarningCallback implements Runnable {
        private final int remain;
        private final LanguageEntry entry;

        private WarningCallback(int remain, LanguageEntry entry) {
            this.remain = remain;
            this.entry = entry;
        }

        @Override
        public void run() {
            this.entry.broadcastMessage(false,false, "remain", this.remain);
        }
    }

    @QuarkCommand(name = "clean", op = true)
    public static final class CleanCommand extends ModuleCommand<GarbageCleaner> {

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            switch (args[0]) {
                case "now" -> this.getModule().cleanTask().run();
                case "schedule" -> this.getModule().submit();
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length != 1) {
                return;
            }
            tabList.add("now");
            tabList.add("schedule");
        }
    }
}
