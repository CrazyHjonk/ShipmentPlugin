package com.crazyhjonk.shipmentplugin;

import com.crazyhjonk.shipmentplugin.functions.GUIs;
import com.crazyhjonk.shipmentplugin.functions.Transfer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Interaction {

    private static final HashMap<Player, Interaction> interactions = new HashMap<>();
    public static HashMap<Player, Interaction> getInteractions() {
        return interactions;
    }
    public static Interaction get(Player player) {
        return interactions.get(player);
    }

    private final Player player;

    private final Port currentPort;
    public Port getCurrentPort() {
        return currentPort;
    }

    private Port selectedPort;
    public Port getSelectedPort() {
        return selectedPort;
    }
    public void setSelectedPort(Port selectedPort) {
        this.selectedPort = selectedPort;
    }

    private boolean pendingConfirmation;

    public boolean isPendingConfirmation() {
        return pendingConfirmation;
    }

    public void setPendingConfirmation(boolean pendingConfirmation) {
        this.pendingConfirmation = pendingConfirmation;
    }

    private Transfer activeTransfer;
    public Transfer getActiveTransfer() {
        return activeTransfer;
    }

    public void setActiveTransfer(Transfer activeTransfer) {
        this.activeTransfer = activeTransfer;
    }

    private List<Transfer> availableTransfers = new ArrayList<>();
    public List<Transfer> getAvailableTransfers() {
        return availableTransfers;
    }
    public void addAvailableTransfer(Transfer transfer) {
        availableTransfers.add(transfer);
    }
    public void setAvailableTransfers(List<Transfer> transferList) {
        availableTransfers = transferList;
    }

    public Interaction(Player player, Port currentPort) {
        this.player = player;
        this.currentPort = currentPort;
        getInteractions().put(player, this);
        GUIs.mainGUI(player);
    }
}
