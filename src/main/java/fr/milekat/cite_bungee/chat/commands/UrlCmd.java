package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class UrlCmd extends Command {
    public UrlCmd() {
        super("url");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        if (args.length == 1) {
            sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Demande d'approbation du lien : §b" + args[0] +
                    " §6envoyée au staff !"));
            TextComponent allowUrl = askUrl(sender.getName(),args[0]);
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                onlineP.sendMessage(allowUrl);
            }
        } else if (args.length == 3 && sender.hasPermission("modo.core.url.validation")) {
            if (sender.hasPermission("modo.core.allow_url")) {
                TextComponent askUrl = sendUrl(args[1], args[2]);
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (onlineP.hasPermission("modo.core.allow_url")) {
                        onlineP.sendMessage(askUrl);
                    }
                }
            }
        } else {
            sendHelp(sender);
        }
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/url <Lien>:§r Demande au staff d'approuver un URL."));
    }

    private TextComponent askUrl(String player, String url) {
        TextComponent ask_url = new TextComponent(MainBungee.prefixCmd + "§c[URL] §6Le joueur §b" + player + " §6propose §9[CE LIEN]");
        ask_url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,url));
        TextComponent allow =  new TextComponent("§a[Accepter]");
        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/url allow " + player + " " + url));
        ask_url.addExtra(allow);
        ask_url.addExtra(new TextComponent("§c[Refuser]"));
        return ask_url;
    }

    private TextComponent sendUrl(String player, String url) {
        TextComponent sent_url = new TextComponent("§b" + player + " §6propose ce lien §9[Clique ici]");
        sent_url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,url));
        return sent_url;
    }
}
