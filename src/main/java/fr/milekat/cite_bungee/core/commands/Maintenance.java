package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Date;

public class Maintenance extends Command {
    public Maintenance() {
        super("maint", "modo.core.maintenance", "mm", "maintenance");
    }

    /**
     *      Commande /maint <durée> active la maintenance pour x seconde et kick tous les non modo/admin
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelp(sender);
            return;
        }
        // Définition du délais
        Long time = DateMilekat.stringToPeriod(args[0]);
        time = time + new Date().getTime();
        // Check si la maintenance est plus petite que 10s (10000ms)
        if (time < (new Date().getTime() + 10000)) {
            sender.sendMessage(new TextComponent(
                    MainBungee.prefixCmd + "§cMerci d'indiquer un délais suppérieur à 10s."));
            return;
        }
        MainBungee.maintenance = new Date(time);
        MainBungee.getInstance().getConfig().set("configs.maintenance.until",DateMilekat.setDate(MainBungee.maintenance));
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            if (!MainBungee.profiles.containsKey(player.getUniqueId())
                    || !MainBungee.profiles.get(player.getUniqueId()).isMaintenance()) {
                player.disconnect(new TextComponent(MainBungee.prefixCmd
                        + System.lineSeparator() +
                        "§cServeur en maintenance !"
                        + System.lineSeparator() +
                        "§6Délais avant la ré-ouverture§c: §b" + DateMilekat.reamingToStrig(MainBungee.maintenance) + "§c."
                        + System.lineSeparator() +
                        "§6En attendant vous pouvez visiter notre site :D"
                        + System.lineSeparator() +
                        "§b§nweb.cite-balkoura.fr"));
            }
        }
        sender.sendMessage(new TextComponent("Maintenance activée pour: " + DateMilekat.reamingToStrig(MainBungee.maintenance)));
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/maintenance <Durée>:§r Active la maintenance et kick les joueurs."));
    }
}
