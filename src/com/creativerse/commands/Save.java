package com.creativerse.commands;

import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.requests.Request;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.*;

public class Save implements CommandExecutor {
    String DOMAIN = CustomConfig.get().getString("Transaction-Domain");
    String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");
    String API_KEY = CustomConfig.get().getString("NFT-Storage-API-Key");


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("save"))) { return true; }
        if (!(sender instanceof Player)) { return true; } // Check if command sender is player
        Player player = (Player) sender;
        PlotAPI plotAPI = new PlotAPI();


        PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());
        Plot plot = plotPlayer.getCurrentPlot();

        if (plot.getId().getX() < 0 || plot.getId().getY() < 0) {
            player.sendMessage(ChatColor.RED + "You can only claim plots with a positive ID. The ID of this plot is (" + plot.getId().getX() + ", " + plot.getId().getY() + ")");
            return true;
        }

        if (!(plot.getOwner() == null) && !(plot.getOwner().equals(player.getUniqueId()))) {
            player.sendMessage(ChatColor.RED + "You don't own this plot.");
            return true;
        }

        CuboidRegion region = plot.getRegions().iterator().next();
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);


        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getLocation().getWorld()))) {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );

            Operations.complete(forwardExtentCopy);
        } catch (WorldEditException e) {
            e.printStackTrace();
        }


        File file = new File("plot-" + System.currentTimeMillis() + ".schem");

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
            writer.write(clipboard);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int p = Util.pair(plot.getId().getX(), plot.getId().getY());
        int[] xy = Util.unpair(p);

        // Uploads to NFT.Storage
        try {
            String response = Request.upload(API_KEY, file);
            JSONObject object = new JSONObject(response);
            String cid = object.getJSONObject("value").getString("cid");
            file.delete();
            player.sendMessage("Send a transaction using this link: " + DOMAIN + "/?tokenId=" + p + "&cid=" + cid);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

}
