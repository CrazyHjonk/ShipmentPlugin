package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Port;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class FileHandler {

    private static final File transferLogs = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder() + File.separator + "Transfers.log");
    private static PrintWriter pw;

    @SuppressWarnings("ConstantConditions")
    public static void loadPorts() {
        File portsFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder() + File.separator + "PortsDatabase.yml");
        if (!portsFile.exists()) {
            try {
                boolean result;
                result = portsFile.createNewFile();
                if (result) System.out.println("Ports File created at " + portsFile.getCanonicalPath());
                else Bukkit.getLogger().info("Ports File exists at " + portsFile.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration portsData = YamlConfiguration.loadConfiguration(portsFile);
        try {
            portsData.load(portsFile);
            for (String portsNum : portsData.getConfigurationSection("ports").getKeys(false)) {
                String name = portsData.getString("ports." + portsNum + ".Name");
                String world = portsData.getString("ports." + portsNum + ".World");
                int X = portsData.getInt("ports." + portsNum + ".X");
                int Y = portsData.getInt("ports." + portsNum + ".Y");
                int Z = portsData.getInt("ports." + portsNum + ".Z");
                List<Integer> availablePorts = portsData.getIntegerList("ports." + portsNum + ".AvailablePorts");
                Port.addPort(new Port(world, X, Y, Z, name, availablePorts));
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updatePortsFile() {
        File portsFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder(), File.separator + "PortsDatabase.yml");
        FileConfiguration portsData = new YamlConfiguration();
        List<Port> ports = Port.getPorts();
        for (int i = 0; i < ports.size(); i++) {
            Port port = ports.get(i);
            if (port == null) return;
            Location location = port.getLocation();
            portsData.createSection("ports." + i);
            portsData.set("ports." + i + ".Name", port.getName());
            portsData.set("ports." + i + ".World", Objects.requireNonNull(location.getWorld()).getName());
            portsData.set("ports." + i + ".X", location.getBlockX());
            portsData.set("ports." + i + ".Y", location.getBlockY());
            portsData.set("ports." + i + ".Z", location.getBlockZ());
            portsData.set("ports." + i + ".AvailablePorts", port.getAvailablePortsInt());
        }
        try {
            portsData.save(portsFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    public static void loadTransfers() {
        File transfersFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder() + File.separator + "TransfersDatabase.yml");
        if (!transfersFile.exists()) {
            try {
                boolean result;
                result = transfersFile.createNewFile();
                if (result) System.out.println("Transfers File created at " + transfersFile.getCanonicalPath());
                else Bukkit.getLogger().info("Transfers File exists at " + transfersFile.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (!transferLogs.exists()) {
            try {
                boolean result;
                result = transferLogs.createNewFile();
                if (result) System.out.println("Transfers Log File created at " + transfersFile.getCanonicalPath());
                else Bukkit.getLogger().info("Transfers Log File exists at " + transfersFile.getCanonicalPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileWriter fw;
        try {
            fw = new FileWriter(transferLogs, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pw = new PrintWriter(fw);
        FileConfiguration transfersData = YamlConfiguration.loadConfiguration(transfersFile);
        try {
            transfersData.load(transfersFile);
            if (transfersData.getConfigurationSection("transfers") == null) return;
            Transfer.setLatestTransferID(transfersData.getInt("latestTransferID"));
            for (String transferNum : transfersData.getConfigurationSection("transfers").getKeys(false)) {
                UUID uuid = UUID.fromString((String) Objects.requireNonNull(transfersData.get("transfers." + transferNum + ".PlayerUUID")));
                int ID = transfersData.getInt("transfers." + transferNum + ".ID");
                int option = transfersData.getInt("transfers." + transferNum + ".Option");
                double time = (double) transfersData.getLong("transfers." + transferNum + ".Time");
                int sendingPort = transfersData.getInt("transfers." + transferNum + ".SendingPort");
                int receivingPort = transfersData.getInt("transfers." + transferNum + ".ReceivingPort");
                @SuppressWarnings("unchecked")
                List<ItemStack> itemsToTransferList = (List<ItemStack>) transfersData.getList("transfers." + transferNum + ".ItemsToTransfer");
                ItemStack[] itemsToTransfer = itemsToTransferList.toArray(new ItemStack[0]);
                Transfer.addActiveTransfer(new Transfer(ID, uuid, option, time, sendingPort, receivingPort, itemsToTransfer));
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTransfersFile() {
        File transfersFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder(), File.separator + "TransfersDatabase.yml");
        FileConfiguration transfersData = new YamlConfiguration();
        List<Transfer> transfers = Transfer.getActiveTransfers();
        transfersData.set("latestTransferID", Transfer.getLatestTransferID());
        for (int i = 0; i < transfers.size(); i++) {
            Transfer transfer = transfers.get(i);
            if (transfer == null) continue;
            transfersData.createSection("transfers." + i);
            transfersData.set("transfers." + i + ".ID", transfer.getID());
            transfersData.set("transfers." + i + ".PlayerUUID", transfer.getOfflinePlayer().getUniqueId().toString());
            transfersData.set("transfers." + i + ".Option", transfer.getOption());
            transfersData.set("transfers." + i + ".Time", (long) transfer.getTime());
            transfersData.set("transfers." + i + ".SendingPort", transfer.getSendingPort());
            transfersData.set("transfers." + i + ".ReceivingPort", transfer.getReceivingPort());
            transfersData.set("transfers." + i + ".ItemsToTransfer", Arrays.asList(transfer.getItemsToTransfer()));

        }
        try {
            transfersData.save(transfersFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void logTransfer(Transfer transfer) {
        pw.println("Transfer with the ID: " + transfer.getID());
        Port sendingPort = Port.getPort(transfer.getSendingPort());
        Location sendingPortLocation = sendingPort.getLocation();
        pw.println("Sending Port: " + " Name = " + sendingPort.getName() + ", X = " + sendingPortLocation.getBlockX() + ", Y = " + sendingPortLocation.getBlockY() + ", Z = "+ sendingPortLocation.getBlockZ() + ", World = " + sendingPortLocation.getWorld());
        Port receivingPort = Port.getPort(transfer.getReceivingPort());
        Location receivingPortLocation = receivingPort.getLocation();
        pw.println("Receiving Port: " + " Name = " + receivingPort.getName() + ", X = " + receivingPortLocation.getBlockX() + ", Y = " + receivingPortLocation.getBlockY() + ", Z = "+ receivingPortLocation.getBlockZ() + ", World = " + receivingPortLocation.getWorld());
        pw.println("Sending Player: Name = " + transfer.getOfflinePlayer().getName() + ", UUID = " + transfer.getOfflinePlayer().getUniqueId());
        pw.println("Sent Items:");
        if (transfer.getItemsToTransfer() == null) {
            pw.println(" ");
            pw.close();
            return;
        }
        for (int i = 0; i < transfer.getItemsToTransfer().length; i++) {
            pw.println(transfer.getItemsToTransfer()[i].serialize());
        }
        pw.println(" ");
        pw.close();
    }
}
