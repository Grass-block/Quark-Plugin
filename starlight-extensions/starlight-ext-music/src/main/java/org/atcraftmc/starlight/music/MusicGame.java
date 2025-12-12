package org.atcraftmc.starlight.music;

import me.gb2022.modular.module.ApplicationModule;
import org.atcraftmc.qlib.command.QuarkCommand;
import org.atcraftmc.qlib.command.execute.CommandExecution;
import org.atcraftmc.qlib.command.execute.CommandSuggestion;
import org.atcraftmc.qlib.language.LanguageItem;
import org.atcraftmc.starlight.api.customization.CustomItem;
import org.atcraftmc.starlight.core.custom.CustomBlockService;
import org.atcraftmc.starlight.foundation.command.CommandProvider;
import org.atcraftmc.starlight.foundation.command.ModuleCommand;
import org.atcraftmc.starlight.framework.module.SLPackageModule;
import org.atcraftmc.starlight.music.game.MusicGameData;
import org.atcraftmc.starlight.music.game.MusicGameMeta;
import org.atcraftmc.starlight.music.game.MusicGameSession;
import org.atcraftmc.starlight.music.resolve.MusicData;
import org.atcraftmc.starlight.util.PlayerMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

@ApplicationModule(id = "music-game")
@CommandProvider(MusicGame.MusicGameCommand.class)
public class MusicGame extends SLPackageModule implements PlayerUIRenderer {
    private final CustomItem item_DiamondSword = new MusicGamePunchItem("m-puncher-diamond", Material.DIAMOND_SWORD);
    private final CustomItem item_GoldenSword = new MusicGamePunchItem("m-puncher-gold", Material.GOLDEN_SWORD);
    private final CustomItem item_IronSword = new MusicGamePunchItem("m-puncher-iron", Material.IRON_SWORD);

    private final PlayerMap<MusicGameSession> sessions = new PlayerMap<>();


    @Override
    public void enable() throws Exception {
        CustomBlockService.instance().registerItem(this.item_DiamondSword);
        CustomBlockService.instance().registerItem(this.item_GoldenSword);
        CustomBlockService.instance().registerItem(this.item_IronSword);
    }

    @Override
    public void disable() throws Exception {
        CustomBlockService.instance().unregisterItem("m-puncher-diamond");
        CustomBlockService.instance().unregisterItem("m-puncher-gold");
        CustomBlockService.instance().unregisterItem("m-puncher-iron");

        for (var session : this.sessions.values()) {
            session.destroySession();
        }
    }

    private void punch(MusicGamePunchItem musicGamePunchItem, Player player, Location loc) {
        if (!this.sessions.contains(player)) {
            return;
        }

        this.sessions.get(player).punch(musicGamePunchItem, player, loc);
    }

    public void addSession(Player player, MusicGameSession session) {
        this.sessions.put(player, session);
    }

    @Override
    public void renderUI(Player player, MusicData currentMusic, int currentTick, boolean pause) {

    }

    @QuarkCommand(name = "music-game", permission = "+starlight.music.game")
    public static final class MusicGameCommand extends ModuleCommand<MusicGame> {

        @Override
        public void suggest(CommandSuggestion suggestion) {
            suggestion.suggest(0, "create");

            suggestion.matchArgument(0, "create", (ctx) -> {
                MusicCommandDispatcher.suggestMusic(suggestion, 1);
            });
        }

        @Override
        public void execute(CommandExecution context) {
            var player = context.requireSenderAsPlayer();


            switch (context.requireEnum(0, "create")) {
                case "create" -> {
                    var data = new MusicGameData();

                    data.music = context.requireArgumentAt(1);
                    data.meta = new MusicGameMeta("Test", "Test", "Test");

                    var d = MusicService.instance().dispatch(MusicCommandDispatcher.selectMusic(context, 0));

                    var session = new MusicGameSession((p, currentMusic, currentTick, pause) -> {
                    }, d, player.getLocation(), context.requireSenderAsPlayer(), true);

                    session.startSession();

                    this.getModule().addSession(player, session);
                }
            }
        }
    }

    public class MusicGamePunchItem extends CustomItem {
        private final Material item;

        public MusicGamePunchItem(String id, Material item) {
            super(id);
            this.item = item;
        }

        @Override
        public LanguageItem getDisplayName(ItemStack stack) {
            return MusicGame.this.language().item(this.id + "-name");
        }

        @Override
        public LanguageItem getDescription(ItemStack stack) {
            return MusicGame.this.language().item(this.id + "-lore");
        }

        @Override
        public void onItemInteractAtBlock(Player player, ItemStack stack, Block target, Action action) {
            MusicGame.this.punch(this, player, target.getLocation());
        }

        @Override
        public void onItemInteractAtAir(Player player, ItemStack stack, Action action) {
            MusicGame.this.punch(this, player, player.getLocation());
        }

        @Override
        public Material getActualBlock() {
            return this.item;
        }
    }

}
