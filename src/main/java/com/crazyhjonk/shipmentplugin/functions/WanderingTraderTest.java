package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.ShipmentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.scheduler.BukkitRunnable;

public class WanderingTraderTest {

    public static void updateTraderPositions() {
        World world = Bukkit.getWorld("world");
        assert world != null;
        world.getEntitiesByClass(WanderingTrader.class).forEach(wanderingTrader -> wanderingTrader.damage(20));
        for (int i = 0; i < 4; i++) {
            int X = (int) Math.round(Math.random()*15000);
            int Z = (int) Math.round(Math.random()*15000);
            int Y = world.getHighestBlockAt(X, Z).getY();
            world.spawnEntity(new Location(world, X, Y, Z), EntityType.WANDERING_TRADER);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTraderPositions();
            }
        }.runTaskLater(ShipmentPlugin.getMain(), 1728000);
    }
}
