package org.example.lsw_proto2.core;

public class BattleContext {
    private final Unit caster;
    private final Unit target;
    private final Party allyParty;
    private final Party enemyParty;
    private final OutputService output;

    public BattleContext(Unit caster, Unit target, Party allyParty, Party enemyParty, OutputService output) {
        this.caster = caster;
        this.target = target;
        this.allyParty = allyParty;
        this.enemyParty = enemyParty;
        this.output = output;
    }

    public Unit getCaster() {return caster;}
    public Unit getTarget() {return target;}
    public Party getAllyParty() {return allyParty;}
    public Party getEnemyParty() {return enemyParty;}
    public OutputService getOutput() {return output;}
}