package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Interaction;
import com.crazyhjonk.shipmentplugin.Port;
import com.crazyhjonk.shipmentplugin.ShipmentPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Barrel;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Transfer {

    private static final List<Transfer> activeTransfers = new ArrayList<>();
    public static List<Transfer> getActiveTransfers() {
        return activeTransfers;
    }
    public static void addActiveTransfer(Transfer transfer) {
        activeTransfers.add(transfer);
    }

    private final Economy economy;

    private Player player;

    private final OfflinePlayer offlinePlayer;
    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    private final int capacity;
    private final double duration;
    private int price = 0;
    private final int risk;

    private int sendingPort;
    public int getSendingPort() {
        return sendingPort;
    }
    private final Inventory[] currentInventories = new Inventory[4];

    private int receivingPort;
    public int getReceivingPort() {
        return receivingPort;
    }
    private final Inventory[] selectedInventories = new Inventory[4];

    private double time;
    public double getTime() {
        return time;
    }

    private final int option;
    public int getOption() {
        return option;
    }

    private ItemStack[] itemsToTransfer;
    public ItemStack[] getItemsToTransfer() {
        return itemsToTransfer;
    }

    private boolean completed = false;
    public boolean isCompleted() {
        return completed;
    }

    public Transfer(Player player, int option) {
        this.economy = ShipmentPlugin.getEconomy();
        this.player = player;
        this.offlinePlayer = player;
        this.sendingPort = Port.getPortIndex(Interaction.get(player).getCurrentPort());
        this.receivingPort = Port.getPortIndex(Interaction.get(player).getSelectedPort());
        this.option = option;
        if (option == -2) {
            //admin option
            this.capacity = 8;
            this.duration = 1.0/30.0;
            this.risk = 0;
            Interaction.get(player).setActiveTransfer(this);
            initiateTransfer();
            return;
        }
        this.capacity = GUIs.getCapacity(option);
        this.duration = GUIs.getDuration(option);
        this.price = GUIs.getPrice(option);
        this.risk = GUIs.getRisk(option);
        Interaction.get(player).setActiveTransfer(this);
        initiateTransfer();
    }

    public Transfer(UUID uuid, int option, double time, int sendingPort, int receivingPort, ItemStack[] itemsToTransfer) {
        this.economy = ShipmentPlugin.getEconomy();
        this.offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        if (offlinePlayer.isOnline()) {
            this.player = Bukkit.getOfflinePlayer(uuid).getPlayer();
        }
        this.sendingPort = sendingPort;
        this.receivingPort = receivingPort;
        this.option = option;
        this.itemsToTransfer = itemsToTransfer;
        this.time = time;
        if (option == -2) {
            //admin option
            this.capacity = 8;
            this.duration = 1.0/30.0;
            this.price = 0;
            this.risk = 0;
        }
        else {
            this.capacity = GUIs.getCapacity(option);
            this.duration = GUIs.getDuration(option);
            this.price = GUIs.getPrice(option);
            this.risk = GUIs.getRisk(option);
        }

        fetchInventories();
        if (java.time.Clock.systemUTC().millis() > time) {
            completed = true;
        }
        else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    completed = true;
                }
            }.runTaskLater(ShipmentPlugin.getMain(), Math.round((time - java.time.Clock.systemUTC().millis())/50)); //50 cuz time is in millis, and this uses ticks
        }
    }

    public void handleRemovedPort(int index) {
        if (sendingPort > index) sendingPort--;
        else if (sendingPort == index) {
            ShipmentPlugin.getEconomy().depositPlayer(offlinePlayer, price);
            if (offlinePlayer.isOnline()) player.sendMessage("§3Since a port which was part of a transfer of yours was destroyed, the price has been refunded.");
            activeTransfers.remove(this);
        }
        if (receivingPort > index) receivingPort--;
        else if (receivingPort == index) {
            ShipmentPlugin.getEconomy().depositPlayer(offlinePlayer, price);
            if (offlinePlayer.isOnline()) player.sendMessage("§3Since a port which was part of a transfer of yours was destroyed, the price has been refunded.");
            activeTransfers.remove(this);
        }
    }

    public static boolean checkActiveTransfers(Port port) {
        int portIndex = Port.getPortIndex(port);
        AtomicBoolean found = new AtomicBoolean(false);
        activeTransfers.forEach(transfer -> {
            if (transfer.sendingPort == portIndex) {
                found.set(true);
            }
        });
        return found.get();
    }

    public void confirmTransfer() {
        Interaction.get(player).setPendingConfirmation(false);
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), player::closeInventory);
        if (!handlePrice()) return;
        fetchInventories();
        fetchItems();
        new BukkitRunnable() {
            @Override
            public void run() {
                completed = true;
            }
        }.runTaskLater(ShipmentPlugin.getMain(), (long) (duration * 72000L));
        this.time = java.time.Clock.systemUTC().millis() + duration * 3600000L;
        activeTransfers.add(this);
        FileHandler.updateTransfersFile();
        player.sendMessage("§3The Transport will commence in " + (int) duration + " hour(s). Items have been removed from the Departure Barrels.");
    }

    public void receiveTransfer() {
        transfer();
        activeTransfers.remove(this);
        FileHandler.updateTransfersFile();
    }

    private void initiateTransfer() {
        if (economy.getBalance(player) < price) {
            player.sendMessage("§4You don't have enough money for this option.");
            return;
        }
        Interaction.get(player).setPendingConfirmation(true);
        GUIs.pendingGUI(player);
    }


    private void fetchItems() {
        @SuppressWarnings("unchecked") HashMap<Integer, ItemStack>[] itemsToTransferMap = new HashMap[2];
        ItemStack[] itemsToTransfer = new ItemStack[capacity];
        int index = 0;
        for (int i = 0; i < 2; i++) {
            if (currentInventories[i].getContents() != null) {
                itemsToTransferMap[i] = new HashMap<>();
                for (int j = 0; j < 27; j++) {
                    if (index >= capacity) break;
                    ItemStack content = currentInventories[i].getContents()[j];
                    if (content != null) {
                        itemsToTransferMap[i].put(j, content);
                        itemsToTransfer[index] = content;
                        index++;
                    }
                }
            }
        }
        ItemStack[] itemsAfterRisk = handleRisk(itemsToTransfer);
        if (itemsAfterRisk == null) return;
        this.itemsToTransfer = itemsAfterRisk;
        for (int i = 0; i < 2; i++) {
            int finalI = i;
            itemsToTransferMap[i].forEach((key, value) -> currentInventories[finalI].setItem(key, null)); //remove items from old container
        }
    }

    private void transfer() {
        ArrayList<ItemStack> remainingItems = new ArrayList<>();
        for (ItemStack itemStack : itemsToTransfer) {
            boolean isRemaining = true;
            for (int i = 2; i < 4; i++) {
                if (selectedInventories[i].firstEmpty() != -1) {
                    if (itemStack != null) {
                        selectedInventories[i].addItem(itemStack); //add items to new container
                        isRemaining = false;
                        break;
                    }
                }
            }
            if (isRemaining) {
                remainingItems.add(itemStack);
            }
        }
        remainingItems.removeIf(Objects::isNull);
        for (int i = 0; i < 2; i++) {
            if (!remainingItems.isEmpty()) {
                if (selectedInventories[i].firstEmpty() != -1) {
                    remainingItems = new ArrayList<>(selectedInventories[i].addItem(remainingItems.toArray(ItemStack[]::new)).values());
                }
            }
            //to-do: queue up as new transfer
        }
    }

    private boolean handlePrice() {
        if (economy.withdrawPlayer(player, price).transactionSuccess()) {
            player.sendMessage("§aYou have paid " + price + "$ for this transfer.");
            return true;
        }
        else {
            player.sendMessage("§4You don't have enough money for this transfer.");
            return false;
        }
    }

    private ItemStack @Nullable [] handleRisk(@NotNull ItemStack[] itemStacks) {
        List<ItemStack> itemStackList = new ArrayList<>(Arrays.asList(itemStacks));
        itemStackList.removeIf(Objects::isNull);
        for (int i = 0; i < itemStackList.size(); i++) {
            if (Math.random() < risk/100D) {
                itemStackList.remove(i);
                i--;
            }
        }
        if (itemStackList.isEmpty()) return null;
        return itemStackList.toArray(new ItemStack[0]);
    }
    private void fetchInventories() {
        Port sendingPort = Port.getPort(this.sendingPort);
        Port receivingPort = Port.getPort(this.receivingPort);
        for (int i = 0; i < 4; i++) {
            Inventory currentInventory = ((Barrel) Objects.requireNonNull(sendingPort.getLocation().getWorld()).getBlockAt(sendingPort.getBarrel(i)).getState()).getInventory();
            Inventory selectedInventory = ((Barrel) Objects.requireNonNull(receivingPort.getLocation().getWorld()).getBlockAt(receivingPort.getBarrel(i)).getState()).getInventory();
            currentInventories[i] = currentInventory;
            selectedInventories[i] = selectedInventory;
        }
    }
}
