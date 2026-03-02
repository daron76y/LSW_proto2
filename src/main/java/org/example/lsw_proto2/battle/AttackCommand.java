package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.Unit;

public class AttackCommand implements BattleCommand {
    private final Unit attacker;
    private final Unit target;

    public AttackCommand(Unit attacker, Unit target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute(Battle battle) {
        battle.attack(attacker, target);
    }
}
