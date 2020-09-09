package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class ScoreBoardCmd extends Command {
    public ScoreBoardCmd() {
        super("scoreboard", null);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (MainBungee.noScoreBoard.contains(((ProxiedPlayer) sender).getUniqueId())) {
            MainBungee.noScoreBoard.remove(((ProxiedPlayer) sender).getUniqueId());
        } else {
            MainBungee.noScoreBoard.add(((ProxiedPlayer) sender).getUniqueId());
        }
    }
}
