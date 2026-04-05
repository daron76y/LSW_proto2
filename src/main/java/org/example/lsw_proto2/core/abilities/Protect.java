package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.BattleContext;
import org.example.lsw_proto2.core.HeroClass;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.core.effects.Effect;
import org.example.lsw_proto2.core.effects.FireShield;
import org.example.lsw_proto2.core.effects.Shield;

public class Protect extends Ability {
    private final int effectMultiplier; //for prophet upgrade

    private Protect() { super(0); this.effectMultiplier = 0; } //jackson

    public Protect(int manaCost, int effectMultiplier) {
        super(manaCost);
        this.effectMultiplier = effectMultiplier;
    }

    @Override
    public String getName() {return "Protect";}

    @Override
    public boolean requiresTarget() {return false;}

    @Override
    public void perform(BattleContext bc) {
        for (Unit ally : bc.getAllyParty().getAliveUnits()) {
            int shieldAmount = (int)(ally.getHealth() * 0.10) * effectMultiplier;

            Effect shieldEffect = (bc.getCaster().getMainClass() == HeroClass.HERETIC) ?
                    new FireShield(shieldAmount, 0.10) :
                    new Shield(shieldAmount);
            ally.addEffect(shieldEffect);

            bc.getOutput().showMessage(String.format("- %s gets %d %ss!", ally.getName(), shieldAmount, shieldEffect.getName()));
        }
    }
}