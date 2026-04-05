package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.BattleContext;
import org.example.lsw_proto2.core.Unit;

public class Fireball extends Ability {
    private final int damageMultiplier;

    private Fireball() { super(0); this.damageMultiplier = 0; } //jackson

    public Fireball(int manaCost, int damageMultiplier) {
        super(manaCost);
        this.damageMultiplier = damageMultiplier; //double damage for sorcerer upgrade
    }

    @Override
    public String getName() {return "Fireball";}

    @Override
    public boolean requiresTarget() {return true;}

    @Override
    public void perform(BattleContext bc) {
        //get the neighboring enemies of the target (fireball is an AOE attack)
        int beforeIndex = bc.getEnemyParty().getAliveUnits().indexOf(bc.getTarget()) - 1;
        int afterIndex = bc.getEnemyParty().getAliveUnits().indexOf(bc.getTarget()) + 1;
        Unit beforeEnemy = (beforeIndex < 0) ? null : bc.getEnemyParty().getAliveUnits().get(beforeIndex);
        Unit afterEnemy = (afterIndex >= bc.getEnemyParty().getNumAliveUnits()) ? null : bc.getEnemyParty().getAliveUnits().get(afterIndex);

        //damage target and neighbors, if they exist
        int damage = bc.getCaster().getAttack() * damageMultiplier;
        int inflictedDamage = bc.getTarget().applyDamage(damage);
        bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, bc.getTarget().getName()));

        if (beforeEnemy != null) {
            inflictedDamage = beforeEnemy.applyDamage(damage);
            bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, beforeEnemy.getName()));
        }

        if (afterEnemy != null) {
            inflictedDamage = afterEnemy.applyDamage(damage);
            bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, afterEnemy.getName()));
        }
    }
}