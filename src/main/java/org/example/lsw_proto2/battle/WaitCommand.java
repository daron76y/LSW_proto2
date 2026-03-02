package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.Unit;

public class WaitCommand implements BattleCommand {
    private final Unit unit;

    public WaitCommand(Unit unit) {
        this.unit = unit;
    }

    @Override
    public void execute(Battle battle) {
        battle.wait(unit);
    }
}
