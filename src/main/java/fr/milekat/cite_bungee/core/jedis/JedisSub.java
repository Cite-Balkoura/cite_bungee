package fr.milekat.cite_bungee.core.jedis;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.events.CustomJedisSub;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.JedisPubSub;

public class JedisSub extends JedisPubSub {
    private final Configuration config = MainBungee.getInstance().getConfig();

    @Override
    public void onMessage(String channel, String message) {
        if (!channel.equalsIgnoreCase(config.getString("redis.thischannel"))) {
            if (MainBungee.jedisDebug) {
                ProxyServer.getInstance().getLogger().info("SUB:{" + channel + "},MSG:{" + message + "}");
            }
            ProxyServer.getInstance().getPluginManager().callEvent(new CustomJedisSub(channel,message));
        } else {
            if (MainBungee.jedisDebug) {
                ProxyServer.getInstance().getLogger().info("PUB:{" + message + "}");
            }
        }
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
        if (MainBungee.jedisDebug) {
            ProxyServer.getInstance().getLogger().info(MainBungee.prefixConsole + "Redis connecté à " + channel);
        }
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
        if (MainBungee.jedisDebug) {
            ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + "Redis déconnecté de " + channel);
        }
    }
}