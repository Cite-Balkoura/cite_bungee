package fr.milekat.cite_bungee.core.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.obj.Profil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayersEngine {
    /**
     *      Routine de gestion du tempban / ban def
     */
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(),
                this::updateProfiles, 20L, 20L, TimeUnit.SECONDS);
    }

    public void updateProfiles() {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement(
                    "SELECT `player_id`, `uuid`, `name`, `team_id`, `chat_mode`, `muted`, `banned`, `reason`, " +
                            "`modson`, `points_quest`, `points_event`, `maintenance`, `discord_id` " +
                            "FROM `" + MainBungee.SQLPREFIX + "player` WHERE `name` != 'Annonce';");
            q.execute();
            while (q.getResultSet().next()){
                MainBungee.profiles.put(UUID.fromString(q.getResultSet().getString("uuid")),
                        new Profil(q.getResultSet().getInt("player_id"),
                                UUID.fromString(q.getResultSet().getString("uuid")),
                                q.getResultSet().getString("name"),
                                q.getResultSet().getInt("team_id"),
                                q.getResultSet().getInt("chat_mode"),
                                q.getResultSet().getString("muted"),
                                q.getResultSet().getString("banned"),
                                q.getResultSet().getString("reason"),
                                q.getResultSet().getBoolean("modson"),
                                q.getResultSet().getBoolean("maintenance"),
                                q.getResultSet().getLong("discord_id"),
                                q.getResultSet().getInt("points_quest"),
                                q.getResultSet().getInt("points_event")));
                MainBungee.joueurslist.put(q.getResultSet().getString("name"),
                        UUID.fromString(q.getResultSet().getString("uuid")));
            }
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Impossible d'update la liste des joueurs !");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }

    }
}
