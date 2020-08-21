package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class NoChat extends Command {

    public NoChat() {
        super("nochat","modo.chat.command.nochat");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            if (args.length != 1) {
                sendHelp(sender);
                return;
            }
            if (args[0].equalsIgnoreCase("on")) {
                MainBungee.noChat.put(((ProxiedPlayer) sender).getUniqueId(),true);
            } else if (args[0].equalsIgnoreCase("off")) {
                MainBungee.noChat.remove(((ProxiedPlayer) sender).getUniqueId());
            }
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/nochat on:§r Passage en noChat."));
        sender.sendMessage(new TextComponent("§6/nochat off:§r Sortie du monde noChat."));
    }
}
