package fr.milekat.cite_bungee.core.obj;

import java.util.ArrayList;

public class Team {
    private final int id;
    private final String name;
    private final String tag;
    private final int money;
    private final ArrayList<Profil> members;

    public Team(int id, String name, String tag, int money, ArrayList<Profil> members) {
        this.id = id;
        this.name = name;
        this.tag = tag;
        this.money = money;
        this.members = members;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public int getMoney() {
        return money;
    }

    public ArrayList<Profil> getMembers() {
        return members;
    }

    public int getSize() {
        return this.members.size();
    }
}

