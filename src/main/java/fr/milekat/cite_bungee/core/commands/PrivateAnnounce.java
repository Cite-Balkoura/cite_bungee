package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PrivateAnnounce extends Command {
    public PrivateAnnounce() {
        super("pvbc", "modo.core.command.privateannounce");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            ProxiedPlayer player = MainBungee.getInstance().getProxy().getPlayer(args[0]);
            StringBuilder sb = new StringBuilder();
            for (String loop : args){
                if (!loop.equals(args[0]) && !loop.equals(args[1])){
                    sb.append(loop);
                    sb.append(" ");
                }
            }
            player.sendMessage(new TextComponent("[" + args[1] + " " + DateMilekat.setDateNow() + "] " + sb.toString()));
        } catch (NullPointerException ignore) {}
    }
}
