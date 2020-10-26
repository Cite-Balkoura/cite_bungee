package fr.milekat.cite_bungee.core.obj;

import java.util.UUID;

public class Profil {
    private final int id;
    private final UUID uuid;
    private final String name;
    private final int team;
    private int chat_mode;
    private String muted;
    private String banned;
    private String reason;
    private boolean modson;
    private final boolean maintenance;
    private final long discordid;
    private final int points_quest;
    private final int points_event;

    public Profil(int id, UUID uuid, String name, int team, int chat_mode, String muted, String banned, String reason, boolean modson, boolean maintenance, long discordid, int points_quest, int points_event) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.team = team;
        this.chat_mode = chat_mode;
        this.muted = muted;
        this.banned = banned;
        this.reason = reason;
        this.modson = modson;
        this.maintenance = maintenance;
        this.discordid = discordid;
        this.points_quest = points_quest;
        this.points_event = points_event;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getTeam() {
        return team;
    }

    public int getChat_mode() {
        return chat_mode;
    }

    public void setChat_mode(int chat_mode) {
        this.chat_mode = chat_mode;
    }

    public String getMuted() {
        return muted;
    }

    public void setMuted(String muted) {
        this.muted = muted;
    }

    public String getBanned() {
        return banned;
    }

    public void setBanned(String banned) {
        this.banned = banned;
    }

    public boolean isModson() {
        return modson;
    }

    public void setModson(boolean modson) {
        this.modson = modson;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public boolean isMute(){
        return !this.muted.equals("pas mute");
    }

    public boolean isBan(){
        return !this.banned.equals("pas ban");
    }

    public long getDiscordid() {
        return discordid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getPoints_quest() {
        return points_quest;
    }

    public int getPoints_event() {
        return points_event;
    }
}
