package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.engines.Classements;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

public class SendClassements extends Command {
    public SendClassements() {
        super("sendclassements", "modo.core.sendclassements");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        ProxyServer.getInstance().getScheduler().runAsync(MainBungee.getInstance(), Classements::sendClassements);
    }
}
