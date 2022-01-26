package com.creativerse.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutoSync implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("autosync"))) {
            sender.sendMessage(ChatColor.RED + "This command has not been implemented.");
            return true;
        }

        return true;
    };
}
