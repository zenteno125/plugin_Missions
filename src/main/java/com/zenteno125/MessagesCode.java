package com.zenteno125;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class MessagesCode extends JavaPlugin {
    /**
     * This class contains all the messages used in the plugin.
     * It is recommended to use this class to manage messages.
     *
     * @author zenteno125
     */
    public static final String PREFIX = "&8[&6Missions&8] ";
    public static String author = "zenteno125";

    public static class RunMessages {
       public static final String START = PREFIX + "By " + author;
       public static final String DISABLE = PREFIX +  "has been disabled!";
    }

    public static String getMessagesColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&', PREFIX + msg);
    }





}