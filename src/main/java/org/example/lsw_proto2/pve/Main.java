package org.example.lsw_proto2.pve;

import org.example.lsw_proto2.core.HeroClass;
import org.example.lsw_proto2.core.Party;
import org.example.lsw_proto2.core.Unit;
import org.example.lsw_proto2.io.ConsoleInputService;
import org.example.lsw_proto2.io.ConsoleOutputService;

public class Main {
    public static void main(String[] args) {
        Unit a1 = new Unit("GNS", 10, 1, 10, 100, HeroClass.WARLOCK);
        Unit a2 = new Unit("Navo", 10, 1, 10, 100, HeroClass.HERETIC);
        Unit a3 = new Unit("Aella", 10, 1, 10, 100, HeroClass.PALADIN);
        Unit a4 = new Unit("Burle", 10, 1, 10, 100, HeroClass.KNIGHT);
        Party partyA =  new Party("Guardians");
        partyA.setGold(500);
        partyA.addUnit(a1);
        partyA.addUnit(a2);
        partyA.addUnit(a3);
        partyA.addUnit(a4);

        ConsoleInputService playerInput = new ConsoleInputService();
        ConsoleOutputService output = new ConsoleOutputService();

        PVECampaignEngine campaign = new PVECampaignEngine(partyA, playerInput, output, 0);
        campaign.startCampaign();
    }
}
