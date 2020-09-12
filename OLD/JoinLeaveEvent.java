package fr.milekat.cite_bungee.core.events;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public class JoinLeaveEvent implements Listener {
    @EventHandler
    public void b(LoginEvent event) {
        MainBungee.info("LoginEvent" + event.getConnection().getUniqueId().toString());
    }

    @EventHandler
    public void c(PostLoginEvent event) {
        MainBungee.info("PostLoginEvent" + event.getPlayer().getUniqueId().toString());
    }


    /**
     *      Quand quelqu'un rejoint le proxy
     *      - Vérif qu'il ne soit pas en liste d'approbation
     *      - Vérif qu'il ne soit pas Ban / TempBan
     *      - Init de startLogging pour le message de join / leave
     * @param event PostLoginEvent
     */
    @EventHandler
    public void onProxyJoin(PostLoginEvent event) throws ParseException {
        // Vérif que le joueur ne soit pas en approbation
        if (MainBungee.approbationList.contains(event.getPlayer().getUniqueId())) {
            event.getPlayer().disconnect(new TextComponent(MainBungee.prefixCmd
                    + System.lineSeparator() +
                    "§cVous faite l'objet d'une surveillance accrue !"
                    + System.lineSeparator() +
                    "§6Merci de faire une demande d'approbation sur notre site,"
                    + System.lineSeparator() +
                    "pour accéder à l'évènement en pleine sécurité§c:"
                    + System.lineSeparator() +
                    "§b§nweb.cite-balkoura.fr"));
            MainBungee.startLogging.put(event.getPlayer().getUniqueId(),false);
            return;
        }
        // Vérif que le joueur n'est pas ban
        if (MainBungee.ban.containsKey(event.getPlayer().getUniqueId())) {
            if (MainBungee.ban.get(event.getPlayer().getUniqueId()).equalsIgnoreCase("def")) {
                event.getPlayer().disconnect(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cVous êtes définitivement bannis !"
                        + System.lineSeparator() +
                        "§6Vous pouvez faire appel de cette décision dans l'onglet support de notre site§c."
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
            } else {
                Date ban = DateMilekat.getDate(MainBungee.ban.get(event.getPlayer().getUniqueId()));
                event.getPlayer().disconnect(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cVous êtes actuellement suspendu!"
                        + System.lineSeparator() +
                        "§6Délais de suspension restant§c: §b" + DateMilekat.periodToString(ban) + "§c."
                        + System.lineSeparator() +
                        "§6Vous pouvez faire appel de cette décision dans l'onglet support de notre site§c"
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
            }
            MainBungee.startLogging.put(event.getPlayer().getUniqueId(),false);
            return;
        }
        // Init de startLogging pour éviter le msg de leave en cas de kick ou msg de join switch server
        MainBungee.startLogging.put(event.getPlayer().getUniqueId(),true);
    }

    /**
     *      Un joueur a réalisé la connection !
     * @param event ServerConnectEvent
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onConnect(ServerConnectedEvent event){
        if (MainBungee.startLogging.getOrDefault(event.getPlayer().getUniqueId(),false)) {
            MainBungee.startLogging.remove(event.getPlayer().getUniqueId());
            // Redis
            JedisPub.sendRedis("join_notif:" + event.getPlayer().getName());
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                setPlayerInfo(event.getPlayer());
                PreparedStatement q = connection.prepareStatement(
                        "INSERT INTO `chat`(`player_id`, `msg`, `msg_type`, `date_msg`) VALUES (" +
                        "(SELECT `player_id` FROM `player` WHERE `uuid` = '" + event.getPlayer().getUniqueId() +
                        "'),'join',5,?) RETURNING `msg_id`;" +
                        "UPDATE `player` SET `online` = '1' WHERE `uuid` = ?;");
                q.setString(1, DateMilekat.setDateNow());
                q.setString(2, event.getPlayer().getUniqueId().toString());
                q.execute();
                q.getResultSet().last();
                new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
                q.close();
                // Call de l'event custom
                ProxyServer.getInstance().getPluginManager().callEvent(new CustomJoinEvent(event.getPlayer()));
            } catch (SQLException throwables) {
                ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + "Erreur SQL onConnect :");
                throwables.printStackTrace();
            }
        }
    }

    /**
     *      Désactive 'online', et insert le message de déconnection
     * @param event PlayerDisconnectEvent
     */
    @EventHandler
    public void onProxyLeave(PlayerDisconnectEvent event) {
        if (MainBungee.startLogging.getOrDefault(event.getPlayer().getUniqueId(),true)) {
            ProxyServer.getInstance().getPluginManager().callEvent(new CustomLeaveEvent(event.getPlayer()));
            // Redis
            JedisPub.sendRedis("quit_notif:" + event.getPlayer().getName());
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                PreparedStatement q = connection.prepareStatement(
                        "INSERT INTO `chat`(`player_id`, `msg`, `msg_type`, `date_msg`) " +
                        "VALUES ((SELECT `player_id` FROM `player` WHERE `uuid` = '" + event.getPlayer().getUniqueId() +
                        "'),'quit',5,?) RETURNING `msg_id`;" +
                        "UPDATE `player` SET `online` = '0' WHERE `uuid` = ?;");
                q.setString(1, DateMilekat.setDateNow());
                q.setString(2, event.getPlayer().getUniqueId().toString());
                q.execute();
                q.getResultSet().last();
                new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
                q.close();
            } catch (SQLException throwables) {
                ProxyServer.getInstance().getLogger().warning(
                        MainBungee.prefixConsole + "Erreur SQL onProxyLeave :");
                throwables.printStackTrace();
            }
        }
    }

    /**
     *      Enregistrement d'un joueur, ou update du name si l'uuid a changé !
     * @param p jouer à reg
     */
    private void setPlayerInfo(ProxiedPlayer p) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement(
                    "INSERT INTO `player`(`uuid`, `name`, `date_join`) " +
                    "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?;");
            q.setString(1,p.getUniqueId().toString());
            q.setString(2,p.getName());
            q.setString(3,DateMilekat.setDateNow());
            q.setString(4,p.getName());
            q.execute();
            q.close();
        } catch (SQLException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole +
                    "Erreur lors de l'enregistrement de : " + p.getName() + ".");
        }
    }
}
