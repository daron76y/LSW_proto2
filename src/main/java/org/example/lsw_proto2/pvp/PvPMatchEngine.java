package org.example.lsw_proto2.pvp;

import org.example.lsw_proto2.battle.BattleEngine;
import org.example.lsw_proto2.core.InputService;
import org.example.lsw_proto2.core.OutputService;
import org.example.lsw_proto2.core.Party;

public class PvPMatchEngine {
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

    public void startMatch() {
        BattleEngine battle = new BattleEngine(player1Party, player2party, input, input, output);
        battle.runBattle();
    }
}
