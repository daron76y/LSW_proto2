package org.example.lsw_proto2.core;

import java.util.List;

public interface OutputService {
   void showMessage(String message);
   void showParty(List<Party> partyList);
   void announceTurn(Unit unit);
   void showUnitBasic(Unit unit);
   void showUnitAdvanced(Unit unit);
   void showInventory(Party playerParty);
   void showItemShop();
}
