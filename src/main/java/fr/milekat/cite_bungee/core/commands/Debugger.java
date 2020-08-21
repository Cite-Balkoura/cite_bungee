package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.ArrayList;

public class Debugger extends Command implements TabExecutor {
    public Debugger() {
        super("debugger", "admin.core.command.debugger");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelp(sender);
            return;
        }
        switch (args[0].toLowerCase())
        {
            case "exeptions":
            {
                MainBungee.logDebug = !MainBungee.logDebug;
                MainBungee.getInstance().getConfig().set("other.debug_exeptions", MainBungee.logDebug);
                sender.sendMessage(new TextComponent("Java exeptions debug: " + MainBungee.logDebug));
                break;
            }
            case "jedis":
            {
                MainBungee.jedisDebug = !MainBungee.jedisDebug;
                MainBungee.getInstance().getConfig().set("redis.debug", MainBungee.jedisDebug);
                sender.sendMessage(new TextComponent("Jedis debug: " + MainBungee.jedisDebug));
                break;
            }
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6/debugger <debugger>:§r Switch un débugger."));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            ArrayList<String> tabs = new ArrayList<>();
            tabs.add("exeptions");
            tabs.add("jedis");
            return tabs;
        }
        return new ArrayList<>();
    }
}
