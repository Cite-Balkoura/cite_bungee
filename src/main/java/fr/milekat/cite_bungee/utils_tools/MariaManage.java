package fr.milekat.cite_bungee.utils_tools;

import fr.milekat.cite_bungee.MainBungee;
import net.md_5.bungee.api.ProxyServer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaManage {
    private Connection connection;
    private final String driver,url,host,database,user,pass;

    public MariaManage(String url, String host, String database, String user, String pass){
        this.driver = "com.mysql.jdbc.Driver";
        this.url = url;
        this.host = host;
        this.database = database;
        this.user = user;
        this.pass = pass;
    }

    public void connection(){
        try {
            Class.forName(this.driver);
            connection = DriverManager.getConnection(url + host + "/" + database + "?autoReconnect=true&allowMultiQueries=true&characterEncoding=UTF-8", user, pass);
            ProxyServer.getInstance().getLogger().info(MainBungee.prefixConsole + "SQL connecté !");
        } catch (SQLException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + "Erreur SQL.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + "Erreur Class SQL.");
        }
    }

    public void disconnect(){
        try {
            connection.close();
            ProxyServer.getInstance().getLogger().info(MainBungee.prefixConsole + "SQL déconnecté !");
        } catch (SQLException e) {
            e.printStackTrace();
            ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + "Erreur SQL.");
        }
    }

    public Connection getConnection(){
        return connection;
    }
}
