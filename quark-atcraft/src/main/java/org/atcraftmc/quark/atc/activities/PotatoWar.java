package org.atcraftmc.quark.atc.activities;

import me.gb2022.commons.nbt.NBTTagCompound;
import me.gb2022.commons.reflect.AutoRegister;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.tbstcraft.quark.foundation.command.CommandProvider;
import org.tbstcraft.quark.foundation.command.ModuleCommand;
import org.tbstcraft.quark.foundation.command.QuarkCommand;
import org.tbstcraft.quark.data.language.Language;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.data.ModuleDataService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkModule(version = "1.0")
@AutoRegister(ServiceType.EVENT_LISTEN)
@CommandProvider(PotatoWar.QueryCommand.class)
public final class PotatoWar extends PackageModule {
    Map<String, Integer> counts = new HashMap<>();

    @Override
    public void enable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());
        for (String s : entry.getTagMap().keySet()) {
            this.counts.put(s, entry.getInteger(s));
        }
    }

    @Override
    public void disable() {
        NBTTagCompound entry = ModuleDataService.getEntry(this.getFullId());
        for (String s : this.counts.keySet()) {
            entry.setInteger(s, this.counts.get(s));
        }
        ModuleDataService.save(this.getFullId());
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        String temp = Language.generateTemplate(this.getConfig(), "announce-ui");
        this.getLanguage().sendTemplate(event.getPlayer(), temp);
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }

        Block b = event.getClickedBlock();

        if (b.getType() != Material.POTATOES) {
            return;
        }

        Ageable data = ((Ageable) b.getBlockData());
        if (data.getAge() != data.getMaximumAge()) {
            return;
        }


        int count = this.counts.getOrDefault(event.getPlayer().getName(), 0);
        count++;
        this.counts.put(event.getPlayer().getName(), count);
    }

    @QuarkCommand(name = "potato-war")
    public static final class QueryCommand extends ModuleCommand<PotatoWar> {
        private StringBuilder query(String target, String template) {
            StringBuilder sb = new StringBuilder();

            List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(this.getModule().counts.entrySet());
            sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

            for (int i = 0; i < Math.min(5, sortedEntries.size()); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);
                sb.append(template.formatted(i + 1, entry.getKey(), entry.getValue())).append('\n');
            }

            int position = -1;
            int score = -1;
            for (int i = 0; i < sortedEntries.size(); i++) {
                Map.Entry<String, Integer> entry = sortedEntries.get(i);
                if (entry.getKey().equals(target)) {
                    position = i + 1;
                    score = entry.getValue();
                    break;
                }
            }

            sb.append("{;}\n");

            if (position != -1) {
                sb.append(template.formatted(position, target + "(You)", score));
            } else {
                sb.append(template.formatted("99+", "You", 0));
            }

            return sb;
        }

        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String template = getConfig().getString("template");
            String list = query(sender.getName(), template).toString();

            String temp = Language.generateTemplate(this.getConfig(), "ui", (s -> s.formatted(list)));

            this.getLanguage().sendTemplate(sender, temp);
        }
    }
}
