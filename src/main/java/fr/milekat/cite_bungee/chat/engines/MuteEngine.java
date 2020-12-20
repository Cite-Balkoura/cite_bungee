package fr.milekat.cite_bungee.chat.engines;


import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MuteEngine {
    /**
     *      Routine de gestion du mute
     */
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                PreparedStatement q = connection.prepareStatement("SELECT `uuid`, `name`, `muted` FROM `" +
                        MainBungee.SQLPREFIX + "player` WHERE `muted` != 'pas mute';");
                q.execute();
                while (q.getResultSet().next()){
                    Date time = DateMilekat.getDate(q.getResultSet().getString("muted"));
                    UUID uuid = UUID.fromString(q.getResultSet().getString("uuid"));
                    if (time!=null && time.getTime() < System.currentTimeMillis()){
                        PreparedStatement q2 = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                                "player` SET `muted` = 'pas mute' WHERE `uuid` = ?;");
                        q2.setString(1, uuid.toString());
                        q2.execute();
                        q2.close();
                        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                            if (player.getUniqueId().toString().equalsIgnoreCase(
                                    q.getResultSet().getString("uuid"))) {
                                player.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2Vous n'êtes plus mute !"
                                        + System.lineSeparator() + "§6Soyez plus vigilant à l'avenir !"));
                            }
                        }
                        MainBungee.info(q.getResultSet().getString("name") + " n'est plus mute !");
                        MainBungee.profiles.get(uuid).setMuted("pas mute");
                        JedisPub.sendRedis("log_sanction#:#unmute#:#" + MainBungee.profiles.get(uuid).getDiscordid() +
                                "#:#console#:#Fin du délai#:#/unmute " + MainBungee.profiles.get(uuid).getName() +
                                " Fin du délai");
                    }
                }
                q.close();
            } catch (SQLException throwables) {
                MainBungee.warning("Impossible d'effectuer le check des mutes !");
                if (MainBungee.logDebug) throwables.printStackTrace();
            }
        }, 1000L, 500L, TimeUnit.MILLISECONDS);
    }
}
