package fr.milekat.cite_bungee;

import fr.milekat.cite_bungee.chat.commands.*;
import fr.milekat.cite_bungee.chat.engines.Annonces;
import fr.milekat.cite_bungee.chat.engines.AntiSpam;
import fr.milekat.cite_bungee.chat.engines.MuteEngine;
import fr.milekat.cite_bungee.chat.events.ChatMsg;
import fr.milekat.cite_bungee.chat.events.ChatSubscribe;
import fr.milekat.cite_bungee.core.Jedis.JedisSub;
import fr.milekat.cite_bungee.core.commands.*;
import fr.milekat.cite_bungee.core.engines.BanEngine;
import fr.milekat.cite_bungee.core.engines.PlayersEngine;
import fr.milekat.cite_bungee.core.events.JoinLeaveEvent;
import fr.milekat.cite_bungee.core.events.PlayerPing;
import fr.milekat.cite_bungee.core.obj.Profil;
import fr.milekat.cite_bungee.utils_tools.DateMilekat;
import fr.milekat.cite_bungee.utils_tools.MariaManage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainBungee extends Plugin {
    // Core
    public static Date maintenance = DateMilekat.getDate("01/01/2020 00:00:00");
    public final static Date opendate = DateMilekat.getDate("24/10/2020 14:00:00");
    public static final String prefixCmd = "§6[§2Balkoura§6]§r ";
    public static final String prefixConsole = "[Balkoura] ";
    public static final String SQLPREFIX = "balkoura_";
    public static boolean jedisDebug;
    public static boolean logDebug;
    public static ArrayList<UUID> approbationList = new ArrayList<>();
    public static HashMap<UUID, Profil> profiles = new HashMap<>();
    public static HashMap<String, UUID> joueurslist = new HashMap<>();
    // Engines
    private ScheduledTask banTask;
    private ScheduledTask profilesTask;

    // Chat
    public static HashMap<String, String> linkToken = new HashMap<>();
    public static HashMap<UUID, Integer> spamChat = new HashMap<>();
    public static HashMap<UUID, String> lastMsg = new HashMap<>();
    public static HashMap<UUID, Boolean> noChat = new HashMap<>();
    // Engines
    private ScheduledTask annoncesTask;
    private ScheduledTask antispamTask;
    private ScheduledTask muteTask;

    // Vars du Main
    private static MainBungee mainBungee;
    public Jedis jedis;
    public JedisSub subscriber;
    private Configuration config;
    private MariaManage sql;
    private final PluginManager pm = ProxyServer.getInstance().getPluginManager();

    @Override
    public void onEnable(){
        mainBungee = this;
        // Config File
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).
                    load(new File(getDataFolder(),"config.yml"));
        } catch (IOException e) {
            MainBungee.warning("Erreur config File: " + e);
        }
        jedisDebug = config.getBoolean("redis.debug");
        logDebug = config.getBoolean("other.debug_exeptions");
        /* SQL */
        sql = new MariaManage("jdbc:mysql://",
                config.getString("SQL.host"),
                config.getString("SQL.db-user"),
                config.getString("SQL.user"),
                config.getString("SQL.log"));
        sql.connection();
        /* Core */
        // Init des vars
        maintenance = DateMilekat.getDate(config.getString("configs.maintenance.until"));
        ProxyServer.getInstance().getLogger().info(prefixConsole + "Date de maintenance:" + maintenance);
        // Engines
        profilesTask = new PlayersEngine().runTask();
        banTask = new BanEngine().runTask();
        // Events
        pm.registerListener(this, new JoinLeaveEvent());
        pm.registerListener(this, new PlayerPing());
        // Command
        pm.registerCommand(this, new Debugger());
        pm.registerCommand(this, new DiscordUrl());
        pm.registerCommand(this, new Ban());
        pm.registerCommand(this, new UnBan());
        pm.registerCommand(this, new Kick());
        /* Chat */
        // Events
        pm.registerListener(this, new ChatMsg());
        pm.registerListener(this, new ChatSubscribe());
        // Commandes
        pm.registerCommand(this, new Broadcast());
        pm.registerCommand(this, new Chat());
        pm.registerCommand(this, new ModoChat());
        pm.registerCommand(this, new Mute());
        pm.registerCommand(this, new UnMute());
        pm.registerCommand(this, new RemoveMsg());
        pm.registerCommand(this, new Message());
        pm.registerCommand(this, new Reply());
        pm.registerCommand(this, new TeamChat());
        pm.registerCommand(this, new UrlCmd());
        pm.registerCommand(this, new NoChat());
        pm.registerCommand(this, new Maintenance());
        // Engines
        annoncesTask = new Annonces().runTask();
        antispamTask = new AntiSpam().runTask();
        muteTask = new MuteEngine().runTask();
        /* Jedis */
        jedis = new Jedis(config.getString("redis.host"),
                Integer.parseInt(Objects.requireNonNull(config.getString("redis.port"))),
                0);
        jedis.auth(config.getString("redis.auth"));
        subscriber = new JedisSub();
        new Thread(() -> {
            try {
                jedis.subscribe(subscriber, "discord", "cite", "event", "survie", "bungee");
            } catch (Exception e) {
                MainBungee.warning("Subscribing failed : " + e);
            }
        }).start();
        if (jedisDebug) {
            MainBungee.info(prefixConsole + "Debug jedis activé");
        }
    }

    @Override
    public void onDisable(){
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).
                    save(config, new File(getDataFolder(),"config.yml"));
        } catch (IOException e) {
            ProxyServer.getInstance().getLogger().warning("Erreur de save du config File :" + e);
        }
        annoncesTask.cancel();
        antispamTask.cancel();
        muteTask.cancel();
        banTask.cancel();
        profilesTask.cancel();
        subscriber.unsubscribe();
        sql.disconnect();
    }

    public static MainBungee getInstance(){
        return mainBungee;
    }

    public Configuration getConfig(){
        return config;
    }

    public MariaManage getSql(){
        return sql;
    }

    public static void info(String log) {
        ProxyServer.getInstance().getLogger().info(MainBungee.prefixConsole + log);
    }

    public static void warning(String log) {
        ProxyServer.getInstance().getLogger().warning(MainBungee.prefixConsole + log);
    }

    public static void log(String message) {
        ProxyServer.getInstance().getLogger().info(message);
    }

    public static ArrayList<String> playerListFull(String arg) {
        List<String> MyStrings = new ArrayList<>(MainBungee.joueurslist.keySet());
        ArrayList<String> MySortStrings =new ArrayList<>();
        for(int i = 0; i<MainBungee.profiles.keySet().size(); i++)
        {
            if(MyStrings.get(i).toLowerCase().startsWith(arg.toLowerCase()))
            {
                MySortStrings.add(MyStrings.get(i));
            }
        }
        return MySortStrings;
    }

    public static ArrayList<String> playerListOnline(String arg) {
        List<String> MyStrings = null;
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) MyStrings.add(player.getName());
        ArrayList<String> MySortStrings = new ArrayList<>();
        if (!(MyStrings ==null)) {
            for (String myString : MyStrings) {
                if (myString.toLowerCase().startsWith(arg.toLowerCase())) {
                    MySortStrings.add(myString);
                }
            }
        }
        return MySortStrings;
    }
}