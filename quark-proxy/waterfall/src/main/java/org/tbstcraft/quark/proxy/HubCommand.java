package org.tbstcraft.quark.proxy;

import me.gb2022.apm.remote.protocol.BufferUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class HubCommand extends Command {
    private final ProxyServer server;

    public HubCommand(ProxyServer server,String id) {
        super(id);
        this.server = server;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(!(sender instanceof ProxiedPlayer p)){
            return;
        }
        p.connect(this.server.getServerInfo("lobby"));
    }

    public static void register(Plugin plugin){
        ProxyServer server=plugin.getProxy();
        server.getPluginManager().registerCommand(plugin, new HubCommand(server, "hub"));
        server.getPluginManager().registerCommand(plugin, new HubCommand(server, "lobby"));
    }
}
