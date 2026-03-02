package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.HeroClass;
import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.core.effects.Effect;
import org.example.lsw_proto2.core.effects.FireShield;
import org.example.lsw_proto2.core.effects.Shield;

public class Protect extends Ability {
    private final int effectMultiplier; //for prophet upgrade

    public Protect(int manaCost, int effectMultiplier) {
        super(manaCost);
        this.effectMultiplier = effectMultiplier;
    }

    @Override
    public String getName() {return "Protect";}

    @Override
    public boolean requiresTarget() {return false;}

    @Override
    public void perform(Unit caster, Unit target, Party allyParty, Party enemyParty, OutputService output) {
        for (Unit ally : allyParty.getAliveUnits()) {
            int shieldAmount = (int)(ally.getHealth() * 0.10) * effectMultiplier;

            Effect shieldEffect = (caster.getMainClass() == HeroClass.HERETIC) ?
                    new FireShield(shieldAmount, 0.10) :
                    new Shield(shieldAmount);
            ally.addEffect(shieldEffect);

            output.showMessage(String.format("- %s gets %d %ss!", ally.getName(), shieldAmount, shieldEffect.getName()));
        }
    }
}
