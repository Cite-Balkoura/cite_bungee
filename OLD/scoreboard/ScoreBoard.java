package fr.milekat.cite_bungee.core.scoreboard;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.protocol.packet.ScoreboardDisplay;
import net.md_5.bungee.protocol.packet.ScoreboardObjective;
import net.md_5.bungee.protocol.packet.ScoreboardScore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static net.md_5.bungee.protocol.packet.ScoreboardObjective.HealthDisplay.INTEGER;

public class ScoreBoard {
    private int board = 0;

    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            board++;
            if (board>3) board = 1;
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    ScoreboardObjective objective = new ScoreboardObjective(
                            "test", "Balkoura", INTEGER, (byte) 0);
                    ScoreboardScore score = new ScoreboardScore("mccitebalkourafr", (byte) 0, "test", 0);
                    ScoreboardDisplay display = new ScoreboardDisplay((byte) 1, "test");
                    player.unsafe().sendPacket(objective);
                    player.unsafe().sendPacket(score);
                    player.unsafe().sendPacket(display);
                    PreparedStatement q = connection.prepareStatement("SELECT `team_id` FROM `balkoura_team` WHERE `team_id` = 1;");
                    q.execute();
                    q.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, 0L, 20L, TimeUnit.SECONDS);
    }

/*
    private ScoreboardScore baseBoard(String name) {
        return new ScoreboardScore("§bmc.cite-balkoura.fr", (byte) 0, name, 0);
    }*/
}