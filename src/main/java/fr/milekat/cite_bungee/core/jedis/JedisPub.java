package fr.milekat.cite_bungee.core.jedis;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import java.util.Objects;

public class JedisPub {
    /**
     *      Envoi d'un message sur le Redis
     * @param msg message à envoyer
     */
    public static void sendRedis(String msg){
        new Thread(() -> {
            Configuration config = MainBungee.getInstance().getConfig();
            try (Jedis jedis = new Jedis(config.getString("redis.host"),
                    Integer.parseInt(Objects.requireNonNull(config.getString("redis.port"))),0)) {
                jedis.auth(config.getString("redis.auth"));
                jedis.publish(config.getString("redis.thischannel"),msg);
                jedis.quit();
            } catch (Exception ex) {
                ProxyServer.getInstance().getLogger().warning("Exception : " + ex.getMessage());
            }
        }).start();
    }
}
