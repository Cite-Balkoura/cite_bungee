package fr.milekat.cite_bungee.chat.utils;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.core.obj.Profil;
import fr.milekat.cite_bungee.core.obj.Team;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
                sendSingleMessage(q, p, true);
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
        if (q.getResultSet().getInt("msg_type")==4) {
            //  Annonces chat
            annonceMessage(q, pString);
            return;
        }
        // Récupération Profil & Équipe du sender
        Profil senderProfil = MainBungee.profiles.getOrDefault
                (UUID.fromString(q.getResultSet().getString("sender.uuid")), null);
        Team senderTeam = MainBungee.teams.getOrDefault(senderProfil.getTeam(), null);
        // Création du Hover
        String hover = chatFormat.infoPlayerBuilder(
                senderProfil.getName(),
                Prefix.getPrefix(senderProfil.getUuid()),
                senderTeam.getName(),
                String.valueOf(senderTeam.getMoney()),
                senderProfil.getPoints_event(),
                senderProfil.getPoints_quest(),
                q.getResultSet().getString("date_msg"));
        switch (q.getResultSet().getInt("msg_type")) {
            //  Général
            case 1:
            //  Discord
            case 3: {
                genralMessage(q, hover, pString, senderProfil);
                break;
            }
            //  Message privé
            case 2: {
                privateMessage(q, hover, pString, noMods, senderProfil);
                break;
            }
            // Message de login
            case 5: {
                loginMessage(q, pString, senderProfil);
                break;
            }
            // Chat Team
            case 6: {
                teamMessage(q, hover, pString, senderProfil, senderTeam);
                break;
            }
            // Chat Event
            case 7: {
                eventMessage(q, hover, pString, senderProfil);
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
    private void genralMessage(PreparedStatement q, String hover, String pString, Profil profil) throws SQLException {
        // Message pour Mods on
        TextComponent ModsMsg = chatFormat.chatModsBuilder(
                Prefix.getPrefix(profil.getUuid()),
                profil.getName(),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by")+"",
                Integer.parseInt(q.getResultSet().getString("msg_id")),
                q.getResultSet().getString("muted"));
        // Message pour joueur
        TextComponent PlayerMsg = chatFormat.chatPlayerBuilder(
                Prefix.getPrefix(profil.getUuid()),
                profil.getName(),
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
                    if (onlineP.hasPermission("modo.bungee.mute.see")) {
                        onlineP.sendMessage(Mute);
                    }
                }
            } else {
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
                if (player!=null && player.hasPermission("modo.bungee.mute.see")) {
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
    private void privateMessage(PreparedStatement q, String hover, String pString, boolean noMods,
                                Profil senderProfil) throws SQLException {
        // Récupération Profil & Équipe du dest
        Profil destProfil = MainBungee.profiles.getOrDefault
                (UUID.fromString(q.getResultSet().getString("dest_uuid")),null);;
        Team destTeam = MainBungee.teams.getOrDefault(destProfil.getTeam(),null);
        // Création du Hover du destinataire
        String hoverDest = chatFormat.infoPlayerBuilder(
                destProfil.getName(),
                Prefix.getPrefix(destProfil.getUuid()),
                destTeam.getName(),
                String.valueOf(destTeam.getMoney()),
                destProfil.getPoints_event(),
                destProfil.getPoints_quest(),
                q.getResultSet().getString("date_msg"));
        TextComponent PrivateToMe = chatFormat.privateToMe(hover, senderProfil.getName(),
                q.getResultSet().getString("msg"));
        TextComponent PrivateOfMe = chatFormat.privateOfMe(hoverDest, destProfil.getName(),
                q.getResultSet().getString("msg"));
        TextComponent PrivateMods = chatFormat.privateMods(hover, senderProfil.getName(), hoverDest, destProfil.getName(),
                q.getResultSet().getString("msg"));
        if (pString.equals("all")) {
            // Envoi du msg à tous les joueurs en ligne
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                if (MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                    // Pour ceux avec /mods on
                    onlineP.sendMessage(PrivateMods);
                } else {
                    if (onlineP.getUniqueId().equals(senderProfil.getUuid())) {
                        onlineP.sendMessage(PrivateOfMe);
                    } else if (onlineP.getUniqueId().equals(destProfil.getUuid())) {
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
        if (q.getResultSet().getString("msg").contains("%nl%")) {
            for (String msg : q.getResultSet().getString("msg").split("%nl%")) {
                annonce.append("   ")
                        .append(ChatColor.translateAlternateColorCodes('&', msg))
                        .append(System.lineSeparator());
            }
        } else {
            for (String msg : q.getResultSet().getString("msg").split("(?<=\\G.{35,}\\s)")) {
                if (msg.length() > 1) annonce.append("   ")
                        .append(ChatColor.translateAlternateColorCodes('&', msg))
                        .append(System.lineSeparator());
            }
        }
        String msg = "§r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r §7[§6Annonce Cité§7§7]§r §r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r" + System.lineSeparator()
                + System.lineSeparator() + annonce + System.lineSeparator() +
                "§r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r §7[§6Annonce Cité§7§7]§r §r§7§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯§r";
        sendToAll(pString, msg);
    }

    /**
     *      Message type Join/Leave
     * @param q requête du message
     * @param pString joueur cible (Ou all)
     */
    private void loginMessage(PreparedStatement q, String pString, Profil senderProfil) throws SQLException {
        String msg;
        if (q.getResultSet().getString("msg").equalsIgnoreCase("join")) {
            msg = MainBungee.prefixCmd + "§2" + senderProfil.getName() + "§6 a rejoint la cité.";
        } else if (q.getResultSet().getString("msg").equalsIgnoreCase("quit")) {
            msg = MainBungee.prefixCmd + "§c" + senderProfil.getName() + "§6 a quitté la cité.";
        } else {
            return;
        }
        sendToAll(pString, msg);
    }

    private void sendToAll(String pString, String msg) {
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
    private void teamMessage(PreparedStatement q, String hover, String pString, Profil profil, Team team) throws SQLException {
        // Message pour joueur
        TextComponent playerChatDisplay = chatFormat.playerChatDisplay(profil.getName(), hover,"");
        String msg = ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg"));
        TextComponent teamMsg = new TextComponent("§7[" + team.getName() + "] ");
        teamMsg.addExtra(playerChatDisplay);
        teamMsg.addExtra("§r §b»§r " + msg);
        ArrayList<UUID> membres = new ArrayList<>();
        for (Profil memberProfil: team.getMembers()) membres.add(memberProfil.getUuid());
        if (pString.equalsIgnoreCase("all")) {
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                if (membres.contains(onlineP.getUniqueId()) || MainBungee.profiles.get(onlineP.getUniqueId()).isModson()) {
                    onlineP.sendMessage(teamMsg);
                }
            }
        } else {
            ProxiedPlayer player = ProxyServer.getInstance().getPlayer(pString);
            if(player != null && (membres.contains(player.getUniqueId()) ||
                    MainBungee.profiles.get(player.getUniqueId()).isModson())) {
                player.sendMessage(teamMsg);
            }
        }
    }

    /**
     *      Chat event
     * @param q requête du message
     * @param hover infos
     * @param pString joueur cible (Ou all)
     */
    private void eventMessage(PreparedStatement q, String hover, String pString, Profil profil) throws SQLException {
        TextComponent ModsMsg = new TextComponent("§6[Event]");
        ModsMsg.addExtra(chatFormat.chatModsBuilder(
                Prefix.getPrefix(profil.getUuid()),
                profil.getName(),
                ChatColor.translateAlternateColorCodes('&', q.getResultSet().getString("msg")),
                hover,
                q.getResultSet().getString("remove_by") + "",
                Integer.parseInt(q.getResultSet().getString("msg_id")),
                q.getResultSet().getString("muted")));
        // Message pour joueur
        TextComponent PlayerMsg = new TextComponent("§6[Event]");
        PlayerMsg.addExtra(chatFormat.chatPlayerBuilder(
                Prefix.getPrefix(profil.getUuid()),
                profil.getName(),
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