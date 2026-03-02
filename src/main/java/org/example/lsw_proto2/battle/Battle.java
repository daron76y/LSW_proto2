package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.core.abilities.Ability;

public interface Battle {
    Party runBattle();
    void attack(Unit attacker, Unit target);
    void defend(Unit unit);
    void wait(Unit unit);
    void cast(Unit caster, Unit target, Party allyParty, Party enemyParty, Ability ability);
}
