package com.zenteno125.commands;

import com.zenteno125.gui.MainMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MissionsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores en línea.");
            return true;
        }

        if (args.length == 0) {
            new MainMenu(player).open();
            return true;
        }

        //  Subcomandos admin (start, stop, lives, reload…)
        //  Se implementarán más adelante.
        return false;
    }
}