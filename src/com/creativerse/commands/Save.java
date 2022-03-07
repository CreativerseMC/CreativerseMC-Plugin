package com.creativerse.commands;

import com.creativerse.Nft;
import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.renderer.McTo3D;
import com.creativerse.renderer.Render;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;

public class Save implements CommandExecutor {
    String DOMAIN = CustomConfig.get().getString("Transaction-Domain");
    String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");
    String API_KEY = CustomConfig.get().getString("NFT-Storage-API-Key");
    String SHOULD_RENDER_PNG = CustomConfig.get().getString("RenderPNG");


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

        player.sendMessage("Compiling plot...");


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

        String name = String.valueOf(System.currentTimeMillis());
        File schemFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/plot-" + name + ".schem");

        try {
            schemFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schemFile))) {
            writer.write(clipboard);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String worldName = player.getLocation().getWorld().getName();
        new Thread(() -> {
        	McTo3D.create3DModel(region, name, worldName);
            File pngFile = null;
            if (SHOULD_RENDER_PNG.equals("true")) {
                pngFile = new Render().objToPng(name);
            }

            File gltfFile = McTo3D.convertObjToGltf(name);

            int p = Util.pair(plot.getId().getX(), plot.getId().getY());
            int[] xz = Util.unpair(p);

            // Uploads to NFT.Storage
            try {
                JSONObject metadata = Nft.createJSON(API_KEY, p, schemFile, gltfFile, pngFile, Long.parseLong(name), xz);
                File metadataFile = new File(Bukkit.getServer().getPluginManager().getPlugin("Creativerse").getDataFolder().getAbsoluteFile().getParentFile().getParentFile() + "/cache/metadata-" + System.currentTimeMillis() + ".json");
                Files.writeString(metadataFile.toPath(), metadata.toString(4));

                JSONObject response = new JSONObject(Request.upload(API_KEY, metadataFile));
                String cid = response.getJSONObject("value").getString("cid");

                schemFile.deleteOnExit();
                metadataFile.deleteOnExit();
                player.sendMessage("Compiled!");
                player.sendMessage(ChatColor.GREEN + "Send a transaction using this link to save: " + ChatColor.YELLOW + DOMAIN + "/save/" + p + "/" + cid);
            } catch (Exception e) { // I know this isn't proper exception handling ill deal with this later ok
                e.printStackTrace();
            }
        }).start();



        return true;
    }

}
