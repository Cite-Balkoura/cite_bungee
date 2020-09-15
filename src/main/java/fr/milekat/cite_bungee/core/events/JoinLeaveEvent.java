package fr.milekat.cite_bungee.core.events;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class JoinLeaveEvent implements Listener {
    /**
     *      Quand quelqu'un essaie de rejoindre le proxy
     *      - Vérif que la maintenance n'est pas activée
     *      - Vérif qu'il ne soit pas Ban / TempBan
     *      - Vérif qu'il soit inscrit
     *      - Init de startLogging pour le message de join / leave
     * @param event LoginEvent
     */
    @EventHandler
    public void onTryJoin(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        if (MainBungee.opendate.getTime() > new Date().getTime()) {
            if (!MainBungee.profiles.containsKey(uuid) || !MainBungee.profiles.get(uuid).isMaintenance()) {
                event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cServeur en préparation !"
                        + System.lineSeparator() +
                        "§6Délais avant l'ouverture§c: §b" + DateMilekat.reamingToStrig(MainBungee.opendate) + "§c."
                        + System.lineSeparator() +
                        "§6En attendant vous pouvez visiter notre site :D"
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
                event.setCancelled(true);
                return;
            }
        }
        if (MainBungee.maintenance.getTime() > new Date().getTime()) {
            if (!MainBungee.profiles.containsKey(uuid) || !MainBungee.profiles.get(uuid).isMaintenance()) {
                event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cServeur en maintenance !"
                        + System.lineSeparator() +
                        "§6Délais avant la ré-ouverture§c: §b" + DateMilekat.reamingToStrig(MainBungee.maintenance) + "§c."
                        + System.lineSeparator() +
                        "§6En attendant vous pouvez visiter notre site :D"
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
                event.setCancelled(true);
                return;
            }
        }
        if (MainBungee.approbationList.contains(uuid)) {
            // Le joueur doit être approuvé
            event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                    + System.lineSeparator() +
                    "§cVous faite l'objet d'une surveillance accrue !"
                    + System.lineSeparator() +
                    "§6Merci de faire une demande d'approbation sur notre site,"
                    + System.lineSeparator() +
                    "pour accéder à l'évènement en pleine sécurité§c:"
                    + System.lineSeparator() +
                    "§b§nweb.cite-balkoura.fr"));
            event.setCancelled(true);
            return;
        }
        if (!MainBungee.profiles.containsKey(uuid) || MainBungee.profiles.get(uuid).getTeam()==0) {
            event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                    + System.lineSeparator() +
                    "§cVous n'êtes pas inscrit !"
                    + System.lineSeparator() +
                    "§6Vous devez vous inscrire directement sur le §9discord§c."
                    + System.lineSeparator() +
                    "§6Une fois inscrit, vous devrez intégrer une §béquipe§c."
                    + System.lineSeparator() +
                    "§9§ndiscord.gg/3q2f53E"
                    + System.lineSeparator() +
                    "§b§nweb.cite-balkoura.fr"));
            event.setCancelled(true);
            return;
        }
        if (MainBungee.profiles.get(uuid).isBan()) {
            // Le joueur est ban ou tempban
            if (MainBungee.profiles.get(uuid).getBanned().equals("def")) {
                event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cVous êtes définitivement bannis pour la raison suivante:"
                        + System.lineSeparator() +
                        "§e" + MainBungee.profiles.get(uuid).getReason()
                        + System.lineSeparator() +
                        "§6Vous pouvez faire appel de cette décision directement sur §9Discord§c."
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
            } else {
                Date ban = DateMilekat.getDate(MainBungee.profiles.get(uuid).getBanned());
                event.setCancelReason(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cVous êtes actuellement suspendu pour la raison suivante:"
                        + System.lineSeparator() +
                        "§e" + MainBungee.profiles.get(uuid).getReason()
                        + System.lineSeparator() +
                        "§6Délais de suspension restant§c: §b" + DateMilekat.reamingToStrig(ban) + "§c."
                        + System.lineSeparator() +
                        "§6Vous pouvez faire appel de cette décision directement sur §9Discord§c:"
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
            }
            event.setCancelled(true);
        }
    }

    /**
     *      Connection autorisée par le proxy, init du joueur
     * @param event PostLoginEvent
     */
    @EventHandler
    public void onProxyJoined(PostLoginEvent event){
        UUID pUuid = event.getPlayer().getUniqueId();
        // Redis
        JedisPub.sendRedis("join_notif#:#" + event.getPlayer().getName());
        // Insert du message de log + update pseudo + send chat
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement(
                    "INSERT INTO `" + MainBungee.SQLPREFIX + "chat`(`player_id`, `msg`, `msg_type`, `date_msg`) VALUES (" +
                        "(SELECT `player_id` FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" + pUuid + "'),'join',5,?)" +
                        " RETURNING `msg_id`;" +
                        "UPDATE `" + MainBungee.SQLPREFIX + "player` SET `online` = '1', `name` = ? WHERE `uuid` = ?;");
            q.setString(1, DateMilekat.setDateNow());
            q.setString(2, event.getPlayer().getName());
            q.setString(3, pUuid.toString());
            q.execute();
            q.getResultSet().last();
            new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur SQL onProxyJoined.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        ProxyServer.getInstance().getScheduler().runAsync(MainBungee.getInstance(),()->
                new ChatSend().RlChatFor(event.getPlayer().getName(),20));
    }

    /**
     *      Désactive 'online', et insert le message de déconnection, & reset des vars
     * @param event PlayerDisconnectEvent
     */
    @EventHandler
    public void onProxyLeave(PlayerDisconnectEvent event) {
        // Redis
        JedisPub.sendRedis("quit_notif#:#" + event.getPlayer().getName());
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "chat`(`player_id`, `msg`, `msg_type`, `date_msg`) " +
                    "VALUES ((SELECT `player_id` FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" +
                    event.getPlayer().getUniqueId() + "'),'quit',5,?) RETURNING `msg_id`;" +
                    "UPDATE `" + MainBungee.SQLPREFIX + "player` SET `online` = '0' WHERE `uuid` = ?;");
            q.setString(1, DateMilekat.setDateNow());
            q.setString(2, event.getPlayer().getUniqueId().toString());
            q.execute();
            q.getResultSet().last();
            new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur SQL onProxyLeave :");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        MainBungee.spamChat.remove(event.getPlayer().getUniqueId());
        MainBungee.lastMsg.remove(event.getPlayer().getUniqueId());
    }

    /**
     *      Enregistrement d'un joueur, ou update du name si l'uuid a changé !
     * @param p jouer à reg
     */
    private void setPlayerInfo(ProxiedPlayer p) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "player`(`uuid`, `name`, `date_join`) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `name` = ?;");
            q.setString(1,p.getUniqueId().toString());
            q.setString(2,p.getName());
            q.setString(3,DateMilekat.setDateNow());
            q.setString(4,p.getName());
            q.execute();
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de l'enregistrement de : " + p.getName() + ".");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }
}
