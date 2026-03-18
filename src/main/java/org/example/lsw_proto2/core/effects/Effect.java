package org.example.lsw_proto2.core.effects;

import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;

public abstract class Effect {
    private int duration; //how many turns

    public Effect(int duration) {
        this.duration = duration;
    }

    public abstract String getName();
    public int getDuration() {return duration;}
    public void decrementDuration() {duration--;}
    public boolean isExpired() {return duration <= 0;}

    // optional overrides unique to each effect. Not all effects need to override/implement
    public void onAttack(Unit attacker, Party allyParty, Unit target, Party enemyParty, OutputService output) {}
    public int modifyDamage(Unit attacker, Unit target, int damage, OutputService output) {return damage;}
    public boolean preventsAction() {return false;}

    //to string
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
