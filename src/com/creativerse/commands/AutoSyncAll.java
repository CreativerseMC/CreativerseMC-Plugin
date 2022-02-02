package com.creativerse.commands;

import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.files.Database;
import com.creativerse.requests.Request;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
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
import org.reactivestreams.Subscription;
import org.web3j.abi.DefaultFunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AutoSyncAll {
    private static String NODE_URL = CustomConfig.get().getString("ETH-Node");
    private static String CONTRACT_ADDRESS = CustomConfig.get().getString("Contract");
    private static String IPFS_NODE = CustomConfig.get().getString("IPFS-Node");

    private static Subscription sub;

    public static void run() {

            Web3j web3 = Web3j.build(new HttpService(NODE_URL));
            sub = (Subscription) web3.transactionFlowable().subscribe(tx -> {
                try {
                    if (!(tx.getTo() == null) && tx.getTo().equals(CONTRACT_ADDRESS.toLowerCase())) {
                        String hexString = tx.getInput();

                        if (hexString.startsWith("0x")) {
                            hexString = hexString.substring(2);
                        }

                        if (hexString.startsWith("78ee7938")) {
                            String idAsString = "";
                            boolean look = true;
                            for (int i=0; i < hexString.substring(8, 72).length(); i++) {
                                if (hexString.substring(8, 72).charAt(i) != '0') {
                                    look = false;
                                }

                                if (!look) {
                                    idAsString = idAsString + hexString.substring(8, 72).charAt(i);
                                }

                            }
                            int id;
                            if (idAsString.equals("")) {
                                id = 0;
                            } else {
                                id = Integer.parseInt(idAsString, 16);
                            }

                            byte[] byteArray = new BigInteger(hexString, 16).toByteArray();
                            String asciiString = new String(byteArray);
                            String[] apart = asciiString.split(";");
                            String cid = apart[1];

                            int[] p = Util.unpair(id);
                            PlotId plotId = PlotId.of(p[0], p[1]);
                            PlotAPI plotAPI = new PlotAPI();
                            PlotArea plotArea = plotAPI.getPlotAreas("plotworld").iterator().next();
                            Plot plot = plotArea.getPlot(plotId);
                            try {
                                String mcUUID = Database.query("SELECT * FROM USERS WHERE ADDRESS='" +
                                        tx.getFrom() + "';")[0];
                                plot.claim(plotAPI.wrapPlayer(UUID.fromString(mcUUID)), false, "", true, false);
                            } catch (IllegalStateException e) {
                                // If user does not exist in db do nothing
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


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
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
    }
}
