package fr.milekat.cite_bungee.core.events;

import net.md_5.bungee.api.plugin.Event;

import java.util.Arrays;

public class CustomJedisSub extends Event {
    private final String channel;
    private final String redisMsg;

    public CustomJedisSub(String channel, String redisMsg) {
        this.channel = channel;
        this.redisMsg = redisMsg;

    }

    public String getChannel() {
        return channel;
    }

    public String getRedisMsg() {
        return redisMsg;
    }

    public String getCommand() {
        String[] args = redisMsg.split("#:#");
        return args[0];
    }

    public String[] getMessage() {
        String[] args = redisMsg.split("#:#");
        return Arrays.copyOfRange(args,1,args.length);
    }
}
