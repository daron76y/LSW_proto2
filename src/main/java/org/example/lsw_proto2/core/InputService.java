package org.example.lsw_proto2.core;

import org.example.lsw_proto2.battle.BattleCommand;
import org.example.lsw_proto2.pve.PVECommand;

import java.util.List;

public interface InputService {
    BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty);

    PVECommand choosePVECommand();

    String chooseInnAction();

    Items chooseItem();

    Unit chooseUnit(List<Unit> recruits);
}
