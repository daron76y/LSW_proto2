package org.example.lsw_proto2.io;

import org.example.lsw_proto2.battle.*;
import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.pve.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GUIInputService implements InputService {
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    //Called by GameApp when the player presses enters the text field
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
        List<String> tokens = parseTokens(input);

        try {
            switch (tokens.get(0).toLowerCase()) {
                case "attack": {
                    if (tokens.size() < 2) throw new IllegalArgumentException("Usage: attack <target>");
                    Unit target = enemyParty.getUnitByName(tokens.get(1));
                    return new AttackCommand(unit, target);
                }
                case "defend":
                    return new DefendCommand(unit);
                case "wait":
                    return new WaitCommand(unit);
                case "cast": {
                    if (tokens.size() < 2) throw new IllegalArgumentException("Usage: cast \"<ability>\" [target]");
                    String abilityName = tokens.get(1);
                    var ability = unit.getAbilityByName(abilityName);
                    if (ability == null) throw new IllegalArgumentException("No such ability: " + abilityName);
                    Unit target = null;
                    if (ability.requiresTarget()) {
                        if (tokens.size() < 3) throw new IllegalArgumentException("This ability requires a target.");
                        target = enemyParty.getUnitByName(tokens.get(2));
                    }
                    return new CastCommand(unit, target, allyParty, enemyParty, ability);
                }
                default:
                    throw new IllegalArgumentException("Unknown action: " + tokens.get(0));
            }
        } catch (Exception e) {
            // Print the error and loop - engine stays blocked waiting for valid input
            queue.add(""); // won't be used; we loop back to waitForLine ourselves
            // Re-throw so BattleEngine's retry loop catches it and prints via output
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public PVECommand choosePVECommand() {
        String input = waitForLine();
        return switch (input.toLowerCase()) {
            case "next"       -> new NextRoomCommand();
            case "use item"   -> new UseItemCommand();
            case "view party" -> new ViewPartyCommand();
            case "level up"   -> new LevelUpCommand();
            case "quit"       -> new QuitCommand();
            default           -> throw new RuntimeException("Unknown command: " + input + "\nValid: next | use item | view party | quit");
        };
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

    // -----------------------------------------------------------------------
    // Token parser - handles quoted strings with spaces, i.e, cast "Chain Lightning" Goblin
    // -----------------------------------------------------------------------
    private List<String> parseTokens(String input) {
        List<String> tokens = new java.util.ArrayList<>();
        var matcher = java.util.regex.Pattern
                .compile("\"([^\"]*)\"|(\\S+)")
                .matcher(input);
        while (matcher.find())
            tokens.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        return tokens;
    }
}