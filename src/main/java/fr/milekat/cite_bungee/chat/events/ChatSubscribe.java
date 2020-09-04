package fr.milekat.cite_bungee.chat.events;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.events.CustomJedisSub;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;

public class ChatSubscribe implements Listener {
    private final ChatSend chatSend = new ChatSend();

    @EventHandler
    public void onSubChat(CustomJedisSub event) {
        String[] msg = event.getMessage();
        switch (event.getCommand()) {
            case "new_msg":
            case "new_mp": {
                try {
                    chatSend.sendChatFor("all", Integer.parseInt(msg[0]), false);
                } catch (SQLException throwables) {
                    MainBungee.warning("Erreur SQL Sub:new_msg ou new_mp.");
                    if (MainBungee.logDebug) throwables.printStackTrace();
                }
                break;
            }
            case "new_token": {
                MainBungee.linkToken.put(msg[0], msg[1]);
                break;
            }
            case "close_token": {
                MainBungee.linkToken.remove(msg[0]);
                break;
            }
            case "join_notif": {
                MainBungee.info(MainBungee.prefixConsole + msg[0] + " à rejoint le serveur");
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (!onlineP.getName().equalsIgnoreCase(msg[0])) {
                        onlineP.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2" + msg[0] + "§6 a rejoint la cité."));
                    }
                }
                break;
            }
        }
    }
}
