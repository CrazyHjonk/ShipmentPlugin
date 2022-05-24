package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Interaction;
import com.crazyhjonk.shipmentplugin.Port;
import com.crazyhjonk.shipmentplugin.ShipmentPlugin;
import com.crazyhjonk.shipmentplugin.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class GUIs {

    private static final String[] names = new String[] {"§4Sloop", "§6Caravel", "§6Brig", "§6Carrack", "§2Galleon"};

    private static final String[] descriptions = new String[] {"§fSwift, but small and insecure.", "§fLarger than a Sloop, but fairly similar otherwise.",
            "§fA well-rounded ship.", "§fA large trading ship.", "§fA highly secure warship."};

    private static final int[] capacities = new int[] {9, 18, 18, 27, 9};
    public static int getCapacity(int index) {return capacities[index];}

    private static final int[] durations = new int[] {1, 2, 4, 6, 4};
    public static int getDuration(int index) {return durations[index]*distance;}

    private static final int[] prices = new int[] {500, 2500, 5000, 10000, 20000};
    public static int getPrice(int index) {return Math.toIntExact(Math.round(prices[index]*priceMod));}

    private static final int[] risks = new int[] {10, 6, 4, 2, 0};
    public static int getRisk(int index) {return risks[index];}

    private static final Material[] materials = new Material[] {Material.RED_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE};

    private static int distance = 1000;
    private static double priceMod = 0.75;
    public static void mainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, "§6Shipping");
        inventory.setItem(0, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE).setDisplayname("§3Outgoing Transfers")
                .setLore("§bView the pending Transfers sent from here").build());
        inventory.setItem(2, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayname("§2Send Items")
                .setLore("§3Use this to send players items").build());
        inventory.setItem(4, new ItemBuilder(Material.PAPER).setDisplayname("§3Port Name:")
                .setLore("§b§o" + Interaction.get(player).getCurrentPort().getName()).build());
        inventory.setItem(6, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE).setDisplayname("§2Receive Items")
                .setLore("§3Use this to receive items from players").build());
        if (player.isOp()) inventory.setItem(8, new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .setDisplayname("§3Admin Settings").setLore("§3Enter Admin Settings").build());
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void availablePortsGUI(Player player) {
        List<Port> ports = Interaction.get(player).getCurrentPort().getAvailablePorts();
        int size = ports.size()/9 + 1;
        Inventory inventory = Bukkit.createInventory(player, size*9, "§6Available Ports");
        inventory.setItem((size*9) - 1, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());
        ports.forEach((port -> {
            Location location = port.getLocation();
            inventory.addItem(new ItemStack(
                    new ItemBuilder(Material.CRAFTING_TABLE)
                            .setDisplayname(port.getName())
                            .setLore("§3X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(),
                                    "§6Distance: " + calculateDistance(Interaction.get(player).getCurrentPort().getLocation(), location))
                            .build()
            ));
        }));
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }
    public static void setAvailablePortsGUI(Player player) {
        List<Port> ports = new ArrayList<>(Port.getPorts());
        ports.remove(Interaction.get(player).getCurrentPort());
        List<Port> currentlyAvailablePorts = Interaction.get(player).getCurrentPort().getAvailablePorts();
        int size = ports.size()/9 + 1;
        Inventory inventory = Bukkit.createInventory(player, size*9, "§6Set Available Ports");
        inventory.setItem((size*9) - 1, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());
        ports.forEach((port -> {
            Location location = port.getLocation();
            inventory.addItem(new ItemStack(
                    new ItemBuilder(Material.CRAFTING_TABLE)
                            .setDisplayname(port.getName())
                            .setLore("§3X: " + location.getBlockX() + " Y: " + location.getBlockY() + " Z: " + location.getBlockZ(),
                                    "§6Distance: " + calculateDistance(Interaction.get(player).getCurrentPort().getLocation(), location), "§4Currently available: §f" + currentlyAvailablePorts.contains(port))
                            .build()
            ));
        }));
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void sendingGUI(Player player) {
        Port selectedPort = Interaction.get(player).getSelectedPort();
        distance = calculateDistance(Interaction.get(player).getCurrentPort().getLocation(), selectedPort.getLocation())/1000;
        String[] hourText = new String[] {" hours", " hours", " hours", " hours", " hours",};
        for (int j = 0; j < 5; j++) if (durations[j]*distance == 1) hourText[j] = " hour";
        priceMod = 0.75 + 0.25*distance;
        Inventory inventory = Bukkit.createInventory(player, 9, "§6Sending");
        inventory.setItem(0, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setDisplayname("§2Admin Abuse")
                .setLore("§fLiterally just admin abuse.", "§1Cost: §f$0", "§6Capacity: Infinite I guess?", "§5Duration: Instant :)", "§4Risk: What's that? kekw", "actually, just testing here").build());
        for (int i = 0; i < 5; i++) {
            inventory.setItem(i + 2, new ItemBuilder(materials[i]).setDisplayname(names[i]).setLore(descriptions[i], "§1Cost: §f$" + Math.round(priceMod*prices[i]),
                    "§6Capacity: " + capacities[i] + " Slots", "§5Duration: " + durations[i]*distance + hourText[i], "§4Risk: " + risks[i] + "%").build());
        }
        inventory.setItem(8, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void receivingGUI(Player player) {
        List<Transfer> activeTransfers = Transfer.getActiveTransfers();
        List<Transfer> eligibleTransfers = new ArrayList<>();
        int currentPort = Port.getPortIndex(Interaction.get(player).getCurrentPort());
        activeTransfers.forEach(transfer -> {
            if (transfer.getReceivingPort() == currentPort) {
                eligibleTransfers.add(transfer);
            }
        });

        int size = eligibleTransfers.size()/9 + 1;
        Inventory inventory = Bukkit.createInventory(player, size*9, "§6Receiving");
        inventory.setItem(size*9 - 1, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());

        AtomicInteger i = new AtomicInteger();
        eligibleTransfers.forEach(transfer -> {
            String completedLore;
            if (transfer.isCompleted()) completedLore = "§dClick to collect this Shipment";
            else completedLore = "§dThe Shipment will arrive in " + Math.round((transfer.getTime() - Clock.systemUTC().millis())/60000) + " minute(s)";
            inventory.setItem(i.get(), new ItemBuilder(Material.PAPER).setDisplayname("§2From: " + Port.getPort(transfer.getSendingPort()).getName())
                    .setLore("§b" + transfer.getItemsToTransfer().length + " items", completedLore).build());
            i.getAndIncrement();
        });
        if (eligibleTransfers.isEmpty()) {
            inventory.setItem(4, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayname("§4Receive Items")
                    .setLore("§5You don't currently have any items to receive.").build());
        }
        Interaction.get(player).setAvailableTransfers(eligibleTransfers);
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void pendingGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, "§6Pending Transaction...");
        inventory.setItem(4, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE).setDisplayname("§2Confirm Transaction")
                .setLore("§d§oMake sure the Port barrels contain", "§d§oonly the items you are wanting to send.").build());
        inventory.setItem(8, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").setLore("§o§1This will cancel the Transaction.").build());
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void sentTransfersGUI(Player player) {
        List<Transfer> activeTransfers = Transfer.getActiveTransfers();
        List<Transfer> eligibleTransfers = new ArrayList<>();
        int currentPort = Port.getPortIndex(Interaction.get(player).getCurrentPort());
        activeTransfers.forEach(transfer -> {
            if (transfer.getSendingPort() == currentPort) {
                eligibleTransfers.add(transfer);
            }
        });

        int size = eligibleTransfers.size()/9 + 1;
        Inventory inventory = Bukkit.createInventory(player, size*9, "§6Sent Transfers");
        inventory.setItem(size*9 - 1, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());

        AtomicInteger i = new AtomicInteger();
        eligibleTransfers.forEach(transfer -> {
            String completedLore;
            if (transfer.isCompleted()) completedLore = "§dThe Transfer is ready to be collected";
            else completedLore = "§dThe Transfer will arrive in " + Math.round((transfer.getTime() - Clock.systemUTC().millis())/60000) + " minute(s)";
            inventory.setItem(i.get(), new ItemBuilder(Material.PAPER).setDisplayname("§2To: " + Port.getPort(transfer.getReceivingPort()).getName()).
                    setLore("§b" + transfer.getItemsToTransfer().length + " items", completedLore, "§6Click to show items").build());
            i.getAndIncrement();
        });
        if (eligibleTransfers.isEmpty()) {
            inventory.setItem(4, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayname("§4Sent Transfers")
                    .setLore("§5You don't currently have any outgoing transfers.").build());
        }
        Interaction.get(player).setAvailableTransfers(eligibleTransfers);
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void transferViewItemsGUI(Player player, Transfer transfer) {
        ItemStack[] items = transfer.getItemsToTransfer();
        int size = items.length/9 + 1;
        Inventory inventory = Bukkit.createInventory(player, size*9, "§6Viewing Transfer's Items...");
        inventory.setContents(items);
        inventory.setItem(size*9 - 1, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static int calculateDistance(Location location1, Location location2) {
        double distance = Math.sqrt(Math.pow(location1.getBlockX() - location2.getBlockX(), 2)
                + Math.pow(location1.getBlockY() - location2.getBlockY(), 2)
                + Math.pow(location1.getBlockZ() - location2.getBlockZ(), 2));
        if (distance < 1000) return 1000;
        return (int) Math.ceil(distance/1000)*1000;
    }
}