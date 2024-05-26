package org.tbstcraft.quark.security;

import me.gb2022.apm.local.MappedBroadcastEvent;
import me.gb2022.apm.local.PluginMessageHandler;
import me.gb2022.commons.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.*;
import org.tbstcraft.quark.SharedObjects;
import org.tbstcraft.quark.framework.command.CommandProvider;
import org.tbstcraft.quark.framework.command.ModuleCommand;
import org.tbstcraft.quark.framework.command.QuarkCommand;
import org.tbstcraft.quark.framework.data.config.LanguageEntry;
import org.tbstcraft.quark.framework.module.PackageModule;
import org.tbstcraft.quark.framework.module.QuarkModule;
import org.tbstcraft.quark.framework.module.services.ModuleService;
import org.tbstcraft.quark.framework.module.services.ServiceType;
import org.tbstcraft.quark.internal.data.PlayerDataService;
import org.tbstcraft.quark.service.network.HttpService;
import org.tbstcraft.quark.service.network.SMTPService;
import org.tbstcraft.quark.service.network.http.HttpHandlerContext;
import org.tbstcraft.quark.service.network.http.HttpRequest;
import org.tbstcraft.quark.util.platform.PlayerUtil;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

//todo:二步验证换绑
@CommandProvider({AccountActivation.AccountCommand.class})
@QuarkModule(version = "1.0.2", beta = true)
@ModuleService({ServiceType.EVENT_LISTEN, ServiceType.PLUGIN_MESSAGE})
public final class AccountActivation extends PackageModule {
    private final Set<String> checkCache = new HashSet<>();
    private final Set<String> disabledPlayers = new HashSet<>();
    private String verifyHTML;
    private String verifyResultHTML;

