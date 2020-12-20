package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Reply extends Command {
    public Reply() {
        super("r", "", "reply", "reponse");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer p = (ProxiedPlayer) sender;
        if (MainBungee.profiles.get(p.getUniqueId()).isMute()) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu es mute !"));
            return;
        }
        if (args.length < 1) {
            sendHelp(sender);
            return;
        }
        ChatSend chatSend = new ChatSend();
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("SELECT c.`msg_id`, c.`dest_id`, p.`name` " +
                    "FROM `balkoura_chat` c LEFT JOIN `balkoura_player` p ON c.dest_id=p.player_id " +
                    "WHERE c.`msg_type` = '2' AND c.`player_id` = " +
                    "(SELECT `player_id` FROM `balkoura_player` WHERE `uuid` = '" + p.getUniqueId() +
                    "') ORDER BY `msg_id` DESC LIMIT 0, 1");
            q.execute();
            PreparedStatement q2 = connection.prepareStatement("SELECT c.`msg_id`, c.`player_id`, p.`name` " +
                    "FROM `balkoura_chat` c LEFT JOIN `balkoura_player` p ON c.dest_id=p.player_id " +
                    "WHERE c.`msg_type` = '2' AND c.`dest_id` = " +
                    "(SELECT `player_id` FROM `balkoura_player` WHERE `uuid` = '" + p.getUniqueId() +
                    "') ORDER BY `msg_id` DESC LIMIT 0, 1");
            q2.execute();
            if (!q.getResultSet().last() && !q2.getResultSet().last()) {
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cAucun correspondant trouvé."));
                q.close();
                q2.close();
                return;
            }
            q.getResultSet().last();
            q2.getResultSet().last();
            String destId;
            String destname;
            if (q.getResultSet().getInt(1) > q2.getResultSet().getInt(1)) {
                destId = q.getResultSet().getString("dest_id");
                destname = q.getResultSet().getString("name");
            } else {
                destId = q2.getResultSet().getString("player_id");
                destname = q.getResultSet().getString("name");
            }
            StringBuilder sb = new StringBuilder();
            for (String loop : args) {
                sb.append(loop);
                sb.append(" ");
            }
            int id = insertSQLNewMp(p, sb.toString(), destId);
            q.close();
            // Send du message au joueur, au dest et au réseau (rédis)
            chatSend.sendChatFor(sender.getName(), id, false);
            chatSend.sendChatFor(destname, id, true);
            JedisPub.sendRedis("new_mp#:#" + id);
        } catch (SQLException throwables) {
            MainBungee.warning(MainBungee.prefixCmd + "Erreur SQL commande reply.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "Aide pour la commande reponse"));
        sender.sendMessage(new TextComponent(
                "§6/r <Message>:§r envoyer un message privé à votre dernier correspondant."));
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
                    "chat`(`player_id`, `msg`, `msg_type`, `date_msg`, `dest_id`) VALUES (" +
                    "(SELECT `player_id` FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` ='"
                    + sender.getUniqueId().toString() + "'), ?, ?, ?, ?) RETURNING msg_id;");
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
}
