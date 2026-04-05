package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.BattleContext;
import org.example.lsw_proto2.core.Unit;

public class Replenish extends Ability {
    private final int effectMultiplier; //for prophet upgrade

    private Replenish() { super(0); this.effectMultiplier = 0; } //jackson

    public Replenish(int manaCost, int effectMultiplier) {
        super(manaCost);
        this.effectMultiplier = effectMultiplier;
    }

    @Override
    public String getName() {return "Replenish";}

    @Override
    public boolean requiresTarget() {return false;}

    @Override
    public void perform(BattleContext bc) {
        bc.getCaster().setMana(bc.getCaster().getMana() + 60 * effectMultiplier);
        bc.getOutput().showMessage(String.format("- %s gets 60 mana!", bc.getCaster().getName()));
        for (Unit ally : bc.getAllyParty().getAliveUnits()) {
            if (ally.equals(bc.getCaster())) continue;
            ally.setMana(ally.getMana() + 30 * effectMultiplier);
            bc.getOutput().showMessage(String.format("- %s gets 30 mana!", ally.getName()));
        }
    }
}