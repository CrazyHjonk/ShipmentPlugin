package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Interaction;
import com.crazyhjonk.shipmentplugin.ShipmentPlugin;
import com.crazyhjonk.shipmentplugin.utils.ItemBuilder;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class AdminSettings {

    public static void mainGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9, "§6Admin Settings");
        if (player.hasPermission("shipment.setname"))
            inventory.setItem(2, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayname("§5Set Name").
                setLore("§3Set the Port's Name").build());
        if (player.hasPermission("shipment.setavailable"))
            inventory.setItem(6, new ItemBuilder(Material.RED_STAINED_GLASS_PANE).setDisplayname("§5Set Available Ports").
                setLore("§3Set reachable Ports").build());
        inventory.setItem(8, new ItemBuilder(Material.ARROW).setDisplayname("§3Back").build());
        Bukkit.getScheduler().runTask(ShipmentPlugin.getMain(), () -> player.openInventory(inventory));
    }

    public static void setName(Player givenPlayer) {
        new AnvilGUI.Builder()
                .onComplete(((player, reply) -> {
                    Interaction.get(player).getCurrentPort().setName(reply);
                    FileHandler.updatePortsFile();
                    return AnvilGUI.Response.close();
                }))
                .text("Name...")
                .title("Setting Port Name")
                .plugin(ShipmentPlugin.getMain())
                .open(givenPlayer);
    }

    public static void setAvailablePorts(Player player) {
        GUIs.setAvailablePortsGUI(player);
    }
}
