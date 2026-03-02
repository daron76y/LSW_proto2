package org.example.lsw_proto2.core.effects;

public class Stunned extends Effect {
    public Stunned(int duration) {
        super(duration);
    }

    @Override
    public String getName() {return "Stunned";}

    @Override
    public boolean preventsAction() {return true;}
}
