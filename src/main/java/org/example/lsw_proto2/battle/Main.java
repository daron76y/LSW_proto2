package org.example.lsw_proto2.battle;

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
        partyA.addUnit(a1);
        partyA.addUnit(a2);
        partyA.addUnit(a3);
        partyA.addUnit(a4);

        Unit b1 = new Unit("Andros", 10, 1, 10, 100, HeroClass.ORDER);
        Unit b2 = new Unit("Lizarra", 10, 1, 10, 100, HeroClass.CHAOS);
        Unit b3 = new Unit("Latifs", 10, 1, 10, 100, HeroClass.WARRIOR);
        Unit b4 = new Unit("Studeo", 10, 1, 10, 100, HeroClass.MAGE);
        Party partyB =  new Party("Villains");
        partyB.addUnit(b1);
        partyB.addUnit(b2);
        partyB.addUnit(b3);
        partyB.addUnit(b4);

        ConsoleInputService playerInput = new ConsoleInputService();
        ConsoleOutputService output = new ConsoleOutputService();
        BattleEngine battle = new BattleEngine(partyA, partyB, playerInput, playerInput, output);

        Party winner = battle.runBattle();
        System.out.println("Battle Over! Winner: " + winner.getName());
    }
}
