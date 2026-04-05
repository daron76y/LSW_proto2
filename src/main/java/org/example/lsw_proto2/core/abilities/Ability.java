package org.example.lsw_proto2.core.abilities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.lsw_proto2.core.BattleContext;
import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Fireball.class,        name = "Fireball"),
        @JsonSubTypes.Type(value = ChainLightning.class,  name = "ChainLightning"),
        @JsonSubTypes.Type(value = Heal.class,            name = "Heal"),
        @JsonSubTypes.Type(value = Protect.class,         name = "Protect"),
        @JsonSubTypes.Type(value = Replenish.class,       name = "Replenish"),
        @JsonSubTypes.Type(value = BerserkerAttack.class, name = "BerserkerAttack"),
})
@JsonAutoDetect(
        fieldVisibility    = JsonAutoDetect.Visibility.ANY,
        getterVisibility   = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public abstract class Ability {
    int manaCost;

    public Ability(int manaCost) {this.manaCost = manaCost;}
    public abstract String getName();
    public int getManaCost() {return this.manaCost;}
    public abstract boolean requiresTarget();

    //template method because all abilities share the same basic cast output
    public void execute(BattleContext bc) {
        bc.getOutput().showMessage(String.format("%s casts %s!", bc.getCaster().getName(), getName()));
        perform(bc);
    }

    //unique ability implementations
    public abstract void perform(BattleContext bc);

    //to string
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + manaCost + ")";
    }
}
