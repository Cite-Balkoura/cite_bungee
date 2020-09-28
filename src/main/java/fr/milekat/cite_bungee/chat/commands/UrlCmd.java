package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.ArrayList;

public class UrlCmd extends Command {
    private final ArrayList<String> allowedURL = new ArrayList<>();
    public UrlCmd() {
        super("url");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) return;
        if (args.length == 1) {
            if (allowedURL.contains(args[0])) {
                TextComponent askUrl = sendUrl(sender.getName(),args[0]);
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    onlineP.sendMessage(askUrl);
                }
            } else {
                sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "§6Demande d'approbation du lien: §b" +
                        args[0] + " §6envoyée au staff !"));
                TextComponent allowUrl = askStaffUrl(sender.getName(), args[0]);
                for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                    if (onlineP.hasPermission("modo.core.url.validation")) onlineP.sendMessage(allowUrl);
                }
            }
        } else if (args.length == 3 && sender.hasPermission("modo.core.url.validation") &&
                args[0].equalsIgnoreCase("allow")) {
            if (allowedURL.contains(args[2])) return;
            allowedURL.add(args[2]);
            TextComponent askUrl = sendUrl(args[1], args[2]);
            for (ProxiedPlayer onlineP : ProxyServer.getInstance().getPlayers()) {
                onlineP.sendMessage(askUrl);
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
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd + "Aide pour la commande url"));
        sender.sendMessage(new TextComponent("§6/url <Lien>:§r Demande au staff d'approuver un URL."));
    }

    /**
     *      Mise en format d'un message json pour demander d'afficher un URL
     */
    private TextComponent askStaffUrl(String player, String url) {
        TextComponent ask_url = new TextComponent(MainBungee.prefixCmd + "§c[URL] §6Le joueur §b" + player +
                " §6propose" + System.lineSeparator() + " §9[CE LIEN] ");
        ask_url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,url));
        ask_url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§b" + url)));
        TextComponent allow = new TextComponent("§a[Accepter]");
        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/url allow " + player + " " + url));
        ask_url.addExtra(allow);
        ask_url.addExtra(new TextComponent("§c[Refuser]"));
        ask_url.setClickEvent(null);
        return ask_url;
    }

    /**
     *      Mise en format d'un message json pour afficher un URL accepté par le staff
     */
    private TextComponent sendUrl(String player, String url) {
        TextComponent sent_url = new TextComponent(MainBungee.prefixCmd + "§b" + player + " §6propose ce lien §9[Clique ici]");
        sent_url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,url));
        sent_url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§b" + url)));
        return sent_url;
    }
}
