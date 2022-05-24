package com.crazyhjonk.shipmentplugin.listeners;


import com.crazyhjonk.shipmentplugin.functions.GUIHandler;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ClickListener implements Listener {

    @EventHandler
    public void onInvClick(final @NotNull InventoryClickEvent event) {
        if (event.getCurrentItem() == null) return;
        int index = switch (event.getView().getTitle()) {
            case "§6Shipping" -> 0;
            case "§6Available Ports" -> 1;
            case "§6Sending" -> 2;
            case "§6Receiving" -> 3;
            case "§6Admin Settings" -> 4;
            case "§6Set Available Ports" -> 5;
            case "§6Pending Transaction..." -> 6;
            case "§6Sent Transfers" -> 7;
            case "§6Viewing Transfer's Items..." -> 8;
            //case "Barrel", "§8Cheese Barrel" -> 99;
            default -> -1;
        };
        if (index == -1) return;
        /*if (index == 99) { //check if the Inventory clicked belongs to the sending barrels of a Port that is currently sending
            boolean isPortBarrel = false;
            Port port = null;
            Block block = Objects.requireNonNull(event.getView().getTopInventory().getLocation()).getBlock();
            for (int i1 = -1; i1 < 2; i1 = i1 + 2) { //only modifying X, since only looking for sending barrels
                Location locationToCheck = new Location(event.getWhoClicked().getWorld(), block.getX() + i1, block.getY() + 1, block.getZ() - 1);
                if (Port.findPort(locationToCheck) != null) {
                    isPortBarrel = true;
                    port = Port.findPort(locationToCheck);
                }
            }
            if (!isPortBarrel) return;

            if (Transfer.checkActiveTransfers(port)) {
                event.getWhoClicked().sendMessage("§aThis Barrel is currently part of a transfer.");
                event.setCancelled(true);
            }
            return;
        }*/
        //Currently, checking for interaction with port barrels isn't needed - I left it in in case I need it at some later point
        event.setCancelled(true);
        ItemStack item = event.getCurrentItem();
        if (item.getItemMeta() == null) return;
        if (!item.getItemMeta().hasEnchant(Enchantment.MENDING)) return;
        if (item.getItemMeta().getEnchantLevel(Enchantment.MENDING) != 4) return;
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        String name = item.getItemMeta().getDisplayName();
        GUIHandler.handleClick(index, name, player, slot);
    }
}
