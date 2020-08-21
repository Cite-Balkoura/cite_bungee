package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DiscordUrl extends Command {
    public DiscordUrl() {
        super("discord");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 2){
            TextComponent DiscordLink = new TextComponent("§9[Lien]");
            DiscordLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§9Clique pour rejoindre le discord").create()));
            DiscordLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/3q2f53E"));
            sender.sendMessage(new TextComponent("§6Rejoindre le Discord" + DiscordLink));
            return;
        }
        if (args[0].equalsIgnoreCase("link")){
            if (MainBungee.linkToken.containsKey(args[1])) {
                Connection connection = MainBungee.getInstance().getSql().getConnection();
                try {
                    PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                            "player` SET `discord_id`= ? WHERE `uuid` = ?;");
                    q.setString(1, MainBungee.linkToken.get(args[1]));
                    q.setString(2,((ProxiedPlayer) sender).getUniqueId().toString());
                    q.execute();
                    q.close();
                    MainBungee.linkToken.remove(args[1]);
                    sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Compte discord associé !"));
                } catch (SQLException throwables) {
                    MainBungee.warning(MainBungee.prefixCmd + "Erreur SQL commande discord.");
                    if (MainBungee.logDebug) throwables.printStackTrace();
                }
            } else {
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§ctoken non reconnu ou expiré."));
            }
        }
    }
}