    @Override
    public void enable() {
        HttpService.registerHandler(this);
        try {
            InputStream stream = this.getResource("/verify.html");
            this.verifyHTML = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            stream.close();

            InputStream stream2 = this.getResource("/verify_result.html");
            this.verifyResultHTML = new String(stream2.readAllBytes(), StandardCharsets.UTF_8);
            stream2.close();

            for (Player p : Bukkit.getOnlinePlayers()) {
                this.testPlayer(p);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void disable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            this.unfreeze(p);
        }
    }

    //status
    @EventHandler
    public void onPlayerLeaved(PlayerQuitEvent event) {
        this.unfreeze(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.testPlayer(event.getPlayer());
    }


    public void testPlayer(Player p) {
        if (this.checkCache.contains(p.getName())) {
            return;
        }
        AccountStatus status = AccountStatus.loadStatus(p.getName());
        if (status.shouldAllowPlayerAction()) {
            p.setGameMode(Bukkit.getServer().getDefaultGameMode());
            return;
        }
        this.freeze(p, status);
    }

    private void freeze(Player player, AccountStatus status) {
        player.setGameMode(GameMode.SPECTATOR);
        if (status == AccountStatus.UNLINKED) {
            getLanguage().sendMessageTo(player, "link-hint");
        } else {
            getLanguage().sendMessageTo(player, "verify-hint");
        }
        this.checkCache.add(player.getName());
        this.disabledPlayers.add(player.getName());
    }

    private void unfreeze(Player player) {
        this.checkCache.remove(player.getName());
        this.disabledPlayers.remove(player.getName());
    }


    //disable
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        this.detectPlayerEvent(event, event.getPlayer(), true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/account")) {
            return;
        }
        this.detectPlayerEvent(event, event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerGamemodeChange(PlayerGameModeChangeEvent event) {
        this.detectPlayerEvent(event, event.getPlayer(), true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        this.detectPlayerEvent(event, event.getPlayer(), false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        this.detectPlayerEvent(event, event.getPlayer(), false);
    }

    public void detectPlayerEvent(Cancellable event, Player p, boolean sendMessage) {
        if (!this.disabledPlayers.contains(p.getName())) {
            return;
        }
        event.setCancelled(true);
        if (!sendMessage) {
            return;
        }
        this.getLanguage().sendMessageTo(p, "interaction-block");
    }


    //mails and links
    @HttpRequest("/verify_mail")
    public void verifyLink(HttpHandlerContext context) {
        try {
            boolean b = ActivateService.verify(context.getParam("code"));

            String content = this.getLanguage().buildUI(this.verifyResultHTML, "zh_cn");

            if (b) {
                content = content.replace("{title}", this.getLanguage().getMessage("zh_cn", "result-html-success-title"));
                content = content.replace("{content}", this.getLanguage().getMessage("zh_cn", "result-html-success-content"));
            } else {
                content = content.replace("{title}", this.getLanguage().getMessage("zh_cn", "result-html_failed-title"));
                content = content.replace("{content}", this.getLanguage().getMessage("zh_cn", "result-html-failed-content"));
            }

            content = content.replace("{code}", context.getParam("code"));

            context.setData(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendVerifyMail(Player player, String mailBox, String code) {
        SharedObjects.SHARED_THREAD_POOL.submit(() -> {
            String content = this.getLanguage().buildUI(this.verifyHTML, player.getLocale());
            int safetyCode = new Random().nextInt(10000, 99999);
            content = content.replace("{player}", player.getName());
            content = content.replace("{link}", code);
            content = content.replace("{safety_code}", String.valueOf(safetyCode));
            String subject = this.getLanguage().getMessage(player.getLocale(), "verify-title");
            if (SMTPService.sendMailTo(mailBox, subject, content)) {
                this.getLanguage().sendMessageTo(player, "msg-send-complete", mailBox, safetyCode);
                return;
            }
            this.getLanguage().sendMessageTo(player, "msg-send-failed", mailBox);
        });
    }

    @PluginMessageHandler("ip:change")
    public void onIpFailure(MappedBroadcastEvent event) {
        AccountStatus.unverify(event.getProperty("player", String.class));
        Player p = PlayerUtil.strictFindPlayer(event.getProperty("player", String.class));
        if (p == null) {
            return;
        }
        p.kickPlayer(this.getLanguage().getMessage(p.getLocale(), "kick_info"));
    }

    //integrated classes
    private enum AccountStatus {
        UNVERIFIED("unverified"),
        VERIFIED("verified"),
        UNLINKED("unlinked");

        final String id;

        AccountStatus(String id) {
            this.id = id;
        }

        static AccountStatus loadStatus(String player) {
            NBTTagCompound tag = PlayerDataService.getEntry(player, "account_activation");
            if (!tag.hasKey("account_status")) {
                tag.setString("account_status", UNLINKED.getId());
                PlayerDataService.save(player);
                return UNLINKED;
            }
            if (!tag.hasKey("mail")) {
                tag.setString("account_status", UNLINKED.getId());
                PlayerDataService.save(player);
                return UNLINKED;
            }
            return fromId(tag.getString("account_status"));
        }

        static AccountStatus fromId(String id) {
            return switch (id) {
                case "unverified" -> UNVERIFIED;
                case "unlinked" -> UNLINKED;
                case "verified" -> VERIFIED;
                default -> throw new IllegalStateException("Unexpected value: " + id);
            };
        }

        static void link(String player, String mail) {
            NBTTagCompound tag = PlayerDataService.getEntry(player, "account_activation");
            tag.setString("account_status", VERIFIED.getId());
            tag.setString("mail", mail);
            PlayerDataService.save(player);
        }

        static boolean isLinked(String player) {
            return loadStatus(player) != UNLINKED;
        }

        static void verify(String player) {
            NBTTagCompound tag = PlayerDataService.getEntry(player, "account_activation");
            tag.setString("account_status", VERIFIED.getId());
            PlayerDataService.save(player);
        }

        public static void unverify(String player) {
            NBTTagCompound tag = PlayerDataService.getEntry(player, "account_activation");
            tag.setString("account_status", UNVERIFIED.getId());
            PlayerDataService.save(player);
        }

        boolean shouldAllowPlayerAction() {
            return switch (this) {
                case UNLINKED, UNVERIFIED -> false;
                case VERIFIED -> true;
            };
        }

        public String getId() {
            return id;
        }
    }

    private interface ActivateService {
        HashMap<String, Runnable> CALLBACK = new HashMap<>(32);

        private static String generateCode(Runnable callback) {
            UUID randomUUID = UUID.randomUUID();
            String code = randomUUID.toString().replaceAll("-", "");
            CALLBACK.put(code, callback);
            return code;
        }

        static boolean verify(String code) {
            if (!CALLBACK.containsKey(code)) {
                return false;
            }
            CALLBACK.get(code).run();
            CALLBACK.remove(code);
            return true;
        }

        static String generateActivationLink(String prefix, Runnable callback) {
            return prefix + "/auth/verify?code=" + generateCode(callback);
        }


        static String linkAccount(String prefix, Player player, LanguageEntry entry, String mail) {
            return generateActivationLink(prefix, () -> {
                AccountStatus.link(player.getName(), mail);
                entry.sendMessageTo(player, "verified");
            });
        }

        static String verifyAccount(String prefix, Player player, LanguageEntry entry) {
            return generateActivationLink(prefix, () -> {
                AccountStatus.verify(player.getName());
                entry.sendMessageTo(player, "verified");
            });
        }
    }

    @QuarkCommand(name = "link")
    public static final class LinkCommand extends ModuleCommand<AccountActivation> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            String prefix = this.getConfig().getString("verify_link_delegation");
            if (AccountStatus.isLinked(sender.getName())) {
                this.getLanguage().sendMessageTo(sender, "link_failed");
            }
            String code = ActivateService.linkAccount(prefix, ((Player) sender), this.getLanguage(), args[1]);
            this.getModule().sendVerifyMail(((Player) sender), args[1], code);
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 2) {
                tabList.add("example@example.com");
                tabList.add("@163.com");
                tabList.add("@126.com");
                tabList.add("@qq.com");
                tabList.add("@gmail.com");
            }
        }
    }

    @QuarkCommand(name = "verify")
    public static final class VerifyCommand extends ModuleCommand<AccountActivation> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            NBTTagCompound tag = PlayerDataService.getEntry(sender.getName(), "account_activation");
            String mail = tag.getString("mail");
            if (!AccountStatus.isLinked(sender.getName())) {
                this.getLanguage().sendMessageTo(sender, "verify_failed", mail);
            }
            String prefix = this.getConfig().getString("verify_link_delegation");
            String code = ActivateService.verifyAccount(prefix, ((Player) sender), this.getLanguage());

            this.getModule().sendVerifyMail(((Player) sender), mail, code);
        }
    }


    @QuarkCommand(name = "account", subCommands = {VerifyCommand.class, LinkCommand.class})
    public static final class AccountCommand extends ModuleCommand<AccountActivation> {
        @Override
        public void onCommand(CommandSender sender, String[] args) {
            if (args[0].equals("unverify")) {
                AccountStatus.unverify(sender.getName());
                this.getLanguage().sendMessageTo(sender, "unverify_success");
            }
        }

        @Override
        public void onCommandTab(CommandSender sender, String[] buffer, List<String> tabList) {
            if (buffer.length == 1) {
                tabList.add("link");
                tabList.add("verify");
                tabList.add("unverify");
            }
            if (buffer.length == 2 && Objects.equals(buffer[0], "link")) {
                tabList.add("example@example.com");
            }
        }
    }
}
