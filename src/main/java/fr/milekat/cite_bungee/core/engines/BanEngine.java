package fr.milekat.cite_bungee.core.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.Jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanEngine {
    /**
     *      Routine de gestion du tempban / ban def
     */
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                PreparedStatement q = connection.prepareStatement("SELECT `uuid`, `name`, `banned` FROM `" + MainBungee.SQLPREFIX +
                        "player` WHERE `banned` != 'pas ban';");
                q.execute();
                while (q.getResultSet().next()){
                    UUID uuid = UUID.fromString(q.getResultSet().getString("uuid"));
                    if (q.getResultSet().getString("banned").equalsIgnoreCase("def")) {
                        MainBungee.profiles.get(uuid).setBanned("def");
                    } else {
                        Date time = DateMilekat.getDate(q.getResultSet().getString("banned"));
                        if (time!=null && time.getTime() < System.currentTimeMillis()) {
                            UUID targetid = UUID.fromString(q.getResultSet().getString("uuid"));
                            PreparedStatement q2 = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                                    "player` SET `banned` = 'pas ban', `reason` = NULL WHERE `player`.`uuid` = ?;");
                            q2.setString(1, targetid.toString());
                            q2.execute();
                            q2.close();
                            MainBungee.info(q.getResultSet().getString("name") + " n'est plus ban !");
                            MainBungee.profiles.get(uuid).setBanned("pas ban");
                            JedisPub.sendRedis("log_sanction#:#unban#:#" + MainBungee.profiles.get(targetid).getDiscordid() +
                                    "#:#console#:#Fin du délai");
                        }
                    }
                }
                q.close();
            } catch (SQLException throwables) {
                MainBungee.warning("Impossible d'effectuer le check des bans !");
                if (MainBungee.logDebug) throwables.printStackTrace();
            }
        }, 3L, 1L, TimeUnit.SECONDS);
    }
}
