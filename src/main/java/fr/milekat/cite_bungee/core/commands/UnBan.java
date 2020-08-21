package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.Jedis.JedisPub;
import fr.milekat.cite_bungee.core.obj.Profil;
import fr.milekat.cite_bungee.utils_tools.Web;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class UnBan extends Command implements TabExecutor {
    public UnBan() {
        super("unban","modo.core.command.pardon");
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
        UUID targetid = MainBungee.joueurslist.get(args[0]);
        if (!MainBungee.profiles.get(targetid).isBan()) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cJoueur n'est pas ban."));
            return;
        }
        // Définition du motif/raison de sanction
        StringBuilder sb = new StringBuilder();
        for (String loop : args){
            if (!loop.equals(args[0])){
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
        try {
            unbanPlayer(targetid);
            MainBungee.info("Le joueur " + args[0] + " a été unban par " + sender.getName() + ".");
            JedisPub.sendRedis("log_sanction#:#unban#:#" + MainBungee.profiles.get(targetid).getDiscordid() + "#:#" +
                    modo_id + "#:#" + motif);
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2Vous avez unban §b" + args[0] + "§2."));
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur SQL lors de l'unban de: " + args[0]);
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/unban <Player> <raison>:§r unban le joueur."));
    }

    private void unbanPlayer(UUID uuid) throws SQLException {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                "player` SET `banned` = 'pas ban' WHERE `uuid` = ?;");
        q.setString(1, uuid.toString());
        q.execute();
        q.close();
    }

    /**
     *      Envois la liste des joueurs ban
     */
    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ArrayList<String> banned = new ArrayList<>();
            for (Profil player : MainBungee.profiles.values()) {
                if (player.isBan()) {
                    banned.add(player.getName());
                }
            }
            return banned;
        }
        return new ArrayList<>();
    }
}
