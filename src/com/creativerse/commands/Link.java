package com.creativerse.commands;

import com.creativerse.Creativerse;
import com.creativerse.Util;
import com.creativerse.files.CustomConfig;
import com.creativerse.files.Database;
import com.plotsquared.core.PlotAPI;
import com.plotsquared.core.player.PlotPlayer;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.plotsquared.core.plot.PlotId;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.web3j.contracts.eip721.generated.ERC721;
import org.web3j.contracts.eip721.generated.ERC721Enumerable;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Link implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String CONTRACT_ADDRESS = CustomConfig.get().getString("Contract");;
        String DOMAIN = CustomConfig.get().getString("Transaction-Domain");
        String NODE_URL = CustomConfig.get().getString("ETH-Node");;
        String msgPrefix = "\u0019Ethereum Signed Message:\n";

        if (!(command.getName().equalsIgnoreCase("link"))) { return true; }
        if (!(sender instanceof Player)) { return true; } // Check if command sender is player
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.GREEN + "Please click the link to link your Ethereum address:");
            player.sendMessage(DOMAIN + "/link?uuid=" + player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "Then use "+ ChatColor.YELLOW + "/link <signature> " + ChatColor.GREEN + "to finish linking account.");
            return true;
        }


        String msg = "I agree that the account signing this message will be linked to the Minecraft Java account with the UUID of "+player.getUniqueId();
        byte[] messageBytes = (msgPrefix + msg.length() + msg).getBytes(StandardCharsets.UTF_8);
        byte[] signatureBytes = Numeric.hexStringToByteArray(args[0]);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        Sign.SignatureData sd = new Sign.SignatureData(
                v,
                Arrays.copyOfRange(signatureBytes, 0, 32),
                Arrays.copyOfRange(signatureBytes, 32, 64)
        );

        try {
            BigInteger pubKey = Sign.signedMessageToKey(messageBytes, sd);
            String address = "0x" + Keys.getAddress(pubKey);

            Web3j web3 = Web3j.build(new HttpService(NODE_URL));
            TransactionManager txManager = new ReadonlyTransactionManager(web3, CONTRACT_ADDRESS);
            ContractGasProvider gas = new DefaultGasProvider();

            ERC721 ERC721contract = ERC721.load(CONTRACT_ADDRESS, web3, txManager, gas);
            ERC721Enumerable ERC721Enumerablecontract = ERC721Enumerable.load(CONTRACT_ADDRESS, web3, txManager, gas);

            int balanceOf = ERC721contract.balanceOf(address).send().intValue();
            List<BigInteger> tokensOwned = new ArrayList<BigInteger>();

            for (int i = 0; i < balanceOf; i++) {
                BigInteger tokenId = ERC721Enumerablecontract.tokenOfOwnerByIndex(address, BigInteger.valueOf(i)).send();
                tokensOwned.add(tokenId);
            }

            PlotAPI plotAPI = new PlotAPI();
            PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());
            PlotArea plotArea = plotPlayer.getLocation().getPlotArea();
            for (int i = 0; i < tokensOwned.size(); i++) {
                int[] p = Util.unpair(tokensOwned.get(i).intValue());
                PlotId plotId = PlotId.of(p[0], p[1]);
                Plot plot = plotArea.getPlot(plotId);
                try {
                    plot.claim(plotPlayer, false, "", true, false);
                } catch (IllegalStateException e) {
                    // If plot already is claimed do nothing
                } catch (Exception e) {
                    plot.setOwner(player.getUniqueId());
                    e.printStackTrace();
                }

            }

            Database.execute("INSERT OR REPLACE INTO USERS (UUID,ADDRESS) VALUES ('" +
                    player.getUniqueId().toString() + "', '" +
                    address + "');");
            Database.commit();

            player.sendMessage(ChatColor.GREEN + "Your account is now linked with " + ChatColor.YELLOW + address);
            player.sendMessage(ChatColor.GREEN + "You can use " + ChatColor.YELLOW + "/plot home " + ChatColor.GREEN + "to teleport to your plot.");
        } catch (SignatureException e) {
            player.sendMessage(ChatColor.RED + "Error. Please check your signature and try again.");
            e.printStackTrace();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
