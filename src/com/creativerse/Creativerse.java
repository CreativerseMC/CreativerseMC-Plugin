package com.creativerse;

import com.creativerse.commands.*;
import com.creativerse.files.CustomConfig;
import com.creativerse.files.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Creativerse extends JavaPlugin {

    @Override
    public void onEnable() {
        CustomConfig.setup();
        CustomConfig.get().addDefault("ETH-Node", "127.0.0.1:7545");
        CustomConfig.get().addDefault("IPFS-Node", "/ip4/127.0.0.1/tcp/5001");
        CustomConfig.get().addDefault("NFT-Storage-API-Key", "");
        CustomConfig.get().addDefault("Transaction-Domain", "https://www.creativersemc.com");
        CustomConfig.get().addDefault("Contract", "0xe608791A1B4ed2BA44d482b68feFe102a720a4BB");
        CustomConfig.get().addDefault("SQLite", Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder() + "/database/");
        CustomConfig.get().options().copyDefaults(true);
        CustomConfig.save();

        AutoSyncAll.run();
        String dbPath = CustomConfig.get().getString("SQLite");
        Database.initiate(dbPath);

        getCommand("save").setExecutor(new Save());
        getCommand("sync").setExecutor(new Sync());
        getCommand("link").setExecutor(new Link());
        getCommand("syncall").setExecutor(new SyncAll());
        getCommand("creativerse reload").setExecutor(new Reload());
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Creativerse is enabled.");
    }

    @Override
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Creativerse is disabled.");
    }

}
