package fr.milekat.cite_bungee.chat.events;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatFormat;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import fr.milekat.cite_bungee.core.jedis.JedisPub;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.SQLException;
import java.util.Arrays;

public class ChatMsg implements Listener {
    private final MainBungee mainCore = MainBungee.getInstance();
    private final String[] urlDetect = {"//","http",".fr",".com",".net",".org"};

    /**
     *      Event de gestion des nvx messages !
     * @param event Event du send message !
     */
    @EventHandler
    public void onChat(ChatEvent event) throws SQLException {
        if (event.isCommand()) return;
        ProxiedPlayer player = (ProxiedPlayer) event.getSender();
        if (MainBungee.noChat.getOrDefault(player.getUniqueId(),false)) return;
        event.setCancelled(true);
        String message = event.getMessage();
        if (mainCore.getConfig().getBoolean("configs.chat.safe_chat")){
            message = cleanMessages(event.getMessage(),player);
            if (message==null) return;
        }
        int id = 0;
        String prefix = "";
        switch (MainBungee.profiles.get(player.getUniqueId()).getChat_mode()){
            case 0:
            {
                // Injection du msg dans le SQL et récupération de son ID
                id = new ChatFormat().insertSQLNewChat(player,message,1);
                prefix = "[Chat]";
                break;
            }
            case 1:
            {
                // Injection du msg dans le SQL et récupération de son ID
                id = new ChatFormat().insertSQLNewChatTeam(player,message);
                if (id == 0) {
                    player.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTu n'as pas de team."));
                    return;
                }
                prefix = "[Team]";
                break;
            }
            case 2:
            {
                if (MainBungee.profiles.get(player.getUniqueId()).isMute()){
                    player.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cChat event indisponible pour les mutes."));
                    warnMute(player);
                    return;
                }
                // Injection du msg dans le SQL et récupération de son ID
                id = new ChatFormat().insertSQLNewChat(player,message,7);
                prefix = "[Event]";
                break;
            }
        }
        // Envoie du message à tous le monde
        new ChatSend().sendChatFor("all",id,false);
        // Log du message dans la console, comme un message normal
        MainBungee.log(prefix + " <" + player.getDisplayName() + "> " + message);
        if (MainBungee.profiles.get(player.getUniqueId()).isMute()){
            warnMute(player);
        }
        // Redis
        JedisPub.sendRedis("new_msg#:#" + id);
    }

    /**
     *      Envoie le temps de mute restant !
     * @param player joueur
     */
    private void warnMute(ProxiedPlayer player) {
        String str = DateMilekat.reamingToString(DateMilekat.getDate(MainBungee.profiles.get(player.getUniqueId()).getMuted()));
        TextComponent Mute = new TextComponent(MainBungee.prefixCmd + "§6Vous serez unmute dans §b" + str + "§c.");
        Mute.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new Text("§cLes modos peuvent" +
                System.lineSeparator() + "encore voir vos" + System.lineSeparator() + "messages")));
        player.sendMessage(Mute);
    }

    /**
     *      If safe_chat is enable, filter words, spamming and upercases
     * @param message message à check
     * @param sender qui envoi le msg
     * @return message check ou null !
     */
    private String cleanMessages(String message, ProxiedPlayer sender){
        // Évite le spam chat
        int spamchat = MainBungee.spamChat.getOrDefault(sender.getUniqueId(),0) + 1;
        MainBungee.spamChat.replace(sender.getUniqueId(),spamchat);
        if (spamchat>mainCore.getConfig().getInt("configs.chat.chat_spam_limit_kick")) {
            sender.disconnect(new TextComponent("Trop de spam !"));
            return null;
        }
        if (spamchat>mainCore.getConfig().getInt("configs.chat.chat_spam_limit_warn")) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cTrop de spam !"));
            MainBungee.lastMsg.put(sender.getUniqueId(),message);
            return null;
        }
        if (MainBungee.lastMsg.getOrDefault(sender.getUniqueId(),"").equalsIgnoreCase(message)) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cMessage déjà envoyé."));
            return null;
        }
        MainBungee.lastMsg.put(sender.getUniqueId(),message);
        // Deny des URL
        if (Arrays.stream(urlDetect).parallel().anyMatch(message::contains)) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§cURL interdits ! §6Merci d'utiliser /url !"));
            return null;
        }
        // Réduction des messages en MAJ
        if (message.length()>mainCore.getConfig().getInt("configs.chat.minlowercaselength")) {
            int upperCase = 0;
            int lowerCase = 0;
            for (int k = 0; k < message.length(); k++) {
                if (Character.isUpperCase(message.charAt(k))) upperCase++;
                if (Character.isLowerCase(message.charAt(k))) lowerCase++;
            }
            if (upperCase>lowerCase) {
                message = message.toLowerCase();
            }
        }
        // Retirer les mots interdits
        String[] messages = message.split(" ");
        for (String word : messages) {
            if (mainCore.getConfig().getList("configs.chat.banned_words").contains(word)) {
                message = message.replaceAll(word,word.replaceAll(".","*"));
            }
        }
        return message;
    }
}