package fr.milekat.cite_bungee.core.engines;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.MariaManage;
import fr.milekat.cite_bungee.core.jedis.JedisPub;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Update {
    public void Classement(){
        Timer timer = new Timer();
        timer.schedule(new ClassementUpdate(),0,10000);
    }

    static class ClassementUpdate extends TimerTask {
        private final List<Integer> update = new ArrayList<>();
        private int lasthour;
        private final MariaManage sql = MainBungee.getInstance().getSql();

        public ClassementUpdate(){
            update.add(0);
            update.add(8);
            update.add(16);
        }

        @Override
        public void run() {
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour !=lasthour && update.contains(hour)) {
                Connection connection = sql.getConnection();
                try {
                    PreparedStatement q = connection.prepareStatement("TRUNCATE `team_classement`;" +
                            "INSERT INTO `team_classement` SELECT * FROM `team`;");
                    q.execute();
                    q.close();
                } catch (SQLException ignored) {}
                new JedisPub().sendRedis("update:classement");
            }
            lasthour = hour;
        }
    }
}
