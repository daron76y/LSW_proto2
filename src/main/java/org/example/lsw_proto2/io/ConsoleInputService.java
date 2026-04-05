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

    @Override
    public BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty) {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;
            List<String> tokens = parseInput(input);

            return switchBattleCommand(unit, allyParty, enemyParty, tokens);
        }
    }

    @Override
    public PVECommand choosePVECommand() {
        while (true) {
            System.out.print(">");
            String input = this.scan.nextLine().trim();
            if (input.isEmpty()) continue;

            try {
                return PVECommandRegistry.getCommand(input);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Unknown command: " + input + "\nValid: next | use item | view party | quit");
            }
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
