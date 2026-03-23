package org.example.lsw_proto2.pvp;

import org.example.lsw_proto2.battle.BattleEngine;
import org.example.lsw_proto2.core.InputService;
import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;

public class PvPMatchEngine implements PvPMatch {
    private final Party player1Party;
    private final Party player2party;
    private final InputService input;
    private final OutputService output;

    public PvPMatchEngine(Party player1Party, Party player2party, InputService input, OutputService output) {
        this.player1Party = player1Party;
        this.player2party = player2party;
        this.input = input;
        this.output = output;
    }

    @Override
    public Party startMatch() {
        BattleEngine battle = new BattleEngine(player1Party, player2party, input, input, output);
        Party winner = battle.runBattle();

        //fully restore both parties
        reviveParty(player1Party);
        reviveParty(player2party);

        return winner;
    }

    private void reviveParty(Party party) {
        for (Unit unit : party.getUnits()) {
            unit.setHealth(unit.getMaxHealth());
            unit.setMana(unit.getMaxMana());
        }
    }
}
