package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class ModoChat extends Command implements TabExecutor {
    private final ChatSend chatSend;

    public ModoChat() {
        super("chatmodo","modo.chat.command.mods");
        chatSend = new ChatSend();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (args.length != 1) {
                sendHelp(sender);
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (args[0].equalsIgnoreCase("on")) {
                MainBungee.profiles.get(player.getUniqueId()).setModson(true);
                setModsForP(player, true);
                chatSend.RlChatFor(sender.getName(), 30);
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "Mode modérateur chat activé"));
            } else if (args[0].equalsIgnoreCase("off")) {
                MainBungee.profiles.get(player.getUniqueId()).setModson(false);
                setModsForP(player, false);
                chatSend.RlChatFor(player.getName(), 30);
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "Mode modérateur chat désactivé"));
            }
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/chatmode on:§r Passage en chat Modo."));
        sender.sendMessage(new TextComponent("§6/chatmode off:§r Passage en chat Joueur."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return new ArrayList<>(Arrays.asList("on", "off"));
        }
        return new ArrayList<>();
    }

    /**
     *      Update SQL du mods pour le joueur
     * @param p cible
     * @param mode null, désactivé, 1 activé
     */
    private void setModsForP(ProxiedPlayer p, boolean mode) {
        Connection sql = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = sql.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                    "player` SET modson = ? WHERE `uuid` = '" + p.getUniqueId() + "';");
            q.setBoolean(1,mode);
            q.execute();
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de l'update du ModsOn de :" + p + ".");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }
}