package fr.milekat.cite_bungee.core.events;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Date;

public class PlayerPing implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void proxyPing(ProxyPingEvent event) {
        if (MainBungee.opendate.getTime() > new Date().getTime()) {
            final ServerPing ping = event.getResponse();
            ping.setDescriptionComponent(new TextComponent(MainBungee.prefixCmd +
                    "§6Serveur en préparation§c.\n" +
                    "§6Retrouvez nous sur§c: §b§nweb.cite-balkoura.fr"));
            ping.getVersion().setProtocol(1);
            ping.getVersion().setName("En préparation");
            ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{
                    new ServerPing.PlayerInfo("§6Ouverture dans§c:", ""),
                    new ServerPing.PlayerInfo("§b" + DateMilekat.reamingToStrig(MainBungee.opendate), "")
            });
            event.setResponse(ping);
        } else if (MainBungee.maintenance.getTime() > new Date().getTime()) {
            final ServerPing ping = event.getResponse();
            ping.setDescriptionComponent(new TextComponent(MainBungee.prefixCmd +
                    "§6Serveur en maintenance§c.\n" +
                    "§6Retrouvez nous sur§c: §b§nweb.cite-balkoura.fr"));
            ping.getVersion().setProtocol(1);
            ping.getVersion().setName("En maintenance");
            ping.getPlayers().setSample(new ServerPing.PlayerInfo[]{
                    new ServerPing.PlayerInfo("§6Ré-ouverture dans§c:", ""),
                    new ServerPing.PlayerInfo("§b" + DateMilekat.reamingToStrig(MainBungee.maintenance), "")
            });
            event.setResponse(ping);
        }
    }
}
