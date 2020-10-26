package fr.milekat.cite_bungee.chat.utils;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChatFormat {
    /**
     *      Mise en format du HoverMessage
     * @param name Pseudo du joueur
     * @param rank Groupe du joueur
     * @param team_name Nom d'équipe du joueur
     * @param money Argent de l'équipe du joueur
     * @param player_pts_event Nombre de pts_event du joueur
     * @param date Date du message
     * @return String pour Hover
     */
    public String infoPlayerBuilder(String name, String rank, String team_name, String money, int player_pts_event, int player_pts_quete, String date){
        String str = MainBungee.prefixCmd + System.lineSeparator() +
                "&6Pseudo&c: &r" + name + System.lineSeparator() +
                "&6Grade&c: &r" + rank + System.lineSeparator() +
                "&6Équipe&c: &2" + team_name + System.lineSeparator() +
                "&6Émeraudes&c: &2" + money + System.lineSeparator() +
                "&6Points Event&c: &2" + player_pts_event + System.lineSeparator() +
                "&6Points Quête&c: &2" + player_pts_quete;
        if (date != null){
            str = str + System.lineSeparator() + ChatColor.GRAY + date.substring(0, 5) + " " + date.substring(11);
        }
        return str;
    }

    /**
     *      Mise en forme du pseudo d'un joueur
     * @param name joueur
     * @param hoverMsg info
     * @param prefix prefix (ou "")
     * @return [prefix] Pseudo avec hover
     */
    public TextComponent playerChatDisplay(String name, String hoverMsg, String prefix) {
        TextComponent Name = new TextComponent(prefix + name);
        Name.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(ChatColor.translateAlternateColorCodes('&', hoverMsg))));
        return Name;
    }

    /**
     *      Mise en format d'un message de chat pour ModsON
     * @param prefix Prefix du joueur
     * @param sender (OffilinePlayer) Joueur qui envoie le msg
     * @param msg Contenu du message
     * @param hoverMsg Hover box avec les infos joueurs
     * @param id id du message (Pour la suppression
     * @return Retour du tellraw
     *
     */
    public TextComponent chatModsBuilder(String prefix, String sender, String msg , String hoverMsg, String remove, int id, String mute){
        TextComponent Chat = new TextComponent("");
        if (remove.equals("null")){
            //Prefix, pseudo & hover du joueur
            Chat.addExtra(playerChatDisplay(sender, hoverMsg, prefix));
            //Message du joueur
            Chat.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&',"&r &b»&r " + msg)));
            //Bouton pour supprimer le msg
            TextComponent DelButton = new TextComponent(" [X]");
            DelButton.setColor(ChatColor.RED);
            DelButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Supprimer ce message ?")));
            DelButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/removemsg " + id));
            Chat.addExtra(DelButton);
            //Bouton pour mute le joueur
            if (mute.equals("pas mute")) {
                TextComponent MuteButton = new TextComponent(" [MUTE]");
                MuteButton.setColor(ChatColor.RED);
                MuteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Mute 5 minutes")));
                MuteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mute " + sender + " " + "5m"));
                Chat.addExtra(MuteButton);
            } else {
                TextComponent unMuteButton = new TextComponent(" [UNMUTE]");
                unMuteButton.setColor(ChatColor.RED);
                unMuteButton.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.RED + "Unmute le joueur")));
                unMuteButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/unmute " + sender));
                Chat.addExtra(unMuteButton);
            }
        } else {
            TextComponent displayName = new TextComponent(ChatColor.RED + "<Message de " + sender + " supprimé par " + remove + ">");
            displayName.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(ChatColor.translateAlternateColorCodes('&', msg))));
            Chat.addExtra(displayName);
        }
        return Chat;
    }

    /**
     *      Mise en forme d'un message pour le chat d'un joueur
     * @param prefix Prefix du joueur
     * @param sender (OffilinePlayer) Joueur qui envoie le msg
     * @param msg Contenu du message
     * @param hoverMsg Hover box avec les infos joueurs
     * @return Retour du tellraw
     */
    public TextComponent chatPlayerBuilder(String prefix, String sender, String msg , String hoverMsg, String remove){
        TextComponent Chat = new TextComponent("");
        if (remove.equals("null")) {
            //Prefix, pseudo & hover du joueur
            Chat.addExtra(playerChatDisplay(sender, hoverMsg, prefix));
            //Message du joueur
            Chat.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&r &b»&r " + msg)));
        } else {
            Chat.addExtra(ChatColor.RED + "<Message de " + sender + " supprimé par " + remove + ">");
        }
        return Chat;
    }

    public TextComponent privateToMe(String hoverSender, String sender, String msg){
        TextComponent Chat = new TextComponent("§6[§c");
        //Prefix, pseudo & hover du joueur
        Chat.addExtra(playerChatDisplay(sender, hoverSender, ""));
        //Message du joueur
        Chat.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', " &6> &cMoi&6]&r " + msg)));
        return Chat;
    }

    public TextComponent privateOfMe(String hoverDest, String dest, String msg){
        TextComponent Chat = new TextComponent("§6[§cMoi §6> §c");
        //Prefix, pseudo & hover du joueur
        Chat.addExtra(playerChatDisplay(dest, hoverDest, ""));
        //Message du joueur
        Chat.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&6]&r " + msg)));
        return Chat;
    }

    public TextComponent privateMods(String hoverSender, String sender, String hoverDest, String dest, String msg){
        TextComponent Chat = new TextComponent(ChatColor.GOLD + "§6[§c");
        //Prefix, pseudo & hover du joueur
        Chat.addExtra(playerChatDisplay(sender, hoverSender, ""));
        Chat.addExtra("§r §6> §c");
        //Prefix, pseudo & hover du joueur
        Chat.addExtra(playerChatDisplay(dest, hoverDest, ""));
        //Message du joueur
        Chat.addExtra(new TextComponent(ChatColor.translateAlternateColorCodes('&', "&r&6]&r " + msg)));
        return Chat;
    }

    /**
     *      Insert d'un nouveau MSG Chat ou Discord
     * @param p le joueur
     * @param msg le msg
     * @param type 1 chat 3 discord 7 Event
     * @return l'id du msg si besoin
     */
    public int insertSQLNewChat(ProxiedPlayer p, String msg, int type) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        int id = 0;
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "chat`(`player_id`, `msg`, `date_msg`, `msg_type`) VALUES " +
                    "((SELECT player_id FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '"+ p.getUniqueId().toString()+
                    "'), ? , ?, ?) RETURNING msg_id;");
            /* exe de l'instert + récupération du return */
            q.setString(1, msg);
            q.setString(2, DateMilekat.setDateNow());
            q.setInt(3, type);
            q.execute();
            q.getResultSet().next();
            id = q.getResultSet().getInt(1);
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de l'injection d'un message dans le SQL.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        return id;
    }

    /**
     *      Insert d'un nouveau MSG Team
     * @param p le joueur
     * @param msg le msg
     * @return l'id du msg si besoin
     */
    public int insertSQLNewChatTeam(ProxiedPlayer p, String msg) {
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        int id = 0;
        try {
            PreparedStatement q = connection.prepareStatement("INSERT INTO `" + MainBungee.SQLPREFIX +
                    "chat`(`player_id`, `msg`, `date_msg`, `msg_type`, `dest_id`) " +
                    "SELECT (SELECT player_id FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" + p.getUniqueId().toString()+ "'), " +
                    "? , ?, 6, (SELECT `team_id` FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" + p.getUniqueId().toString() + "') " +
                    "WHERE (SELECT `team_id` FROM `" + MainBungee.SQLPREFIX + "player` WHERE `uuid` = '" + p.getUniqueId().toString() + "') " +
                    "RETURNING msg_id;");
            /* exe de l'instert + récupération du return */
            q.setString(1, msg);
            q.setString(2, DateMilekat.setDateNow());
            q.execute();
            if (q.getResultSet().last()) {
                id = q.getResultSet().getInt(1);
            }
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de l'injection d'un message dans le SQL.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        return id;
    }

    /**
     *      Mise en forme d'une requêtes pour récupérer X messages
     * @param NbMsg nombre de messages "chat" à récupérer
     * @return requête SQL avec les messages
     */
    public PreparedStatement getFullChat(int NbMsg){
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        String query = "SELECT chat.msg_id as msg_id, chat.msg as msg, chat.msg_type as msg_type, chat.date_msg as date_msg, " +
                "chat.dest_id as dest_id, removeby.name as remove_by, " +
                //-- Sender info
                "sender.name, sender.uuid, sender.muted as muted, " +
                /*
                "COALESCE(senderT.team_name, 'Pas d''équipe') as team_name, " +
                "COALESCE(senderT.team_tag, 'Pas d''équipe') as team_tag, COALESCE(senderT.money, 'Pas d''équipe') as money, " +
                */
                //-- Dest info
                "dest.name as dest_name, dest.uuid as dest_uuid, dest.muted as dest_muted/*,*/ " +
                /*
                "COALESCE(destT.team_name, 'Pas d''équipe') as dest_team_name, " +
                "COALESCE(destT.team_tag, 'Pas d''équipe') as dest_team_tag, " +
                "COALESCE(destT.money, 'Pas d''équipe') as dest_money " +
                */
                "FROM `" + MainBungee.SQLPREFIX + "chat` chat " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` sender ON chat.player_id = sender.player_id " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` dest ON chat.dest_id = dest.player_id " +
                /*
                "LEFT JOIN luckperms_players senderLP ON sender.uuid = senderLP.uuid " +
                "LEFT JOIN luckperms_groups senderLG ON senderLP.primary_group = senderLG.name " +
                "LEFT JOIN luckperms_players destLP ON dest.uuid = destLP.uuid " +
                "LEFT JOIN luckperms_groups destLG ON destLP.primary_group = destLG.name " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "team` senderT ON sender.team_id = senderT.team_id " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "team` destT ON dest.team_id = destT.team_id " +
                */
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` removeby ON chat.remove_by = removeby.player_id " +
                "WHERE chat.msg_id > (SELECT `msg_id` FROM " +
                "(SELECT `msg_id` FROM `" + MainBungee.SQLPREFIX + "chat` " +
                "WHERE `msg_type` = 1 OR `msg_type` = 3 OR `msg_type` = 4 OR `msg_type` = 5 " +
                "ORDER BY msg_id DESC LIMIT " + NbMsg + ") AS `chat` " +
                "ORDER BY `msg_id` ASC LIMIT 1) ORDER BY chat.msg_id ASC;";
        try {
            PreparedStatement q = connection.prepareStatement(query);
            q.execute();
            return q;
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de la récupération du chat.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        return null;
    }

    /**
     *      Mise en forme d'une requête SQL avec les fulls infos d'un message
     * @param id du message à récupérer
     * @return requête SQL
     */
    public PreparedStatement getChatId(int id){
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        String query = "SELECT chat.msg_id as msg_id, chat.msg as msg, chat.msg_type as msg_type, chat.date_msg as date_msg, " +
                "chat.dest_id as dest_id, removeby.name as remove_by, " +
                //-- Sender info
                "sender.uuid, sender.muted, " +
                /*
                "COALESCE(senderT.team_name, 'Pas d''équipe') as team_name, " +
                "COALESCE(senderT.team_tag, 'Pas d''équipe') as team_tag, COALESCE(senderT.money, 'Pas d''équipe') as money, " +
                */
                //-- Dest info
                "dest.uuid as dest_uuid, dest.muted as dest_muted/*,*/ " +
                /*
                "COALESCE(destT.team_name, 'Pas d''équipe') as dest_team_name, " +
                "COALESCE(destT.team_tag, 'Pas d''équipe') as dest_team_tag, " +
                "COALESCE(destT.money, 'Pas d''équipe') as dest_money " +
                */
                "FROM `" + MainBungee.SQLPREFIX + "chat` chat " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` sender ON chat.player_id = sender.player_id " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` dest ON chat.dest_id = dest.player_id " +
                /*
                "LEFT JOIN luckperms_players senderLP ON sender.uuid = senderLP.uuid " +
                "LEFT JOIN luckperms_groups senderLG ON senderLP.primary_group = senderLG.name " +
                "LEFT JOIN luckperms_players destLP ON dest.uuid = destLP.uuid " +
                "LEFT JOIN luckperms_groups destLG ON destLP.primary_group = destLG.name " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "team` senderT ON sender.team_id = senderT.team_id " +
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "team` destT ON dest.team_id = destT.team_id " +
                */
                "LEFT JOIN `" + MainBungee.SQLPREFIX + "player` removeby ON chat.remove_by = removeby.player_id " +
                "WHERE chat.msg_id = ? ORDER BY chat.msg_id ASC;";
        try {
            PreparedStatement q = connection.prepareStatement(query);
            q.setInt(1, id);
            q.execute();
            return q;
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de la récupération du message : " + id + ".");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
        return null;
    }
}