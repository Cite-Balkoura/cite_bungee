package fr.milekat.cite_bungee.chat.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.chat.utils.ChatSend;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RemoveMsg extends Command {
    public RemoveMsg() {
        super("removemsg","modo.chat.command.removemsg");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sendHelp(sender);
            return;
        }
        // Mise à jour SQL
        setMessageToRemoved((ProxiedPlayer) sender, Integer.parseInt(args[0]));
        // Reload du chat
        new ChatSend().RlChatFor("all", 25);
    }

    /**
     *      Envoie la liste d'help de la commande
     * @param sender joueur qui exécute la commande
     */
    private void sendHelp(CommandSender sender){
        sender.sendMessage(new TextComponent(MainBungee.prefixCmd));
        sender.sendMessage(new TextComponent("§6/removemsg <id>:§r supprime le message du chat"));
    }

    /**
     *      Update SQL d'un message pour le passer supprimé
     * @param player Joueur qui supprime
     * @param id id du message à supprimer
     */
    private void setMessageToRemoved(ProxiedPlayer player, int id){
        Connection connection = MainBungee.getInstance().getSql().getConnection();
        try {
            PreparedStatement q = connection.prepareStatement("UPDATE `" + MainBungee.SQLPREFIX +
                    "chat` SET `remove_by`= (SELECT `player_id` FROM `" + MainBungee.SQLPREFIX +
                    "player` WHERE `uuid` = '" + player.getUniqueId() + "') WHERE `msg_id` = '" + id + "';");
            q.execute();
            q.close();
        } catch (SQLException throwables) {
            MainBungee.warning("Erreur lors de la suppression du message id: " + id + ".");
            if (MainBungee.logDebug) throwables.printStackTrace();
        }
    }
}
