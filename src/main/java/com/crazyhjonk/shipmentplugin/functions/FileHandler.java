package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Port;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileHandler {

    @SuppressWarnings("ConstantConditions")
    public static void loadPorts() {
        File portsFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder() + File.separator + "PortsDatabase.yml");
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

    @SuppressWarnings("ConstantConditions")
    public static void loadTransfers() {
        File transfersFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder() + File.separator + "TransfersDatabase.yml");
        FileConfiguration transfersData = YamlConfiguration.loadConfiguration(transfersFile);
        try {
            transfersData.load(transfersFile);
            if (transfersData.getConfigurationSection("transfers") == null) return;
            for (String transferNum : transfersData.getConfigurationSection("transfers").getKeys(false)) {
                UUID uuid = UUID.fromString((String) Objects.requireNonNull(transfersData.get("transfers." + transferNum + ".PlayerUUID")));
                int option = transfersData.getInt("transfers." + transferNum + ".Option");
                double time = (double) transfersData.getLong("transfers." + transferNum + ".Time");
                int sendingPort = transfersData.getInt("transfers." + transferNum + ".SendingPort");
                int receivingPort = transfersData.getInt("transfers." + transferNum + ".ReceivingPort");
                @SuppressWarnings("unchecked")
                List<ItemStack> itemsToTransferList = (List<ItemStack>) transfersData.getList("transfers." + transferNum + ".ItemsToTransfer");
                ItemStack[] itemsToTransfer = itemsToTransferList.toArray(new ItemStack[0]);
                Transfer.addActiveTransfer(new Transfer(uuid, option, time, sendingPort, receivingPort, itemsToTransfer));
            }
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTransfersFile() {
        File transfersFile = new File(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("ShipmentPlugin")).getDataFolder(), File.separator + "TransfersDatabase.yml");
        FileConfiguration transfersData = new YamlConfiguration();
        List<Transfer> transfers = Transfer.getActiveTransfers();
        for (int i = 0; i < transfers.size(); i++) {
            Transfer transfer = transfers.get(i);
            if (transfer == null) continue;
            transfersData.createSection("transfers." + i);
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
}
