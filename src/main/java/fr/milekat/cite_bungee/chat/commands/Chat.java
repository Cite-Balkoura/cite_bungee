package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

public class Chat extends Command implements TabExecutor {
    public Chat() {
        super("chat");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length<1) {
            sendHelp(sender);
            return;
        }
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        UUID pUuid = ((ProxiedPlayer) sender).getUniqueId();
        try {
            PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                    "player` SET `chat_mode` = ? WHERE `player`.`uuid` = ?;");
            switch (args[0].toLowerCase()) {
                case "all":
                {
                    q.setInt(1,0);
                    MainBungee.profiles.get(pUuid).setChat_mode(0);
                    break;
                }
                case "team":
                {
                    q.setInt(1,1);
                    MainBungee.profiles.get(pUuid).setChat_mode(1);
                    break;
                }
                case "event":
                {
                    q.setInt(1,2);
                    MainBungee.profiles.get(pUuid).setChat_mode(2);
                    break;
                }
                default:
                {
                    sendHelp(sender);
                    return;
                }
            }
            q.setString(2,pUuid.toString());
            q.execute();
            q.close();
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Chat défini sur " + args[0].toLowerCase()));
        } catch (SQLException throwables) {
            MainBungee.warning("Impossible d'update le chat_mode de " + sender.getName());
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Chat help !"));
        sender.sendMessage(new TextComponent("§6/chat all: §rPassage en mode chat général."));
        sender.sendMessage(new TextComponent(
                "§6/chat team: §rPassage en mode chat équipe, seul votre équipe verra vos messages."));
        sender.sendMessage(new TextComponent(
                "§6/chat event: §rPassage en mode chat évent, seuls les joueurs dans ce mode verons vos messages"));
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer && args.length == 1) {
            return new ArrayList<>(Arrays.asList("all", "team", "event"));
        }
        return new ArrayList<>();
    }
}
