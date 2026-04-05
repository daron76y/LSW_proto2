package org.example.lsw_proto2.io;

import org.example.lsw_proto2.pve.*;

import java.util.HashMap;
import java.util.Map;

public class PVECommandRegistry {
    public static Map<String, PVECommand> commands = new HashMap<>();
    static {
        commands.put("next", new NextRoomCommand());
        commands.put("use item", new UseItemCommand());
        commands.put("view party", new ViewPartyCommand());
        commands.put("level up", new LevelUpCommand());
        commands.put("quit", new QuitCommand());
    }

    public static PVECommand getCommand(String commandName) {
        if (commands.containsKey(commandName))
            return commands.get(commandName);
        else throw new IllegalArgumentException("Unknown command: " + commandName);
    }

    public static void putCommand(String commandName, PVECommand command) {
        commands.put(commandName, command);
    }
}
