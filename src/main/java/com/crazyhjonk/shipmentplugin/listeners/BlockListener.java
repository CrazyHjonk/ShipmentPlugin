package com.crazyhjonk.shipmentplugin.listeners;

import com.crazyhjonk.shipmentplugin.Port;
import com.crazyhjonk.shipmentplugin.functions.FileHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;

public class BlockListener implements Listener {

    @EventHandler
    public void onPlaceBlock(@NotNull BlockPlaceEvent event) {
        if (!event.getPlayer().hasPermission("shipment.create")) return;
        if (event.getBlock().getType() != Material.CRAFTING_TABLE) return;
        Block block = event.getBlock();
        int X = block.getX();
        int Y = block.getY();
        int Z = block.getZ();
        for (int i1 = -1; i1 < 2; i1 = i1 + 2) {
            for (int i2 = -1; i2 < 2; i2 = i2 + 2) {
                if (event.getPlayer().getWorld().getBlockAt(X + i1, Y-1, Z + i2).getType() != Material.BARREL) return;
            }
        }
        event.getPlayer().sendMessage("§6A new port has been created!");
        Port.addPort(new Port(block.getLocation(), "Unnamed Port"));
        FileHandler.updatePortsFile();
    }

    @EventHandler
    public void onBreakBlock(@NotNull BlockBreakEvent event) {
        Block block = event.getBlock();
        Location location = event.getBlock().getLocation();
        if (block.getType() == Material.BARREL) {
            boolean isPortBarrel = false;
            for (int i1 = -1; i1 < 2; i1 = i1 + 2) {
                for (int i2 = -1; i2 < 2; i2 = i2 + 2) {
                    if (Port.findPort(new Location(event.getPlayer().getWorld(), block.getX() + i1, block.getY() + 1, block.getZ() + i2)) != null) {
                        isPortBarrel = true;
                        location = new Location(event.getPlayer().getWorld(), block.getX() + i1, block.getY() + 1, block.getZ() + i2);
                    }
                }
            }
            if (!isPortBarrel) return;
        }
        else if (block.getType() == Material.CRAFTING_TABLE) {
            if (Port.findPort(block.getLocation()) == null) return;
        }
        else return;
        if (!event.getPlayer().isOp()) event.setCancelled(true);
        else {
            Port.removePort(location);
            FileHandler.updatePortsFile();
            FileHandler.updateTransfersFile();
            event.getPlayer().sendMessage("§6You destroyed a port.");
        }
    }
}
