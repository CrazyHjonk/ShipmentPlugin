package com.crazyhjonk.shipmentplugin;

import com.crazyhjonk.shipmentplugin.functions.FileHandler;
import com.crazyhjonk.shipmentplugin.listeners.BlockListener;
import com.crazyhjonk.shipmentplugin.listeners.ClickListener;
import com.crazyhjonk.shipmentplugin.listeners.OpenListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ShipmentPlugin extends JavaPlugin {

    private static ShipmentPlugin main;

    private static Economy econ = null;
    private static final Logger log = Logger.getLogger("Minecraft");



    @Override
    public void onEnable() {
        main = this;
        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - ShipmentPlugin could not find the Economy System.", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(new OpenListener(), this);
        Bukkit.getPluginManager().registerEvents(new ClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        FileHandler.loadPorts();
        FileHandler.loadTransfers();
        //this.getCommand("boatTestCommand").setExecutor(new boatTestCommand());
    }

    @Override
    public void onDisable() {
        FileHandler.updateTransfersFile();
        FileHandler.updatePortsFile();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static ShipmentPlugin getMain() {
        return main;
    }

    public static Economy getEconomy() {
        return econ;
    }
}