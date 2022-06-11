package com.crazyhjonk.shipmentplugin.listeners;

import com.crazyhjonk.shipmentplugin.Interaction;
import com.crazyhjonk.shipmentplugin.Port;
import com.crazyhjonk.shipmentplugin.functions.GUIs;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;


public class OpenListener implements Listener {

    @EventHandler
    public void onOpenUse(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getPlayer().hasPermission("shipment.use")) return;
        if (Port.getPorts() == null) return;
        int portIndex = getPortIndex(Objects.requireNonNull(event.getClickedBlock()).getLocation());
        if (portIndex == -1) return;
        event.setCancelled(true);
        if (Interaction.get(event.getPlayer()) == null) {
            new Interaction(event.getPlayer(), Port.getPort(portIndex));
            return;
        }
        else if (Interaction.get(event.getPlayer()).isPendingConfirmation()) {
            GUIs.pendingGUI(event.getPlayer());
            return;
        }
        new Interaction(event.getPlayer(), Port.getPort(portIndex));
    }

    public int getPortIndex(Location location) {
        for (int i = 0; i < Port.getPorts().size(); i++) {
            if (Port.getPort(i).getLocation().equals(location)) return i;
        }
        return -1; //Port doesn't exist
    }
}
