package org.example.lsw_proto2.io;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.example.lsw_proto2.core.*;

import java.util.List;

/**
 * Implements OutputService by appending formatted text to a shared TextArea in the GUI.
 */
public class GUIOutputService implements OutputService {
    private final TextArea console; //the actual output console of our game

    public GUIOutputService(TextArea console) {
        this.console = console;
    }

    //instead of printing to System.out, we print to the console TextArea from the GameApp
    private void print(String line) {
        Platform.runLater(() -> {
            console.appendText(line + "\n");
        });
    }

    @Override
    public void showMessage(String message) {
        print(message);
    }

    @Override
    public void showParty(List<Party> partyList) {
        print("=====================================================");
        for (Party party : partyList) {
            print(party.toString());
        }
        print("=====================================================");
    }

    @Override
    public void announceTurn(Unit unit) {
        print("It is " + unit.getName() + "'s turn!");
    }

    @Override
    public void showUnitBasic(Unit unit) {
        print(unit.toString());
    }

    @Override
    public void showUnitAdvanced(Unit unit) {
        print(unit.toString());
        print("- classes: " + unit.getClassLevels());
        print("- abilities: " + unit.getAbilities());
        print("- effects: " + unit.getEffects());
    }

    @Override
    public void showInventory(Party playerParty) {
        print("===================== Inventory ======================");
        if (playerParty.getInventory().isEmpty()) {
            print("Empty inventory!");
        } else {
            playerParty.getInventory().forEach((item, qty) -> {
                if (qty > 0) print(item + " : " + qty);
            });
        }
        print("=====================================================");
    }

    @Override
    public void showItemShop() {
        print("==================== Item Shop ======================");
        for (Items item : Items.values()) {
            print(String.format("%s\tCost: %dg\tHP: %d\tMP: %d", item, item.getCost(), item.getHealthBoost(), item.getManaBoost()));
        }
        print("=====================================================");
    }
}