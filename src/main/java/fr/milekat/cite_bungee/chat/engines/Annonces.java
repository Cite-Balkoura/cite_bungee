package fr.milekat.cite_bungee.chat.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class Annonces {
    /**
     *      Toutes les 5 minutes envois une annonce si la last message est un msg du chat
     */
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            try {
                PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                        "chat`(`player_id`, `msg`, `msg_type`, `date_msg`) SELECT 10, " +
                        "(SELECT `text` FROM `" + MainBungee.SQLPREFIX + "annonce` WHERE `active` = TRUE ORDER BY RAND() LIMIT 1)" +
                        " , 4, ? WHERE (SELECT `msg_type` FROM `" + MainBungee.SQLPREFIX + "chat` ORDER BY `msg_id` DESC LIMIT 1)" +
                        " = 1 RETURNING `msg_id`;");
                q.setString(1,DateMilekat.setDateNow());
                q.execute();
                if (q.getResultSet().last()) {
                    JedisPub.sendRedis("new_msg#:#" + q.getResultSet().getInt("msg_id"));
                    new ChatSend().sendChatFor("all",q.getResultSet().getInt("msg_id"),false);
                } else if (MainBungee.logDebug) {
                    MainBungee.info("Pas de nouvelle annonce.");
                }
                q.close();
            } catch (SQLException throwables) {
                MainBungee.warning("Erreur SQL sur les annonces.");
                if (MainBungee.logDebug) throwables.printStackTrace();
            }
        },0L,5L,TimeUnit.MINUTES);
    }
}
