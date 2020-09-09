package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class Message extends Command implements TabExecutor {
    public Message() {
        super("m", "", "mp", "dm", "directmessage", "msg", "message", "private", "tell", "w", "whisper");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (MainBungee.profiles.get(p.getUniqueId()).isMute()) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu es mute !"));
            return;
        }
        if (args.length < 2) {
            sendHelp(sender);
            return;
        }
        if (sender.getName().equalsIgnoreCase(args[0])) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu ne peux t'envoyer de MP !"));
            return;
        }
        ChatSend chatSend = new ChatSend();
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("SELECT `" + MainBungee.SQLPREFIX +
                    "player_id` FROM `player` WHERE SOUNDEX(`name`) = SOUNDEX('" + args[0] + "')");
            q.execute();
            if (!q.getResultSet().next()) {
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cLe joueur est introuvable."));
                q.close();
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (String loop : args) {
                if (!loop.equals(args[0])) {
                    sb.append(loop);
                    sb.append(" ");
                }
            }
            int id = insertSQLNewMp(p, sb.toString(), q.getResultSet().getString("player_id"));
            q.close();
            // Send du message au joueur, au dest et au réseau (rédis)
            chatSend.sendChatFor(sender.getName(), id, false);
            chatSend.sendChatFor(args[0], id, true);
            JedisPub.sendRedis("new_mp#:#" + id);
        } catch (SQLException throwables) {
            MainBungee.warning(MainBungee.prefixCmd + "Erreur SQL commande message.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/mp <Destinataire> <Message>:§r envoyer message privé au destinataire."));
    }

    /**
     *      Insert d'un MP dans le SQL
     * @param sender joueur qui envoi le msg
     * @param msg message
     * @param dest joueur qui recoit le msg
     * @return id du message
     */
    private int insertSQLNewMp(ProxiedPlayer sender, String msg, String dest) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "chat`(`player_id`, `msg`, `msg_type`, `date_msg`, `dest_id`) VALUES ((SELECT `player_id` FROM `"
                    + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" + sender.getUniqueId().toString() +
                    "'), ?, ?, ?, ?) RETURNING msg_id;");
            q.setString(1, msg);
            q.setInt(2, 2);
            q.setString(3, DateMilekat.setDateNow());
            q.setString(4, dest);
            q.execute();
            q.getResultSet().next();
            int id = q.getResultSet().getInt(1);
            q.close();
            return id;
        } catch (SQLException throwables) {
            MainBungee.warning(MainBungee.prefixCmd + "Erreur lors de l'injection d'un message privé dans le SQL.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        return 0;
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return MainBungee.playerListFull(args[0]);
        }
        return new ArrayList<>();
    }
}
