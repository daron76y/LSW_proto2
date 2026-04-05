package org.example.lsw_proto2.core;

import org.example.lsw_proto2.battle.*;
import org.example.lsw_proto2.core.abilities.Ability;

import java.util.List;

public interface BattleInputService {
    BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty);

    default BattleCommand switchBattleCommand(Unit unit, Party allyParty, Party enemyParty, List<String> tokens) {
        switch (tokens.getFirst().toLowerCase()) {
            case "attack":
                if (tokens.size() < 2) throw new IllegalArgumentException("Usage: attack <target>");
                Unit attackTarget = enemyParty.getUnitByName(tokens.get(1));
                return new AttackCommand(unit, attackTarget);

            case "defend":
                return new DefendCommand(unit);

            case "wait":
                return new WaitCommand(unit);

            case "cast":
                if (tokens.size() < 2) throw new IllegalArgumentException("Usage: cast \"<ability>\" [target]");
                String abilityName = tokens.get(1);
                Ability ability = unit.getAbilityByName(abilityName);
                if (ability == null) throw new IllegalArgumentException("No such ability: " + abilityName);

                Unit castTarget = null;
                if (ability.requiresTarget()) {
                    if (tokens.size() < 3) throw new IllegalArgumentException("This ability requires a target.");
                    castTarget = enemyParty.getUnitByName(tokens.get(2));
                }
                return new CastCommand(unit, castTarget, allyParty, enemyParty, ability);

            default:
                throw new IllegalArgumentException("Unknown action: " + tokens.getFirst());
        }
    }
}
