package fr.milekat.cite_bungee.chat.utils;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class ChatSend {
    private final ChatFormat chatFormat;

    public ChatSend(){
        chatFormat = new ChatFormat();
    }

    /**
     *      Reload le chat de p sur X msg
     * @param nbMsg Nombre de msg à reload
     * @param p Nom du joueur à reload, si all mettre 'all'
     */
    public void RlChatFor(String p , int nbMsg) {
        try {
            PreparedStatement q = chatFormat.getFullChat(nbMsg);
            while (q.getResultSet().next()) {
                sendSingleMessage(q, p, false);
            }
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors du reload de chat de \" + p + \" pour \" + nbMsg + \" messgaes.");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoi le message X à p
     * @param p Nom du joueur à qui il faut envoyer, si all mettre 'all'
     * @param id du message à envoyer
     * @param isForDest pour éviter le double post sur les mods (false premier coup, true 2ème coup) ->
     *                  false sera envoyé au modos
     */
    public void sendChatFor(String p, int id, boolean isForDest) throws SQLException {
        PreparedStatement q = chatFormat.getChatId(id);
        q.getResultSet().last();
        sendSingleMessage(q, p, isForDest);
        q.close();
    }

    /**
     *      Envoi d'un message du chat à joueur / tous le monde
     * @param q résultat de la requête
     * @param pString Nom du joueur qui va recevoir le message, si all mettre 'all'
     * @param noMods pour éviter le double post sur les mods (false premier coup, true 2ème coup) ->
     *               false sera envoyé au modos
     */
    public void sendSingleMessage(PreparedStatement q, String pString, boolean noMods) throws SQLException {
        // Création du Hover
        String hover = chatFormat.infoPlayerBuilder(
                q.getResultSet().getString("name"),
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("sender.uuid"))),
                q.getResultSet().getString("team_name"),
                q.getResultSet().getString("money"),
                q.getResultSet().getString("player_pts_event"),
                q.getResultSet().getString("date_msg"));
        switch (q.getResultSet().getInt("msg_type")) {
            //  Général
            case 1:
            //  Discord
            case 3: {
                genralMessage(q, hover, pString);
                break;
            }
            //  Message privé
            case 2: {
                privateMessage(q, hover, pString, noMods);
                break;
            }
            //  Annonces chat
            case 4: {
                annonceMessage(q, pString);
                break;
            }
            case 5: {
                loginMessage(q, pString);
                break;
            }
            case 6: {
                teamMessage(q, hover, pString);
                break;
            }
            case 7: {
                eventMessage(q, hover, pString);
                break;
            }
        }
    }

    /**
     *      Envoi d'un message type général dans le chat.
     * @param q Requête du message
     * @param hover infos
     * @param pString joueur à envooyé (ou all)
     */
    private void genralMessage(PreparedStatement q, String hover, String pString) throws SQLException {
        // Message pour Mods on
        TextComponent ModsMsg = chatFormat.chatModsBuilder(
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("sender.uuid"))),
                q.getResultSet().getString("name"),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by")+"",
                Integer.parseInt(q.getResultSet().getString("msg_id")),
                q.getResultSet().getString("muted"));
        // Message pour joueur
        TextComponent PlayerMsg = chatFormat.chatPlayerBuilder(
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("sender.uuid"))),
                q.getResultSet().getString("name"),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by")+"");
        // Ajout du [DISCORD] pour les messages discord
        if (q.getResultSet().getString("msg_type").equals("3")){
            ModsMsg = addDiscord(ModsMsg);
            PlayerMsg = addDiscord(PlayerMsg);
        }
        // Vérif si le joueur est mute ?
        if (q.getResultSet().getString("muted").equals("pas mute")) {
            if (pString.equals("all")) {
                // Envoi du msg à tous les joueurs en ligne
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                        // Pour ceux avec /mods on
                        onlineP.sendMessage(ModsMsg);
                    } else {
                        onlineP.sendMessage(PlayerMsg);
                    }
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
                if (player!=null && MainBungee.profiles.get(player.getUniqueId()).isModson()) {
                    // Pour ceux avec /mods on
                    player.sendMessage(ModsMsg);
                } else if (player!=null) {
                    player.sendMessage(PlayerMsg);
                }
            }
        } else {
            TextComponent Mute = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "[Mute] ");
            Mute.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§cLes modos peuvent"
                    + System.lineSeparator() + "encore voir vos" + System.lineSeparator() + "messages !")));
            Mute.addExtra(ModsMsg);
            if (pString.equals("all")) {
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (onlineP.hasPermission("modo.mute.see")) {
                        onlineP.sendMessage(Mute);
                    }
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
                if (player!=null && player.hasPermission("modo.mute.see")) {
                    player.sendMessage(Mute);
                }
            }
        }
    }

    /**
     *      Envoi d'un MP dans le chat (Au joueurs & Mods)
     * @param q requête du message
     * @param hover infos
     * @param pString joueur cible (Ou all)
     * @param noMods pour éviter le double post sur les mods (false premier coup, true 2ème coup) ->
     *               false sera envoyé au modos
     */
    private void privateMessage(PreparedStatement q, String hover, String pString, boolean noMods) throws SQLException {
        // Création du Hover du destinataire
        String HoverDest = chatFormat.infoPlayerBuilder(
                q.getResultSet().getString("dest_name"),
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("dest_uuid"))),
                q.getResultSet().getString("dest_team_name"),
                q.getResultSet().getString("dest_money"),
                q.getResultSet().getString("dest_player_pts_event"),
                q.getResultSet().getString("date_msg"));
        TextComponent PrivateToMe = chatFormat.privateToMe(
                hover,
                q.getResultSet().getString("name"),
                q.getResultSet().getString("msg"));
        TextComponent PrivateOfMe = chatFormat.privateOfMe(
                HoverDest,
                q.getResultSet().getString("dest_name"),
                q.getResultSet().getString("msg"));
        TextComponent PrivateMods = chatFormat.privateMods(
                hover,
                q.getResultSet().getString("name"),
                HoverDest,
                q.getResultSet().getString("dest_name"),
                q.getResultSet().getString("msg"));
        if (pString.equals("all")) {
            // Envoi du msg à tous les joueurs en ligne
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                if (MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                    // Pour ceux avec /mods on
                    onlineP.sendMessage(PrivateMods);
                } else {
                    if (onlineP.getUniqueId().toString().equals(q.getResultSet().getString("uuid"))) {
                        onlineP.sendMessage(PrivateOfMe);
                    } else if (onlineP.getUniqueId().toString().equals(
                            q.getResultSet().getString("dest_uuid"))) {
                        onlineP.sendMessage(PrivateToMe);
                    }
                }
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
            if(player == null) {
                return;
            }
            if (!noMods) {
                // Pour ceux avec /mods on
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                        if (!onlineP.getUniqueId().toString().equals(q.getResultSet().getString("uuid")) &&
                                !onlineP.getUniqueId().toString().equals(
                                        q.getResultSet().getString("dest_uuid"))){
                            onlineP.sendMessage(PrivateMods);
                        }
                    }
                }
            }
            if (player.getUniqueId().toString().equals(q.getResultSet().getString("uuid"))) {
                player.sendMessage(PrivateOfMe);
            } else if (player.getUniqueId().toString().equals(q.getResultSet().getString("dest_uuid"))) {
                player.sendMessage(PrivateToMe);
            }
        }
    }

    /**
     *      Message type annonce
     * @param q requête du message
     * @param pString joueur cible (Ou all)
     */
    private void annonceMessage(PreparedStatement q, String pString) throws SQLException {
        StringBuilder annonce = new StringBuilder();
        for (String msg : q.getResultSet().getString("msg").split("(?<=\\G.{35,}\\s)")) {
            annonce.append("   ").append(ChatColor.translateAlternateColorCodes('&', msg)).append(System.lineSeparator());
        }
        String msg = "§r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r §2[§6Annonce Cité§7§2]§r §r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r" + System.lineSeparator()
                + System.lineSeparator() + annonce + System.lineSeparator() +
                "§r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r §2[§6Annonce Cité§7§2]§r §r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r";
        if (pString.equalsIgnoreCase("all")) {
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                onlineP.sendMessage(new TextComponent(msg));
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
            if(player != null) {
                player.sendMessage(new TextComponent(msg));
            }
        }
    }

    /**
     *      Message type Join/Leave
     * @param q requête du message
     * @param pString joueur cible (Ou all)
     */
    private void loginMessage(PreparedStatement q, String pString) throws SQLException {
        String msg;
        if (q.getResultSet().getString("msg").equalsIgnoreCase("join")) {
            msg = MainBungee.prefixCmd + "§2" + q.getResultSet().getString("name") + "§6 a rejoint la cité.";
        } else if (q.getResultSet().getString("msg").equalsIgnoreCase("quit")) {
            msg = MainBungee.prefixCmd + "§c" + q.getResultSet().getString("name") + "§6 a quitté la cité.";
        } else {
            return;
        }
        if (pString.equalsIgnoreCase("all")) {
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                onlineP.sendMessage(new TextComponent(msg));
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
            if(player != null) {
                player.sendMessage(new TextComponent(msg));
            }
        }
    }

    /**
     *      Chat team
     * @param q requête du message
     * @param hover infos
     * @param pString joueur cible (Ou all)
     */
    private void teamMessage(PreparedStatement q, String hover, String pString) throws SQLException {
        // Message pour joueur
        TextComponent playerChatDisplay =
                chatFormat.playerChatDisplay(q.getResultSet().getString("name"),hover,"");
        String msg = ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg"));
        TextComponent teamMsg = new TextComponent("§7[" + q.getResultSet().getString("team_name") + "] ");
        teamMsg.addExtra(playerChatDisplay);
        teamMsg.addExtra("§r §b»§r " + msg);
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q2 = connection.prepareStatement("SELECT * FROM `" + MainBungee.SQLPREFIX +
                    "player` WHERE `team_id` = '" + q.getResultSet().getString("dest_id") + "';");
            q2.execute();
            ArrayList<String> teamMembers = new ArrayList<>();
            while (q2.getResultSet().next()) {
                teamMembers.add(q2.getResultSet().getString("uuid"));
            }
            q2.close();
            if (pString.equalsIgnoreCase("all")) {
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (teamMembers.contains(onlineP.getUniqueId().toString()) || MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                        onlineP.sendMessage(teamMsg);
                    }
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
                if(player != null && (teamMembers.contains(player.getUniqueId().toString()) ||
                        MainBungee.profiles.get(player.getUniqueId()).isModson())) {
                    player.sendMessage(teamMsg);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     *      Chat event
     * @param q requête du message
     * @param hover infos
     * @param pString joueur cible (Ou all)
     */
    private void eventMessage(PreparedStatement q, String hover, String pString) throws SQLException {
        TextComponent ModsMsg = new TextComponent("§6[Event]");
        ModsMsg.addExtra(chatFormat.chatModsBuilder(
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("sender.uuid"))),
                q.getResultSet().getString("name"),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by") + "",
                Integer.parseInt(q.getResultSet().getString("msg_id")),
                q.getResultSet().getString("muted")));
        // Message pour joueur
        TextComponent PlayerMsg = new TextComponent("§6[Event]");
        PlayerMsg.addExtra(chatFormat.chatPlayerBuilder(
                Prefix.getPrefix(UUID.fromString(q.getResultSet().getString("sender.uuid"))),
                q.getResultSet().getString("name"),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by") + ""));
        if (pString.equals("all")) {
            // Envoi du msg à tous les joueurs en ligne
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                if (MainBungee.profiles.get(onlineP.getUniqueId()).getChat_mode() == 2) {
                    if (MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                        // Pour ceux avec /mods on
                        onlineP.sendMessage(ModsMsg);
                    } else {
                        onlineP.sendMessage(PlayerMsg);
                    }
                }
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
            // Seulement si pString est un joueur en /chat event
            if (player != null && MainBungee.profiles.get(player.getUniqueId()).getChat_mode() == 2) {
                if (MainBungee.profiles.get(player.getUniqueId()).isModson()) {
                    // Pour ceux avec /mods on
                    player.sendMessage(ModsMsg);
                } else {
                    player.sendMessage(PlayerMsg);
                }
            }
        }
    }

    /**
     *      Ajoute [DISCORD] devant les messages type 3
     * @param msg tellraw de base
     * @return tellraw avec [DISCORD] devant
     */
    private TextComponent addDiscord(TextComponent msg){
        TextComponent var = new TextComponent(ChatColor.BLUE + "(D)");
        var.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(ChatColor.BLUE + "Accéder au Discord")));
        var.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/3q2f53E"));
        var.addExtra(msg);
        return var;
    }
}