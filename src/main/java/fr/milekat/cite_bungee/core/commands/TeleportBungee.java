package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.core.obj.Profil;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class TeleportBungee extends Command implements TabExecutor {
    public TeleportBungee() {
        super("tpb", "modo.bungee.teleport");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Profil profil = MainBungee.profiles.getOrDefault(((ProxiedPlayer) sender).getUniqueId(),null);
        if (profil==null) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cErreur serveur !"));
            return;
        }
        if (args.length <= 1) {
            sendHelp(sender);
            return;
        }
        if (sender.getName().equalsIgnoreCase(args[0])) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu ne peux pas te téléporté à toi même"));
            return;
        }
        if (!MainBungee.joueurslist.containsKey(args[0])) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cLe joueur est introuvable."));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        ((ProxiedPlayer) sender).connect(target.getServer().getInfo());
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd +"§fVous avez été téléporté sur le serveur de " + args[0]));
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/tpb <Destinataire> :§rTe téléporte au joueur sur le proxy"));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return MainBungee.playerListFull(args[0]);
        }
        return new ArrayList<>();
    }
}
