package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.core.effects.Stunned;

public class BerserkerAttack extends Ability {
    private BerserkerAttack() { super(0); } //jackson
    public BerserkerAttack(int manaCost) {super(manaCost);}

    @Override
    public String getName() {return "Berserker Attack";}

    @Override
    public boolean requiresTarget() {return true;}

    @Override
    public void perform(BattleContext bc) {
        //paladin upgrade
        if (bc.getCaster().getMainClass() == HeroClass.PALADIN) {
            int healAmount = (int)(bc.getCaster().getHealth() * 0.10);
            bc.getCaster().setHealth(bc.getCaster().getHealth() + healAmount);
            bc.getOutput().showMessage(String.format("- %s heals %d HP!", bc.getCaster().getName(), healAmount));
        }

        //attack the initial target
        int damage = bc.getCaster().getAttack();
        int inflictedDamage = bc.getTarget().applyDamage(damage);
        bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, bc.getTarget().getName()));
        if (stun(bc.getCaster(), bc.getTarget())) bc.getOutput().showMessage(String.format("- %s stunned!", bc.getTarget().getName()));

        // damage 2 more units for 25% of original dmg
        damage = (int)(damage * 0.25);
        int count = 0;
        for (Unit enemy : bc.getEnemyParty().getAliveUnits()) {
            if (count >= 2) break;
            if (enemy.equals(bc.getTarget())) continue;
            inflictedDamage = enemy.applyDamage(damage);
            count++;
            bc.getOutput().showMessage(String.format("- inflicts %d damage to %s", inflictedDamage, enemy.getName()));
            if (stun(bc.getCaster(), enemy)) bc.getOutput().showMessage(String.format("- %s stunned!", enemy.getName()));
        }
    }

    //knight upgrade
    private boolean stun(Unit caster, Unit target) {
        if (caster.getMainClass() == HeroClass.KNIGHT && Math.random() < 0.5) {
            target.addEffect(new Stunned(1));
            return true;
        }
        return false;
    }
}
