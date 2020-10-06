package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import fr.milekat.cite_bungee.utils_tools.Web;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Mute extends Command implements TabExecutor {
    public Mute() {
        super("mute","helper.chat.command.mute");
    }

    /**
     *      Commandes de mute / unmute
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 3) {
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
        // Définition du délais
        Long time = DateMilekat.stringToPeriod(args[1]);
        time = time + new Date().getTime();
        // Check si le joueur est un helper pour limiter à 1h le mute (3600000ms)
        if (!sender.hasPermission("modo.chat.command.mute")){
            if (time > (new Date().getTime()+3600000)){
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cDésolé mais tu ne peux pas mute un joueur de plus d'1h."));
                return;
            }
        }
        // Check si le mute est plus petit que 10s (10000ms)
        if (time < (new Date().getTime()+10000)){
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cMerci d'indiquer un délais suppérieur à 10s."));
            return;
        }
        String muteDate = DateMilekat.setDate(new Date(time));
        // Définition du motif/raison de sanction
        StringBuilder sb = new StringBuilder();
        for (String loop : args){
            if (!loop.equals(args[0]) && !loop.equals(args[1])){
                sb.append(loop);
                sb.append(" ");
            }
        }
        String motif = Web.remLastChar(sb.toString());
        if (motif.equalsIgnoreCase("")) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cMerci d'indiquer un dmotif."));
            return;
        }
        // Action
        try {
            mutePlayer(MainBungee.joueurslist.get(args[0]), muteDate);
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur Java lors du mute de: " + args[0]);
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cErreur Java lors du mute."));
            if (MainBungee.logDebug) throwables.printStackTrace();
            return;
        }
        MainBungee.info("Le joueur " + args[0] + " a été mute par " + sender.getName() + " jusqu'au " + muteDate + ".");
        JedisPub.sendRedis("log_sanction#:#mute#:#" + MainBungee.profiles.get(targetid).getDiscordid() + "#:#" +
                modo_id + "#:#" + args[1] + "#:#" + muteDate + "#:#" + motif + "#:#" +
                "/mute " + args[0] + sb.toString());
        if (target!=null && target.isConnected()) {
            target.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cVous êtes mute jusqu'au §b" + muteDate + "§2."));
        }
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2Vous avez mute §b" + args[0] + "§2 jusqu'au §b" + muteDate + "§2."));
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/mute <Player> <Durée> <Raison>:§r mute le joueur avec motif."));
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Format de la durée:§r §rx§bJ§rx§bh§rx§bm§rx§bs."));
    }

    /**
     *      Effectue le mute d'un joueur
     * @param uuid du joueur ciblé
     * @param duration durée du ban (peut être def)
     */
    private void mutePlayer(UUID uuid, String duration) throws SQLException {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                "player` SET `muted` = ? WHERE `uuid` = ?;");
        q.setString(1, duration);
        q.setString(2, uuid.toString());
        q.execute();
        q.close();
        MainBungee.profiles.get(uuid).setMuted(duration);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return MainBungee.playerListFull(args[0]);
        }
        return new ArrayList<>();
    }
}
