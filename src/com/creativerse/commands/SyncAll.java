package com.creativerse.commands;

import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.requests.Request;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import com.plotsquared.core.plot.world.PlotAreaManager;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.web3j.contracts.eip721.generated.ERC721Enumerable;
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

public class SyncAll implements CommandExecutor {

    String NODE_URL = CustomConfig.get().getString("ETH-Node");
    String CONTRACT_ADDRESS = CustomConfig.get().getString("Contract");
    String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(command.getName().equalsIgnoreCase("syncall"))) { return true; }
        sender.sendMessage("Syncing all plots with version on the blockchain, this may take some time.");

        Web3j web3 = Web3j.build(new HttpService(NODE_URL));
        TransactionManager txManager = new ReadonlyTransactionManager(web3, CONTRACT_ADDRESS);
        ContractGasProvider gas = new DefaultGasProvider();

        ERC721Metadata contractMetadata = ERC721Metadata.load(CONTRACT_ADDRESS, web3, txManager, gas);
        ERC721Enumerable contractEnumerable = ERC721Enumerable.load(CONTRACT_ADDRESS, web3, txManager, gas);

        try {
            BigInteger totalSupply = contractEnumerable.totalSupply().send();

            for (int i = 0; i < totalSupply.intValue(); i++) {
                BigInteger tokenId = contractEnumerable.tokenByIndex(BigInteger.valueOf(i)).send();

                int[] p = Util.unpair(tokenId.intValue());
                PlotId plotId = PlotId.of(p[0], p[1]);
                PlotAPI plotAPI = new PlotAPI();
                PlotArea plotArea = plotAPI.getPlotAreas("plotworld").iterator().next();
                Plot plot = plotArea.getPlot(plotId);

                String cid = contractMetadata.tokenURI(tokenId).send();

                byte[] fileContents = Request.getFile(IPFS_NODE, cid);

                Clipboard clipboard;

                ClipboardFormat format = ClipboardFormats.findByAlias("schem");
                InputStream input = new ByteArrayInputStream(fileContents);
                try (ClipboardReader reader = format.getReader(input)) {
                    clipboard = reader.read();

                    try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(Bukkit.getWorld("plotworld")))) {
                        CuboidRegion region = plot.getRegions().iterator().next();
                        editSession.setMask(new RegionMask(region));
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(region.getPos1())
                                // configure here
                                .build();
                        Operations.complete(operation);
                    }
                }
            }
            sender.sendMessage(ChatColor.GREEN + "Plots synced!");
        } catch (Exception e) {
            e.printStackTrace();
        }


        return true;
    };
}
