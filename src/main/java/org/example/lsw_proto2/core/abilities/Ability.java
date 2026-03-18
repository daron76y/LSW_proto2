package org.example.lsw_proto2.core.abilities;

import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;

public abstract class Ability {
    int manaCost;

    public Ability(int manaCost) {this.manaCost = manaCost;}
    public abstract String getName();
    public int getManaCost() {return this.manaCost;}
    public abstract boolean requiresTarget();

    //template method because all abilities share the same basic cast output
    public void execute(Unit caster, Unit target, Party allyParty, Party enemyParty, OutputService output) {
        output.showMessage(String.format("%s casts %s!", caster.getName(), getName()));
        perform(caster, target, allyParty, enemyParty, output);
    }

    //unique ability implementations
    public abstract void perform(Unit caster, Unit target, Party allyParty, Party enemyParty, OutputService output);

    //to string
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + manaCost + ")";
    }
}
