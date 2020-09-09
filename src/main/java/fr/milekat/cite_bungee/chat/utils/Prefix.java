package fr.milekat.cite_bungee.chat.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.md_5.bungee.api.ChatColor;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Prefix {
    public static String getPrefix(UUID uuid) {
        String prefix = "";
        LuckPerms api = LuckPermsProvider.get();
        try {
            User user = api.getUserManager().loadUser(uuid).get();
            CachedMetaData cachedMetaData = user.getCachedData().getMetaData();
            if (cachedMetaData.getPrefix()!=null)
                prefix = ChatColor.translateAlternateColorCodes('&', cachedMetaData.getPrefix());
        } catch (NullPointerException | InterruptedException | ExecutionException ignore) {}
        return prefix;
    }
}
