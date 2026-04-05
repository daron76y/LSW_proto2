package org.example.lsw_proto2.io;

import org.example.lsw_proto2.battle.*;
import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.pve.PVECommand;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple AI-based decision-making implementation of InputService for PVE enemies
 */
public class AIInputService implements BattleInputService {
    @Override
    public BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty) {
        try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

        if (unit.getHealth() < unit.getMaxHealth() * 0.25 && 1 == ThreadLocalRandom.current().nextInt(1, 5)) { //defend
            return new DefendCommand(unit);
        }
        else if (1 == ThreadLocalRandom.current().nextInt(1, 5)) { //wait
            return new WaitCommand(unit);
        }
        else { //attack
            Unit target = enemyParty.getAliveUnits().stream()
                    .min(Comparator.comparingInt(Unit::getHealth))
                    .orElse(null);
            return new AttackCommand(unit, target);
        }
    }
}
