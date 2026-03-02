package org.example.lsw_proto2.core;

import java.util.List;

public interface UnitFactory {
    Party generateEnemyParty(int playerCumulativeLevel);
    List<Unit> generateHeroRecruits(int numRecruits);
}
