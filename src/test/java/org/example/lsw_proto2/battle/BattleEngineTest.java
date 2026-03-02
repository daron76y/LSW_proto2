package org.example.lsw_proto2.battle;

import org.example.lsw_proto2.core.*;
import org.example.lsw_proto2.core.abilities.*;
import org.example.lsw_proto2.core.effects.*;
import org.example.lsw_proto2.core.effects.Shield;
import org.example.lsw_proto2.io.AIInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleEngineTest {

    private Party partyA;
    private Party partyB;
    private DummyOutputService output;
    private BattleEngine engine;

    @BeforeEach
    void setup() {
        partyA = new Party("Heroes");
        partyB = new Party("Monsters");

        output = new DummyOutputService();
        engine = new BattleEngine(partyA, partyB, new AIInputService(), new AIInputService(), output);
    }

    // =========================
    // ATTACK TESTS
    // =========================

    @Test
    void attack_appliesCorrectDamage() {
        Unit attacker = new Unit("Hero", 25, 5, 50, 20, HeroClass.WARRIOR);
        Unit target = new Unit("Enemy", 10, 0, 40, 10, HeroClass.WARRIOR);

        partyA.addUnit(attacker);
        partyB.addUnit(target);

        engine.attack(attacker, target);

        // damage = attack - defense = 25 - 0 = 25
        assertEquals(15, target.getHealth()); // 40 - 25 = 15
        assertTrue(output.getMessages().get(0).contains("Hero attacked Enemy"));
    }

    @Test
    void attack_throwsIfTargetDead() {
        Unit attacker = new Unit("Hero", 10, 5, 50, 20, HeroClass.WARRIOR);
        Unit target = new Unit("Enemy", 10, 0, 0, 10, HeroClass.WARRIOR); // dead
        assertTrue(target.isDead());

        partyA.addUnit(attacker);
        partyB.addUnit(target);

        assertThrows(IllegalArgumentException.class, () -> engine.attack(attacker, target));
    }

    @Test
    void attack_withShieldEffect_reducesDamage() {
        Unit hero = new Unit("Hero", 30, 5, 50, 50, HeroClass.WARRIOR);
        Unit enemy = new Unit("Enemy", 0, 0, 50, 10, HeroClass.WARRIOR);
        enemy.addEffect(new Shield(10));
        partyA.addUnit(hero);
        partyB.addUnit(enemy);

        engine.attack(hero, enemy);

        // Damage: 30 - 10 shield = 20
        assertEquals(30, enemy.getHealth()); // 50 - 20 = 30
        assertTrue(output.getMessages().stream().anyMatch(msg -> msg.contains("shielded")));
    }

    // =========================
    // DEFEND TEST
    // =========================

    @Test
    void defend_increasesHpAndMana() {
        Unit unit = new Unit("Knight", 10, 5, 50, 20, HeroClass.WARRIOR);

        unit.setHealth(40);
        unit.setMana(10);

        engine.defend(unit);

        assertEquals(50, unit.getHealth()); // 40 + 10
        assertEquals(15, unit.getMana());  // 10 + 5
        assertTrue(output.getMessages().get(0).contains("Knight defends"));
    }

    // =========================
    // WAIT TEST
    // =========================

    @Test
    void wait_outputsCorrectMessage() {
        Unit unit = new Unit("Mage", 10, 5, 50, 20, HeroClass.MAGE);
        engine.wait(unit);

        assertTrue(output.getMessages().get(0).contains("Mage waits"));
    }

    // =========================
    // CAST TESTS
    // =========================

    @Test
    void cast_fireball_reducesManaAndDealsDamage() {
        Unit caster = new Unit("Caster", 10, 5, 50, 50, HeroClass.MAGE);
        Unit enemy1 = new Unit("Enemy1", 10, 0, 50, 10, HeroClass.WARRIOR);
        Unit enemy2 = new Unit("Enemy2", 10, 0, 50, 10, HeroClass.WARRIOR);

        partyA.addUnit(caster);
        partyB.addUnit(enemy1);
        partyB.addUnit(enemy2);

        Fireball fireball = new Fireball(20, 1); // damage multiplier 1
        caster.addAbility(fireball);

        engine.cast(caster, enemy1, partyA, partyB, fireball);

        assertEquals(30, caster.getMana()); // 50 - 20
        assertEquals(40, enemy1.getHealth()); // 50 - 50
        assertEquals(40, enemy2.getHealth()); // neighboring enemy also damaged
        assertTrue(output.getMessages().stream().anyMatch(msg -> msg.contains("casts Fireball")));
    }

    @Test
    void cast_throwsWhenNotEnoughMana() {
        Unit caster = new Unit("Caster", 10, 5, 50, 10, HeroClass.MAGE);
        Fireball fireball = new Fireball(20, 1);
        caster.addAbility(fireball);

        assertThrows(IllegalStateException.class, () -> engine.cast(caster, null, partyA, partyB, fireball));
    }

    @Test
    void cast_heal_restoresHpCorrectly() {
        Unit priest = new Unit("Priest", 5, 5, 100, 50, HeroClass.PRIEST);
        Unit ally1 = new Unit("Ally1", 10, 5, 100, 50, HeroClass.WARRIOR);
        Unit ally2 = new Unit("Ally2", 10, 5, 100, 50, HeroClass.WARRIOR);

        ally1.setHealth(50);
        ally2.setHealth(20);

        partyA.addUnit(priest);
        partyA.addUnit(ally1);
        partyA.addUnit(ally2);

        Heal heal = new Heal(10, 1);

        engine.cast(priest, null, partyA, partyB, heal);

        // All allies healed 25% max HP * 1
        assertEquals(75, ally1.getHealth());
        assertEquals(45, ally2.getHealth());
        assertTrue(output.getMessages().stream().anyMatch(msg -> msg.contains("heals")));
    }

    @Test
    void attack_withManaBurn_reducesTargetMana() {
        Unit attacker = new Unit("Hero", 10, 5, 100, 50, HeroClass.WARRIOR);
        Unit enemy = new Unit("Enemy", 10, 5, 100, 50, HeroClass.WARRIOR);

        attacker.addEffect(new ManaBurn());

        partyA.addUnit(attacker);
        partyB.addUnit(enemy);

        engine.attack(attacker, enemy);

        int expectedMana = 50 - (int)(50 * 0.10); // 10% of 50 = 5
        assertEquals(expectedMana, enemy.getMana());
        assertTrue(output.getMessages().stream().anyMatch(msg -> msg.contains("burns away")));
    }

    // =========================
    // Dummy OutputService for tests
    // =========================
    static class DummyOutputService implements OutputService {
        private final List<String> messages = new ArrayList<>();

        @Override
        public void showMessage(String message) { messages.add(message); }
        @Override
        public void showParty(List<Party> partyList) {}
        @Override
        public void announceTurn(Unit unit) {}
        @Override
        public void showUnitBasic(Unit unit) {}
        @Override
        public void showUnitAdvanced(Unit unit) {}
        @Override
        public void showInventory(Party playerParty) {}
        @Override
        public void showItemShop() {}

        public List<String> getMessages() { return messages; }
    }
}