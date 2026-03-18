package org.example.lsw_proto2.io;

import org.example.lsw_proto2.battle.*;
import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.core.abilities.Ability;
import org.example.lsw_proto2.pve.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleInputService implements InputService {
    private final Scanner scan;

    public ConsoleInputService() {
        this.scan = new Scanner(System.in);
    }

    private List<String> parseInput(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) tokens.add(matcher.group(1)); //text with quotes
            else tokens.add(matcher.group(2));
        }

        return tokens;
    }

    @Override
    public BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty) {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;
            List<String> tokens = parseInput(input);

            switch (tokens.getFirst().toLowerCase()) {
                case "attack":
                    //ensure at least the attack and target name is present
                    if (tokens.size() < 2) throw new IllegalArgumentException("Usage: attack [target name]");

                    //get the target unit from the enemy party, and create the command
                    Unit attackTarget = enemyParty.getUnitByName(tokens.get(1));
                    return new AttackCommand(unit, attackTarget);

                case "defend":
                    return new DefendCommand(unit);

                case "wait":
                    return new WaitCommand(unit);

                case "cast":
                    //ensure at least the cast and ability keywords are present
                    if (tokens.size() < 2) throw new IllegalArgumentException("Usage: cast \"[ability name]\" [target name if required]");

                    //get the ability name and ensure the unit has it
                    String abilityName = tokens.get(1);
                    Ability ability = unit.getAbilityByName(abilityName);
                    if (ability == null) throw new IllegalArgumentException("No such ability for this unit: " + abilityName);

                    //get the target, if the ability requires it
                    Unit castTarget = null;
                    if (ability.requiresTarget()) {
                        if (tokens.size() < 3) throw new IllegalArgumentException("Ability requires a target. Usage: cast \"" + abilityName + "\" [target name]");
                        castTarget = enemyParty.getUnitByName(tokens.get(2));
                    }

                    //create the complete command
                    return new CastCommand(unit, castTarget, allyParty, enemyParty, ability);

                default:
                    throw new IllegalArgumentException("Unknown action: " + tokens.getFirst());
            }
        }
    }

    @Override
    public PVECommand choosePVECommand() {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;

            return switch (input.toLowerCase()) {
                case "next" -> new NextRoomCommand();
                case "use item" -> new UseItemCommand();
                case "view party" -> new ViewPartyCommand();
                case "quit" -> new QuitCommand();
                default -> throw new IllegalArgumentException("Unknown action: " + input);
            };
        }
    }

    @Override
    public String chooseInnAction() {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;

            return input;
        }
    }

    @Override
    public Items chooseItem() {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;

            for (Items item : Items.values())
                if (input.equalsIgnoreCase(item.toString())) return item;
            throw new IllegalArgumentException("Unknown item: " + input);
        }
    }

    @Override
    public Unit chooseUnit(List<Unit> units) {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;

            Unit unit = units.stream()
                    .filter(u -> u.getName().equalsIgnoreCase(input))
                    .findFirst()
                    .orElse(null);
            if (unit == null) throw new IllegalArgumentException("No such unit: " + input);
            return unit;
        }
    }

    @Override
    public HeroClass chooseHeroClass(List<HeroClass> heroClasses) {
        return null;
    }
}
