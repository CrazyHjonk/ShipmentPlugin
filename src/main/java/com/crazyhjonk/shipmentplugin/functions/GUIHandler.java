package com.crazyhjonk.shipmentplugin.functions;

import com.crazyhjonk.shipmentplugin.Interaction;
import com.crazyhjonk.shipmentplugin.Port;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GUIHandler {

    private static int guiType;
    private static String clickedItemName;
    private static Player player;
    private static int slot;

    public static void handleClick(int guiType, String clickedItemName, Player player, int slot) {

        GUIHandler.guiType = guiType;
        GUIHandler.clickedItemName = clickedItemName;
        GUIHandler.player = player;
        GUIHandler.slot = slot;

        /*  0: mainGUI
            1: Available Ports
            2: Sending
            3: Receiving
            4: Admin Settings
         */
        switch (guiType) {
            case 0 -> mainGUIHandler();
            case 1 -> availablePortsGUIHandler();
            case 2 -> sendingGUIHandler();
            case 3 -> receivingGUIHandler();
            case 4 -> adminSettingsGUIHandler();
            case 5 -> setAvailablePortsGUIHandler();
            case 6 -> pendingGUIHandler();
            case 7 -> sentTransfersGUIHandler();
            case 8 -> transferViewItemsGUIHandler();
        }
    }

    private static void mainGUIHandler() {
        switch (clickedItemName) {
            case "§3Outgoing Transfers" -> GUIs.sentTransfersGUI(player);
            case "§2Send Items" -> GUIs.availablePortsGUI(player);
            case "§2Receive Items" -> GUIs.receivingGUI(player);
            case "§3Admin Settings" -> AdminSettings.mainGUI(player);
        }
    }

    private static void availablePortsGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.mainGUI(player);
            return;
        }
        selectPort();
    }

    private static void sendingGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.availablePortsGUI(player);
            return;
        }
        initiateTransfer();
    }

    private static void receivingGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.mainGUI(player);
            return;
        }
        if (clickedItemName.equals("§4Receive Items")) {
            return;
        }
        Transfer transfer = Interaction.get(player).getAvailableTransfers().get(slot);
        if (transfer.isCompleted()) {
            transfer.receiveTransfer();
            GUIs.receivingGUI(player);
        }
    }

    private static void adminSettingsGUIHandler() {
        switch (clickedItemName) {
            case "§3Back" -> GUIs.mainGUI(player);
            case "§5Set Name" -> AdminSettings.setName(player);
            case "§5Set Available Ports" -> AdminSettings.setAvailablePorts(player);
        }
    }

    private static void setAvailablePortsGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            AdminSettings.mainGUI(player);
            return;
        }
        Port currentPort = Interaction.get(player).getCurrentPort();
        List<Port> shownPorts = new ArrayList<>(Port.getPorts());
        shownPorts.remove(currentPort);
        Port clickedPort = shownPorts.get(slot);
        if (currentPort.getAvailablePorts().contains(clickedPort))
            currentPort.removeAvailablePort(clickedPort);
        else currentPort.addAvailablePort(clickedPort);
        FileHandler.updatePortsFile();
        GUIs.setAvailablePortsGUI(player);
    }

    private static void pendingGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.sendingGUI(player);
            Interaction.get(player).setPendingConfirmation(false);
            return;
        }
        Interaction.get(player).getActiveTransfer().confirmTransfer();
    }

    private static void sentTransfersGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.mainGUI(player);
            return;
        }
        Transfer transfer = Interaction.get(player).getAvailableTransfers().get(slot);
        GUIs.transferViewItemsGUI(player, transfer);
    }

    private static void transferViewItemsGUIHandler() {
        if (clickedItemName.equals("§3Back")) {
            GUIs.mainGUI(player);
        }
    }
    private static void selectPort() {
        Interaction.get(player).setSelectedPort(Interaction.get(player).getCurrentPort().getAvailablePorts().get(slot));
        GUIs.sendingGUI(player);
    }

    private static void initiateTransfer() {
        new Transfer(player, slot - 2);
    }
}
