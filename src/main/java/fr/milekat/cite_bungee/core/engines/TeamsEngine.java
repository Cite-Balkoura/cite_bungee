package fr.milekat.cite_bungee.core.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.obj.Profil;
import fr.milekat.cite_bungee.core.obj.Team;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeamsEngine {
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(),
                this::updateTeams, 60L, 60L, TimeUnit.SECONDS);
    }

    public void updateTeams() {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("SELECT * FROM `" + MainBungee.SQLPREFIX + "team`;");
            q.execute();
            while (q.getResultSet().next()){
                PreparedStatement q2 = connection.prepareStatement("SELECT `uuid` FROM `" + MainBungee.SQLPREFIX +
                        "player` WHERE `team_id` = ?;");
                q2.setInt(1,q.getResultSet().getInt("team_id"));
                q2.execute();
                ArrayList<Profil> members = new ArrayList<>();
                while (q2.getResultSet().next()) {
                    members.add(MainBungee.profiles.get(UUID.fromString(q2.getResultSet().getString("uuid"))));
                }
                q2.close();
                MainBungee.teams.put(q.getResultSet().getInt("team_id"),
                        new Team(q.getResultSet().getInt("team_id"),
                                q.getResultSet().getString("team_name"),
                                q.getResultSet().getString("team_tag"),
                                q.getResultSet().getInt("money"),
                                members));
            }
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Impossible d'update la liste des équipes !");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }
}
