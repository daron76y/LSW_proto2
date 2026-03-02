package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.core.abilities.Ability;
import org.example.lsw_proto2.core.effects.Effect;
import org.example.lsw_proto2.core.InputService;

import java.util.*;

public class BattleEngine implements Battle {
    private final Queue<Unit> turnQueue;
    private final Party partyA;
    private final Party partyB;
    private final InputService partyAInput;
    private final InputService partyBInput;
    private final OutputService output;

    public BattleEngine(Party partyA, Party partyB, InputService partyAInput, InputService partyBInput, OutputService output) {
        this.partyA = partyA;
        this.partyB = partyB;
        this.partyAInput = partyAInput;
        this.partyBInput = partyBInput;
        this.output = output;
        turnQueue = new LinkedList<Unit>();
    }

    public Party runBattle() {
        //initialize the turn queue based on highest level unit
        List<Unit> allUnits = new ArrayList<>();
        allUnits.addAll(partyA.getUnits());
        allUnits.addAll(partyB.getUnits());
        allUnits.removeIf(Unit::isDead);
        allUnits.sort(Comparator.comparingInt(Unit::getLevel).reversed());
        turnQueue.addAll(allUnits);

        //battle loop
        while (!turnQueue.isEmpty() && !isBattleOver()) {
            //get the current unit if alive
            Unit currentUnit = turnQueue.poll();
            if (currentUnit == null || currentUnit.isDead()) continue;

            //check if any effects block the units turn
            if (currentUnit.getEffects().stream().anyMatch(Effect::preventsAction)) {
                output.showMessage(currentUnit.getName() + "'s turn has been cancelled!");
                turnQueue.add(currentUnit);
                continue;
            }

            //output all parties
            output.showParty(List.of(partyA, partyB));
            //announce the turn of the current unit
            output.announceTurn(currentUnit);

            //get allies and enemies of this unit
            Party allyParty = (partyA.getUnits().contains(currentUnit)) ? partyA : partyB;
            Party enemyParty = (allyParty ==  partyB) ? partyA : partyB;

            //get the input service for this unit
            InputService input = (allyParty == partyA) ? partyAInput : partyBInput;

            //listen for battle commands. Retry if they failed.
            while (true) {
                try {
                    output.showMessage("Actions:\n[attack <target>]\n[defend]\n[wait]\n[cast \"<ability name>\" |target|]");
                    BattleCommand command = input.chooseBattleCommand(currentUnit, allyParty, enemyParty);
                    command.execute(this);
                    break;
                } catch (Exception e) {
                    output.showMessage("Error: " + e.getMessage());
                }
            }

            //decrement duration of status effects, and remove them if expired
            for (Effect effect : currentUnit.getEffects())
                effect.decrementDuration();
            currentUnit.getEffects().removeIf(Effect::isExpired);

            //put unit at the end of the turn list
            if (currentUnit.isAlive()) turnQueue.add(currentUnit);
        }

        //clear all status effects
        for (Unit unit : partyA.getUnits()) unit.clearDebuffEffects();
        for (Unit unit : partyB.getUnits()) unit.clearDebuffEffects();

        //return winning party
        return (partyA.isDefeated()) ? partyB : partyA;
    }

    private boolean isBattleOver() {return partyA.isDefeated() || partyB.isDefeated();}

    //====================================================================================
    //=                                  BATTLE COMMANDS                                 =
    //====================================================================================

    public void attack(Unit attacker, Unit target) {
        if (target == null) throw new IllegalArgumentException("Invalid target!");
        if (target.isDead()) throw new IllegalArgumentException("Target is dead");

        //apply damage to the target, modifying it with whatever effects the target has
        int damage = attacker.getAttack();
        for (Effect effect : target.getEffects())
            damage = effect.modifyDamage(attacker, target, damage, output);
        target.getEffects().removeIf(Effect::isExpired);

        //apply the modified damage
        damage = target.applyDamage(damage);
        output.showMessage(String.format("%s attacked %s for %d!", attacker.getName(), target.getName(), damage));

        //apply on-attack effects from the attacker
        Party allyParty = (partyA.getUnits().contains(attacker)) ? partyA : partyB;
        Party enemyParty = (partyB.getUnits().contains(attacker)) ? partyB : partyA;
        for (Effect effect : attacker.getEffects())
            effect.onAttack(attacker, allyParty, target, enemyParty, output);
        attacker.getEffects().removeIf(Effect::isExpired);
    }

    public void defend(Unit unit) {
        unit.setHealth(unit.getHealth() + 10);
        unit.setMana(unit.getMana() + 5);
        output.showMessage(String.format("%s defends! +10hp +5mp", unit.getName()));
    }

    public void wait(Unit unit) {
        output.showMessage(String.format("%s waits! Skip turn!", unit.getName()));
    }

    public void cast(Unit caster, Unit target, Party allyParty, Party enemyParty, Ability ability) {
        if (caster.getMana() < ability.getManaCost()) throw new IllegalStateException("Not enough mana!");
        if (ability.requiresTarget() && target == null) throw new IllegalArgumentException("Invalid target!");
        if (ability.requiresTarget() && target.isDead()) throw new IllegalArgumentException("Target is dead!");

        caster.setMana(caster.getMana() - ability.getManaCost());
        ability.execute(caster, target, allyParty, enemyParty, output);
    }
}
