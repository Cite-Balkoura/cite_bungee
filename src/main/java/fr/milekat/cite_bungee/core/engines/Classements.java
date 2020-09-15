package fr.milekat.cite_bungee.core.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.commands.Broadcast;
import fr.milekat.cite_bungee.utils_tools.Web;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Classements {
    private int lasthour = 0;
    private static final ArrayList<String> classementsTypes = new ArrayList<>(Arrays.asList("Solo","Duo/Trio","Équipes"));

    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            if (calendar.get(Calendar.HOUR_OF_DAY)!=lasthour && calendar.get(Calendar.HOUR_OF_DAY)==18) {
                sendClassements();
            }
            lasthour = calendar.get(Calendar.HOUR_OF_DAY);
        },0L,30L,TimeUnit.SECONDS);
    }

    public static void sendClassements() {
        ArrayList<LinkedHashMap<String, Integer>> classments = getClassements();
        if (classments == null) return;
        Broadcast.createAnnonce("Annonce des classements !");
        try {
        TimeUnit.SECONDS.sleep(5);
        for (int loop = 1; loop < 5; loop++) {
                int type = 0;
                StringBuilder annonce = new StringBuilder();
                for (LinkedHashMap<String, Integer> classement : classments) {
                    int position = 1;
                    if (type!=0) annonce.append("%nl%");
                    annonce.append("§c").append(classementsTypes.get(type)).append(" §6- §bTOP 5§r").append("%nl%");
                    for (Map.Entry<String, Integer> values : classement.entrySet()) {
                        annonce.append(position).append(". ");
                        if ((loop==1 && position<=3) || (loop==2 && position<=2) ||(loop==3 && position==1)) {
                            annonce.append("§b????????")
                                    .append("§r§6 avec §r")
                                    .append(values.getValue().toString().replaceAll("[0-9]", "?"))
                                    .append("§2 Émeraudes§r%nl%");
                        } else {
                            annonce.append("§b")
                                    .append(values.getKey())
                                    .append("§r§6 avec §r")
                                    .append(values.getValue())
                                    .append("§2 Émeraudes§r%nl%");
                        }
                        position++;
                    }
                    type++;
                }
                Broadcast.createAnnonce(Web.remLastChar(Web.remLastChar(Web.remLastChar(Web.remLastChar(annonce.toString())))));
                TimeUnit.SECONDS.sleep(10);
            }
        } catch (InterruptedException exception) {
            if (MainBungee.logDebug) exception.printStackTrace();
        }
    }

    private static ArrayList<LinkedHashMap<String, Integer>> getClassements() {
        try {
            Connection connection = MainBungee.getInstance().getSql().getConnection();
            PreparedStatement q = connection.prepareStatement("SELECT `team_name` as team_name, `money` as team_money " +
                    "FROM `balkoura_team` a LEFT JOIN `balkoura_player` b ON b.`team_id` = a.`team_id` " +
                    "GROUP BY `team_name` HAVING COUNT(b.`name`) > 3 ORDER BY `money` DESC LIMIT 5;");
            q.execute();
            LinkedHashMap<String, Integer> team = new LinkedHashMap<>();
            while (q.getResultSet().next()) {
                team.put(q.getResultSet().getString("team_name"),
                        q.getResultSet().getInt("team_money"));
            }
            q.close();
            q = connection.prepareStatement("SELECT `team_name` as team_name, `money` as team_money " +
                    "FROM `balkoura_team` a LEFT JOIN `balkoura_player` b ON b.`team_id` = a.`team_id` " +
                    "GROUP BY `team_name` HAVING COUNT(b.`name`) = 2 OR COUNT(b.`name`) = 3 " +
                    "ORDER BY `money` DESC LIMIT 5;");
            q.execute();
            LinkedHashMap<String, Integer> duos = new LinkedHashMap<>();
            while (q.getResultSet().next()) {
                duos.put(q.getResultSet().getString("team_name"),
                        q.getResultSet().getInt("team_money"));
            }
            q.close();
            q = connection.prepareStatement("SELECT `team_name` as team_name, `money` as team_money " +
                    "FROM `balkoura_team` a LEFT JOIN `balkoura_player` b ON b.`team_id` = a.`team_id` " +
                    "GROUP BY `team_name` HAVING COUNT(b.`name`) = 1 ORDER BY `money` DESC LIMIT 5;");
            q.execute();
            LinkedHashMap<String, Integer> solo = new LinkedHashMap<>();
            while (q.getResultSet().next()) {
                solo.put(q.getResultSet().getString("team_name"),
                        q.getResultSet().getInt("team_money"));
            }
            q.close();
            return new ArrayList<>(Arrays.asList(solo,duos,team));
        } catch (SQLException exception) {
            if (MainBungee.logDebug) exception.printStackTrace();
        }
        return null;
    }
}
