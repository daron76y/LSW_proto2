package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.Unit;

public class DefendCommand implements BattleCommand {
    private final Unit unit;

    public DefendCommand(Unit unit) {
        this.unit = unit;
    }

    @Override
    public void execute(Battle battle) {
        battle.defend(unit);
    }
}
