package fr.milekat.cite_bungee.core.commands;

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
import java.util.*;

public class Ban extends Command implements TabExecutor {
    public Ban() {
        super("ban", "modo.core.command.ban", "tempban");
    }

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
        String banDate = "def";
        if (!args[1].equalsIgnoreCase("def")) {
            Long time = DateMilekat.stringToPeriod(args[1]);
            time = time + new Date().getTime();
            // Check si le ban est plus petit que 10s (10000ms)
            if (time < (new Date().getTime() + 10000)) {
                sender.sendMessage(new TextComponent(
                        MainBungee.prefixCmd + "§cMerci d'indiquer un délais suppérieur à 10s."));
                return;
            }
            banDate = DateMilekat.setDate(new Date(time));
        }
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
        try {
            banPlayer(MainBungee.joueurslist.get(args[0]), banDate, motif);
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur SQL lors du ban de: " + args[0]);
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cErreur SQL lors du ban."));
            if (MainBungee.logDebug) throwables.printStackTrace();
            return;
        }
        MainBungee.info("Le joueur " + args[0] + " a été ban par "
                + sender.getName() + " jusqu'au " + banDate + ".");
        JedisPub.sendRedis("log_sanction#:#ban#:#" + MainBungee.profiles.get(targetid).getDiscordid() + "#:#" +
                modo_id + "#:#" + args[1] + "#:#" + banDate + "#:#" + motif + "#:#" +
                "/ban " + args[0] + " " + sb.toString());
        if (target!=null && target.isConnected()) target.disconnect(getBanMsg(target));
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§2Vous avez ban §b"
                + args[0] + "§2 jusqu'au §b" + banDate + "§2."));
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/ban <Player> <Durée/def> <Raison>:§r ban le joueur avec motif."));
        sender.sendMessage(new TextComponent("§6Format de la durée:§r §rx§bJ§rx§bh§rx§bm§rx§bs."));
    }

    /**
     *      Effectue le banissement d'un joueur
     * @param uuid du joueur ciblé
     * @param duration durée du ban (peut être def)
     */
    private void banPlayer(UUID uuid, String duration, String reason) throws SQLException {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                "player` SET `banned` = ?, `reason` = ? WHERE `uuid` = ?;");
        q.setString(1, duration);
        q.setString(2, reason);
        q.setString(3, uuid.toString());
        q.execute();
        q.close();
        MainBungee.profiles.get(uuid).setBanned(duration);
        MainBungee.profiles.get(uuid).setReason(reason);
    }

    /**
     *      Mise en format du message de ban lors du kick si le joueur est online
     * @param player bannis
     * @return msg de ban
     */
    private TextComponent getBanMsg(ProxiedPlayer player) {
        // Le joueur est ban ou tempban
        String reason = MainBungee.profiles.get(player.getUniqueId()).getReason();
        if (MainBungee.profiles.get(player.getUniqueId()).getBanned().equalsIgnoreCase("def")) {
            return new TextComponent(MainBungee.prefixCmd
                    + System.lineSeparator() +
                    "§cVous êtes définitivement bannis pour la raison suivante:"
                    + System.lineSeparator() +
                    "§e" + reason
                    + System.lineSeparator() +
                    "§6Vous pouvez faire appel de cette décision directement sur §9Discord§c."
                    + System.lineSeparator() +
                    "§b§nweb.cite-balkoura.fr");
        } else {
            Date ban = DateMilekat.getDate(MainBungee.profiles.get(player.getUniqueId()).getBanned());
            return new TextComponent(MainBungee.prefixCmd
                    + System.lineSeparator() +
                    "§cVous êtes actuellement suspendu pour la raison suivante:"
                    + System.lineSeparator() +
                    "§e" + reason
                    + System.lineSeparator() +
                    "§6Délais de suspension restant§c: §b" + DateMilekat.reamingToStrig(ban) + "§c."
                    + System.lineSeparator() +
                    "§6Vous pouvez faire appel de cette décision directement sur §9Discord§c:"
                    + System.lineSeparator() +
                    "§b§nweb.cite-balkoura.fr");
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return MainBungee.playerListFull(args[0]);
        } else if (args.length == 2) {
            if ("def".startsWith(args[1].toLowerCase())) {
                return new ArrayList<>(Collections.singletonList("def"));
            }
        }
        return new ArrayList<>();
    }
}
