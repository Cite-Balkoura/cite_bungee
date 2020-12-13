package fr.milekat.cite_bungee.core.commands;

import fr.milekat.cite_bungee.MainBungee;
import fr.milekat.cite_bungee.utils_tools.Tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddCrate extends Command {
    public AddCrate() {
        super("shopaddcrate", "admin.shop.command.addcrate");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (args.length == 3) {
                Connection connection = MainBungee.getInstance().getSql().getConnection();
                PreparedStatement q = connection.prepareStatement("SELECT `crates` FROM `balkoura_player` WHERE `name` = ?");
                q.setString(1, args[0]);
                q.execute();
                q.getResultSet().next();
                String[] crates = q.getResultSet().getString("crates").split(";");
                q.close();
                String[] modifiedcrate = crates[Integer.parseInt(args[1])-1].split(":");
                crates[Integer.parseInt(args[1])-1] = modifiedcrate[0] + ":" + (Integer.parseInt(modifiedcrate[1]) + Integer.parseInt(args[2]));
                StringBuilder stringBuilder = new StringBuilder();
                for (String crate : crates) {
                    stringBuilder.append(crate).append(";");
                }
                PreparedStatement q2 = connection.prepareStatement("UPDATE `balkoura_player` SET `crates`= ? WHERE `name` = ?;");
                q2.setString(1, Tools.remLastChar(stringBuilder.toString()));
                q2.setString(2, args[0]);
                q2.execute();
                q2.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
