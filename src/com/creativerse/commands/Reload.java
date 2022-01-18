package com.creativerse.commands;

import com.creativerse.files.CustomConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class Reload implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("creativerse reload"))) {
            return true;
        }

        CustomConfig.reload();
        return true;
    }
}
