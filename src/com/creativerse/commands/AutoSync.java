package com.creativerse.commands;

import com.creativerse.files.CustomConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutoSync implements CommandExecutor {
    String NODE_URL = CustomConfig.get().getString("ETH-Node");
    String CONTRACT_ADDRESS = CustomConfig.get().getString("Contract");
    String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("autosync"))) { return true; }
        String autoSyncAllToggle = CustomConfig.get().getString("AutoSyncAll");

        if (autoSyncAllToggle.toLowerCase() == "true") {
            CustomConfig.get().set("AutoSyncAll", "false");
            sender.sendMessage("Auto Sync disabled on all plots.");
        } else if (autoSyncAllToggle.toLowerCase() == "false") {
            CustomConfig.get().set("AutoSyncAll", "true");
            sender.sendMessage("Auto Sync enabled on all plots.");
        } else {
            sender.sendMessage(ChatColor.RED + "An unknown error occured. Please check that \"AutoSyncAll\" is set to \"true\" or \"false\" in the config.yml");
        }

        return true;
    };
}
