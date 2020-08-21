package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.Jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.Web;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;
import java.util.UUID;

public class Kick extends Command implements TabExecutor {
    public Kick() {
        super("kick", "modo.core.command.kick");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sendHelp(sender);
            return;
        }
        // Définition modo
        String modo_id;
        if (!(sender instanceof ProxiedPlayer)) {
            modo_id = "console";
        } else {
            UUID senderid = ((ProxiedPlayer) sender).getUniqueId();
            modo_id = MainBungee.profiles.get(senderid).getDiscordid() + "";
        }
        // Définition de la cible
        if(!MainBungee.joueurslist.containsKey(args[0])) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cJoueur introuvable."));
            return;
        }
        ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
        UUID targetid = MainBungee.joueurslist.get(args[0]);
        // Définition du motif/raison de sanction
        StringBuilder sb = new StringBuilder();
        for (String loop : args){
            if (!loop.equals(args[0]) && !loop.equals(args[1])){
                sb.append(loop);
                sb.append(" ");
            }
        }
        String motif = Web.remLastChar(sb.toString());
        // Check si le motif est null
        if (motif.equalsIgnoreCase("")) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cMerci d'indiquer un motif."));
            return;
        }
        // Action
        MainBungee.info("Le joueur " + args[0] + " a été kick par " + sender.getName() + " motif " + motif + ".");
        JedisPub.sendRedis("log_sanction#:#kick#:#" + MainBungee.profiles.get(targetid).getDiscordid() + "#:#" +
                modo_id + "#:#" + motif);
        if (target!=null && target.isConnected()) target.disconnect(getKickMsg(motif));
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2Vous avez kick §b" + args[0] + "§2."));
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/kick <Player> <Raison>:§r kick le joueur avec motif."));
    }

    /**
     *      Mise en format du message du kick
     * @param reason du kick
     * @return msg de ban
     */
    private TextComponent getKickMsg(String reason) {
        return new TextComponent(MainBungee.prefixCmd
                + System.lineSeparator() +
                "§cVous avez été kick pour la raison suivante:"
                + System.lineSeparator() +
                "§e" + reason);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return MainBungee.playerListOnline(args[0]);
        }
        return new ArrayList<>();
    }
}
