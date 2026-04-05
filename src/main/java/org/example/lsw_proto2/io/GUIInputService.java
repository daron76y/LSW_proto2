package org.example.lsw_proto2.io;

import org.example.lsw_proto2.battle.*;
import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.core.abilities.Ability;
import org.example.lsw_proto2.pve.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * JavaFX based implementation of the InputService. Logic is mostly the same as ConsoleInputService
 * With some extra methods to enforce the blocking logic using a BlockingQueue
 */
public class GUIInputService implements InputService {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    //called by GameApp when the player presses enters the text field
    public void submitInput(String line) {
        queue.add(line.trim());
    }

    //Blocks until the player submits a non-empty line
    private String waitForLine() {
        try {
            String line;
            do {
                line = queue.take();
            } while (line.isEmpty());
            return line;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Input interrupted", e);
        }
    }

    // -----------------------------------------------------------------------
    // |                         InputService methods                        |
    // -----------------------------------------------------------------------
    @Override
    public BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty) {
        String input = waitForLine();
        List<String> tokens = parseInput(input);

        try {
            return switchBattleCommand(unit, allyParty, enemyParty, tokens);
        } catch (Exception e) {
            //print the error and loop - engine stays blocked waiting for future valid input
            queue.add(""); //won't be used cuz we loop back to waitForLine ourselves
            //re-throw so BattleEngine's retry loop catches it and prints via output
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PVECommand choosePVECommand() {
        String input = waitForLine();
        try {
            return PVECommandRegistry.getCommand(input);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unknown command: " + input + "\nValid: next | use item | view party | quit");
        }
    }

    @Override
    public String chooseInnAction() {
        return waitForLine();
    }

    @Override
    public Items chooseItem() {
        String input = waitForLine();
        for (Items item : Items.values()) {
            if (input.equalsIgnoreCase(item.toString())) return item;
        }
        throw new RuntimeException("Unknown item: " + input);
    }

    @Override
    public Unit chooseUnit(List<Unit> units) {
        String input = waitForLine();
        return units.stream()
                .filter(u -> u.getName().equalsIgnoreCase(input))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No such unit: " + input));
    }

    @Override
    public HeroClass chooseHeroClass(List<HeroClass> heroClasses) {
        String input = waitForLine();
        for (HeroClass heroClass : heroClasses) {
            if (input.equalsIgnoreCase(heroClass.toString())) return heroClass;
        }
        throw new RuntimeException("Invalid hero class: " + input);
    }
}