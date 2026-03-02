package org.example.lsw_proto2.pve;

import org.example.lsw_proto2.battle.BattleEngine;
import org.example.lsw_proto2.core.*;

import java.util.List;
import java.util.Random;

public class PVECampaignEngine implements PVECampaign {
    private final Party playerParty;
    private final InputService input;
    private final OutputService output;

    private final int totalRooms = 30;
    private enum RoomTypes {BATTLE, INN}
    private final RoomTypes[] rooms = new RoomTypes[totalRooms + 1];
    private int currentRoom;
    private int lastInnCheckpoint = 0;
    private UnitFactory unitFactory = new UnitFactoryCSV();

    public PVECampaignEngine(Party playerParty, InputService input, OutputService output, int currentRoom) {
        this.playerParty = playerParty;
        this.input = input;
        this.output = output;
        this.currentRoom = currentRoom;
    }

    public void startCampaign() {
        output.showMessage("Starting PvE campaign with party: " + playerParty.getName());
        while (currentRoom < totalRooms) {
            output.showMessage("Current room: " + currentRoom);
            try {
                output.showMessage("Actions: [next] [use item] [view party] [quit]");
                PVECommand command = input.choosePVECommand();
                command.execute(this);
            } catch (Exception e) {
                output.showMessage(e.getMessage());
            }
        }
        output.showMessage("Campaign Finished!");
    }

    public void nextRoom() {
        output.showMessage("Entering the next room...");
        currentRoom++;

        //if this room has never been visited/discovered before
        if (rooms[currentRoom] == null) {
            //calculate probabilities
            int cumulativeLevel = playerParty.getCumulativeLevels();
            int probabilityShift = cumulativeLevel / 10 * 3;

            //determine the type of this room
            if (Math.random() * 100 <= 60 + probabilityShift)
                rooms[currentRoom] = RoomTypes.BATTLE;
            else
                rooms[currentRoom] = RoomTypes.INN;
        }

        //if this room has already been discovered (as a result of dying and going back to an Inn)
        if ((rooms[currentRoom] == RoomTypes.BATTLE)) enterBattle();
        else enterInn();
    }

    private void enterBattle() {
        output.showMessage("Entering a battle...");
        Party enemyParty = unitFactory.generateEnemyParty(playerParty.getCumulativeLevels());
        BattleEngine battle = new BattleEngine(playerParty, enemyParty, input, input, output); //TODO Ai input
        Party winner = battle.runBattle();

        if (winner == playerParty) { //player is victorious
            output.showMessage("Victory!");
            int totalExperienceEarned = 0;
            int totalGoldEarned = 0;

            // get exp and gold per enemy unit defeated
            for (Unit enemy : enemyParty.getUnits()) {
                totalExperienceEarned += 50 * enemy.getLevel();
                totalGoldEarned += 75 * enemy.getLevel();
            }

            // divide exp amongst all alive members
            int numStandingUnits = playerParty.getAliveUnits().size();
            for (Unit standingUnit : playerParty.getAliveUnits()) {
                standingUnit.gainExperience(totalExperienceEarned / numStandingUnits);
            }

            // gain gold for the party
            playerParty.setGold(playerParty.getGold() + totalGoldEarned);
            output.showMessage("+ " + totalGoldEarned + " Gold Earned!");
            output.showMessage("+ " +  totalExperienceEarned + " Total Experience Earned!");
        }
        else { //player died
            output.showMessage("Defeat!");
            output.showMessage("Returning to previous inn!");
            currentRoom = lastInnCheckpoint;
            enterInn();
        }
    }

    private void enterInn() {
        output.showMessage("You arrived at an inn! Healing all heroes and restoring mana.");
        lastInnCheckpoint = currentRoom;

        //restore hero stats
        for (Unit u : playerParty.getUnits()) {
            u.setHealth(u.getMaxHealth());
            u.setMana(u.getMaxMana());
        }

        //Inn loop
        while (true) {
            try {
                output.showMessage("Actions: [buy] [recruit] [view party] [leave]");
                String choice = input.chooseInnAction();
                switch (choice.toLowerCase().trim()) {
                    case "buy":
                        output.showItemShop();
                        Items item = input.chooseItem();
                        if (playerParty.getGold() >= item.getCost()) {
                            playerParty.setGold(playerParty.getGold() - item.getCost());
                            playerParty.addItem(item);
                            output.showMessage("Purchased " + item);
                        }
                        else {
                            output.showMessage("Not enough gold for a " + item);
                        }
                        break;
                    case "recruit":
                        if (playerParty.getUnits().size() >= 5) {
                            output.showMessage("Party is full. Cannot recruit more heroes.");
                            continue;
                        }

                        //generate 1-5 random hero recruits
                        int numRecruits = new Random().nextInt(5) + 1;
                        List<Unit> recruits = unitFactory.generateHeroRecruits(numRecruits);
                        output.showMessage("Available Recruits:");
                        for (int i=0; i<recruits.size(); i++) {
                            Unit recruit = recruits.get(i);
                            int cost = recruit.getLevel() == 1 ? 0 : recruit.getLevel() * 200;
                            output.showMessage((i+1) + ". " + recruit.getName() + " - lvl: " + recruit.getLevel() + " - cost: " + cost);
                        }

                        //listen for player selection
                        output.showMessage("Your gold: " + playerParty.getGold());
                        output.showMessage("Who will you recruit?");
                        Unit selection = input.chooseUnit(recruits);
                        int cost = selection.getLevel() == 1 ? 0 : selection.getLevel() * 200;
                        if (playerParty.getGold() >= cost) {
                            playerParty.setGold(playerParty.getGold() - cost);
                            playerParty.addUnit(selection);
                            output.showMessage("Recruited " + selection.getName());
                        }
                        else {
                            output.showMessage("Not enough gold for " + selection.getName());
                        }
                        break;
                    case "view party":
                        viewParty();
                        break;
                    case "leave":
                        output.showMessage("Leaving Inn...");
                        return;
                }
            } catch (Exception e) {
                output.showMessage("Inn Error: " + e.getMessage());
            }
        }
    }

    public void quit() {
        //TODO:
    }

    public void useItem() {
        output.showInventory(playerParty);

        //get and validate the item
        output.showMessage("Enter the item you want to use:");
        Items item = input.chooseItem();
        if (!playerParty.getInventory().containsKey(item) || playerParty.getInventory().get(item) <= 0) {
            output.showMessage("You do not have this item.");
            return;
        }

        //get and validate the unit
        output.showMessage("Enter the unit you want to use it on");
        Unit unit = input.chooseUnit(playerParty.getUnits());
        if (unit.isDead() && item != Items.ELIXIR) {
            output.showMessage("Must revive this unit with an ELIXIR first!");
            return;
        }

        //use the item on the unit
        playerParty.removeItem(item);
        unit.setHealth(unit.getHealth() + item.getHealthBoost());
        unit.setMana(unit.getMana() + item.getManaBoost());
        output.showMessage(String.format("%s consumed %s!", unit.getName(), item));
    }

    public void viewParty() {
        output.showParty(List.of(playerParty));
        output.showInventory(playerParty);
        output.showMessage("Party gold: " + playerParty.getGold());
    }
}

