package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatFormat;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.SQLException;

public class TeamChat extends Command {
    public TeamChat() {
        super("teamchat","","tc","tchat");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (MainBungee.profiles.get(player.getUniqueId()).isMute()){
            warnMute(player);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String loop : args) {
            sb.append(loop);
            sb.append(" ");
        }
        int id = new ChatFormat().insertSQLNewChatTeam(player, sb.toString());
        if (id == 0) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu n'as pas de team."));
            return;
        }
        // Envoie du message à toute la team
        try {
            new ChatSend().sendChatFor("all",id,false);
            // Log du message dans la console, comme un message normal
            MainBungee.log("[Team] <" + player.getDisplayName() + "> " + sb.toString());
            // Redis
            JedisPub.sendRedis("new_msg#:#" + id);
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur SQL commande teamchat.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie le temps de mute restant !
     * @param player joueur
     */
    private void warnMute(ProxiedPlayer player) {
        String str = DateMilekat.reamingToStrig(DateMilekat.getDate(MainBungee.profiles.get(player.getUniqueId()).getMuted()));
        TextComponent Mute = new TextComponent(MainBungee.prefixCmd + "§6Vous serez unmute dans §b" + str + "§c.");
        Mute.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text("§cLes modos peuvent" +
                System.lineSeparator() + "encore voir vos" + System.lineSeparator() + "messages")));
        player.sendMessage(Mute);
    }
}
