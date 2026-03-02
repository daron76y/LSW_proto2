package org.example.lsw_proto2.io;

import org.example.lsw_proto2.battle.BattleCommand;
import org.example.lsw_proto2.core.InputService;
import org.example.lsw_proto2.core.Items;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.pve.PVECommand;

import java.util.List;

public class AIInputService implements InputService {
    @Override
    public BattleCommand chooseBattleCommand(Unit unit, Party allyParty, Party enemyParty) {
        return null;
    }

    @Override
    public PVECommand choosePVECommand() {
        return null;
    }

    @Override
    public String chooseInnAction() {
        return "";
    }

    @Override
    public Items chooseItem() {
        return null;
    }

    @Override
    public Unit chooseUnit(List<Unit> recruits) {
        return null;
    }
}
