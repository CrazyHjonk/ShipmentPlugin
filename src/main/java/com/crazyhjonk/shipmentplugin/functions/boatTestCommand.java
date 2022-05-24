package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.ShipmentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class boatTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Bukkit.getScheduler().runTaskLater(ShipmentPlugin.getMain(), new Runnable() {
            @Override
            public void run() {

                Player player = (Player) sender;
                Boat boat = null;
                if (player.getVehicle() != null && player.getVehicle() instanceof Boat) {
                    boat = (Boat) player.getVehicle();
                }
                player.sendMessage();
                boat.setVelocity(boat.getVelocity().multiply(10));
            }
        }, 40);
        return true;
    }
}
