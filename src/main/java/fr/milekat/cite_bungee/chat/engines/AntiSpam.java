package fr.milekat.cite_bungee.chat.engines;


import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AntiSpam {
    public ScheduledTask runTask() {
        return ProxyServer.getInstance().getScheduler().schedule(MainBungee.getInstance(), () -> {
            for (Map.Entry<UUID, Integer> loop : MainBungee.spamChat.entrySet()) {
                int val = Integer.parseInt(loop.getValue().toString()) - 3;
                if (val < 0) {
                    val = 0;
                }
                loop.setValue(val);
            }
        }, 0L, 500L, TimeUnit.MILLISECONDS);
    }
}