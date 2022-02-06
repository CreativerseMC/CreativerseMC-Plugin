package com.creativerse.commands;

import com.creativerse.Creativerse;
import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.requests.Request;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.command.CommandCategory;
import com.plotsquared.core.command.CommandDeclaration;
import com.plotsquared.core.command.MainCommand;
import com.plotsquared.core.command.RequiredType;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;
import org.web3j.contracts.eip721.generated.ERC721Metadata;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;

public class Sync implements CommandExecutor{
    String NODE_URL = CustomConfig.get().getString("ETH-Node");
    String CONTRACT_ADDRESS = CustomConfig.get().getString("Contract");
    String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("sync"))) { return true; }
        if (!(sender instanceof Player)) { return true; } // Check if command sender is player
        Player player = (Player) sender;
        PlotAPI plotAPI = new PlotAPI();

        PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());
        Plot plot = plotPlayer.getCurrentPlot();

        if (plot.getId().getX() < 0 || plot.getId().getY() < 0) {
            player.sendMessage(ChatColor.RED + "You can only sync plots with a positive ID. The ID of this plot is (" + plot.getId().getX() + ", " + plot.getId().getY() + ")");
            return true;
        }

        if (!player.hasPermission("creativerse.syncall")) {
            if ( !(plot.getOwner().equals(player.getUniqueId()))) {
                player.sendMessage(ChatColor.RED + "You can only sync your plot!");
                return true;
            }
        }


        sender.sendMessage("Syncing this plot with the current state of the blockchain...");

        Web3j web3 = Web3j.build(new HttpService(NODE_URL));
        TransactionManager txManager = new ReadonlyTransactionManager(web3, CONTRACT_ADDRESS);
        ContractGasProvider gas = new DefaultGasProvider();

        ERC721Metadata contract = ERC721Metadata.load(CONTRACT_ADDRESS, web3, txManager, gas);

        int p = Util.pair(plot.getId().getX(), plot.getId().getY());
        BigInteger pBig = BigInteger.valueOf(p);
        try {
            String jsonCid = contract.tokenURI(pBig).send();
//            jsonCid = jsonCid.substring(7); // Removes 'ipfs://'
            JSONObject metadata = new JSONObject(new String(Request.getFile(IPFS_NODE, jsonCid)));
            String cid = metadata.getString("schem").substring(7);

            byte[] fileContents = Request.getFile(IPFS_NODE, cid);

            Clipboard clipboard;

            ClipboardFormat format = ClipboardFormats.findByAlias("schem");
            InputStream input = new ByteArrayInputStream(fileContents);
            try (ClipboardReader reader = format.getReader(input)) {
                clipboard = reader.read();
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(player.getLocation().getWorld()))) {
                    CuboidRegion region = plot.getRegions().iterator().next();
                    editSession.setMask(new RegionMask(region));
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(region.getPos1())
                            // configure here
                            .build();
                    Operations.complete(operation);
                    player.sendMessage(ChatColor.GREEN + "Plot synced!");
                }
            }

        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "ERROR: Token metadata could not be read. You may need to save your plot with /save again. If this error persists, please contact a Creativerse developer.");
            e.printStackTrace();
        }

        return true;
    }
}
