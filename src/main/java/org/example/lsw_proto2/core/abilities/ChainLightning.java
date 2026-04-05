package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.BattleContext;
import org.example.lsw_proto2.core.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChainLightning extends Ability {
    private final double subsequentDamageMultiplier; //for invoker upgrade

    private ChainLightning() { super(0); this.subsequentDamageMultiplier = 0; } //jackson

    public ChainLightning(int manaCost, double subsequentDamageMultiplier) {
        super(manaCost);
        this.subsequentDamageMultiplier = subsequentDamageMultiplier;
    }

    @Override
    public String getName() {return "Chain Lightning";}

    @Override
    public boolean requiresTarget() {return true;}

    @Override
    public void perform(BattleContext bc) {
        //damage initial target
        int damage = bc.getCaster().getAttack();
        int inflictedDamage = bc.getTarget().applyDamage(damage);
        bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, bc.getTarget().getName()));

        //randomly damage all other enemies for a percentage of the previous damage
        List<Unit> enemies = new ArrayList<>(bc.getEnemyParty().getUnits()); //do not shuffle original list
        Collections.shuffle(enemies);
        for (Unit enemy : enemies) {
            if (enemy.equals(bc.getTarget())) continue;
            damage = (int)(damage * subsequentDamageMultiplier);
            inflictedDamage = enemy.applyDamage(damage);
            bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, enemy.getName()));
        }
    }
}