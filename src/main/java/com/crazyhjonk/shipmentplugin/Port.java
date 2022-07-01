package com.crazyhjonk.shipmentplugin;

import com.crazyhjonk.shipmentplugin.functions.Transfer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Port {

    private static List<Port> ports = new ArrayList<>();

    public static List<Port> getPorts() {
        return ports;
    }
    public static Port getPort(int index) {
        return ports.get(index);
    }

    public static void removePort(Location location) {
        int index = ports.indexOf(findPort(location));
        ports.forEach(port -> port.handleRemovedPort(index));
        List<Transfer> transfersToRemove = new ArrayList<>();
        Transfer.getActiveTransfers().forEach(transfer -> {
            if (transfer.handleRemovedPort(index)) transfersToRemove.add(transfer);
        });
        Transfer.getActiveTransfers().removeAll(transfersToRemove);
        ports.remove(index);
    }

    public static @Nullable Port findPort(String name) {
        for (Port port : ports) {
            if (port.getName().equals(name)) {
                return port;
            }
        }
        return null;
    }

    public static @Nullable Port findPort(Location location) {
        for (Port port : ports) {
            if (port.getLocation().equals(location)) {
                return port;
            }
        }
        return null;
    }

    public static int getPortIndex(Port port) {
        return ports.indexOf(port);
    }

    public static void setPorts(List<Port> ports) {
        Port.ports = ports;
    }

    public static void addPort(Port port) {
        ports.add(port);
    }


    private final String world;
    private final int X;
    private final int Y;
    private final int Z;
    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    private List<Integer> availablePorts = new ArrayList<>();
    public List<Port> getAvailablePorts() {
        List<Port> portsReturn = new ArrayList<>();
        availablePorts.forEach(index -> portsReturn.add(getPort(index)));
        return portsReturn;
    }
    public List<Integer> getAvailablePortsInt() {
        return availablePorts;
    }
    public void setAvailablePorts(@NotNull List<Port> newAvailablePorts) {
        availablePorts = new ArrayList<>();
        newAvailablePorts.forEach(port -> availablePorts.add(getPortIndex(port)));
    }
    public void addAvailablePort(Port addedPort) {
        availablePorts.add(getPortIndex(addedPort));
    }
    public void removeAvailablePort(Port removedPort) {
        availablePorts.remove((Integer) getPortIndex(removedPort));
    }

    public void handleRemovedPort(int index) {
        availablePorts.remove((Integer) index);
        availablePorts.forEach(port -> {
            if (port > index) availablePorts.set(availablePorts.indexOf(port), port - 1);
        });
    }
    private Location[] barrels;
    public Location[] getBarrels() {
        return barrels;
    }
    public Location getBarrel(int index) {
        return barrels[index];
    }
    public void setBarrels(Location[] barrels) {
        this.barrels = barrels;
    }

    public Port(@NotNull Location location, String name) {
        this.world = Objects.requireNonNull(location.getWorld()).getName();
        this.X = location.getBlockX();
        this.Y = location.getBlockY();
        this.Z = location.getBlockZ();
        this.name = name;
        initializeBarrels(location);
    }

    public Port(String world, int X, int Y, int Z, String name) {
        this.world = world;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.name = name;
        initializeBarrels(new Location(Bukkit.getServer().getWorld(world), X, Y, Z));
    }

    public Port(String world, int X, int Y, int Z, String name, @NotNull List<Integer> availablePorts) {
        this.world = world;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.name = name;
        this.availablePorts = availablePorts;
        initializeBarrels(new Location(Bukkit.getServer().getWorld(world), X, Y, Z));
    }

    public Location getLocation() {
        return new Location(Bukkit.getServer().getWorld(world), X, Y, Z);
    }

    private void initializeBarrels(@NotNull Location location) {
        barrels = new Location[] {
                new Location(location.getWorld(), location.getBlockX()+1, location.getBlockY()-1, location.getBlockZ()+1),
                new Location(location.getWorld(), location.getBlockX()-1, location.getBlockY()-1, location.getBlockZ()+1),
                new Location(location.getWorld(), location.getBlockX()-1, location.getBlockY()-1, location.getBlockZ()-1),
                new Location(location.getWorld(), location.getBlockX()+1, location.getBlockY()-1, location.getBlockZ()-1)
        };
    }

}
