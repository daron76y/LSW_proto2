package org.example.lsw_proto2.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.example.lsw_proto2.core.abilities.Ability;
import org.example.lsw_proto2.core.effects.Effect;

import java.util.*;

@JsonAutoDetect(
        fieldVisibility    = JsonAutoDetect.Visibility.ANY,
        getterVisibility   = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonPropertyOrder({
        "name", "progression", "attack", "defense", "maxHealth", "maxMana", "health", "mana", "abilities", "effects"
})
public class Unit {
    private final String name;

    private final ClassProgression progression;

    private final List<Effect> effects;
    private final List<Ability> abilities;

    private int maxHealth;
    private int maxMana;
    private int attack;
    private int defense;
    private int health;
    private int mana;

    /** No-arg constructor for Jackson deserialization. */
    private Unit() {
        this.name = null;
        this.progression = new ClassProgression();
        this.effects = new ArrayList<>();
        this.abilities = new ArrayList<>();
    }

    //Constructor
    public Unit(String name, int atk, int def, int maxHp, int maxMp, HeroClass startingClass) {
        this.name = name;

        progression = new ClassProgression(startingClass);

        effects = new ArrayList<>(startingClass.getEffects());
        abilities = new ArrayList<>(startingClass.getAbilities());

        this.attack = atk;
        this.defense = def;
        this.health = this.maxHealth = maxHp;
        this.mana = this.maxMana = maxMp;
    }

    public Unit(String name, HeroClass startingClass) {
        //default stats constructor
        this(name, 5, 5, 100, 50, startingClass);
    }

    // -----------------------------------------------------------------------
    // |                                Basic                                |
    // -----------------------------------------------------------------------
    public String getName() {return name;}

    public int getMaxHealth() {return maxHealth;}

    public void setMaxHealth(int maxHealth) {this.maxHealth = maxHealth;}

    public int getMaxMana() {return maxMana;}

    public void setMaxMana(int maxMana) {this.maxMana = maxMana;}

    public int getAttack() {return attack;}

    public void setAttack(int attack) {this.attack = attack;}

    public int getDefense() {return defense;}

    public void setDefense(int defence) {this.defense = defence;}

    public int getHealth() {return health;}

    public void setHealth(int health) {this.health = Math.min(Math.max(0, health), maxHealth);}

    public int getMana() {return mana;}

    public void setMana(int mana) {this.mana = Math.min(Math.max(0, mana), maxMana);}


    // -----------------------------------------------------------------------
    // |                              Effects                                |
    // -----------------------------------------------------------------------
    public List<Effect> getEffects() {return this.effects;}
    public void addEffect(Effect effect) {this.effects.add(effect);}
    public void removeEffect(Effect effect) {this.effects.remove(effect);}

    public void clearDebuffEffects() {
        effects.removeIf(effect -> !getMainClass().getEffects().contains(effect));
    }

    // -----------------------------------------------------------------------
    // |                             Abilities                               |
    // -----------------------------------------------------------------------
    public List<Ability> getAbilities() {return this.abilities;}
    public void addAbility(Ability ability) {this.abilities.add(ability);}
    public void removeAbility(Ability ability) {this.abilities.remove(ability);}

    public Ability getAbilityByName(String name) {
        return abilities.stream()
                .filter(ability -> ability.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    // -----------------------------------------------------------------------
    // |                               Battle                                |
    // -----------------------------------------------------------------------
    public boolean isAlive() {return health > 0;}
    public boolean isDead() {return health <= 0;}

    public int applyDamage(int damage) {
        //apply defense
        damage -= defense;

        //ensure damage is not negative
        damage = Math.max(0, damage);

        //inflict damage onto health
        health = Math.max(0, health - damage);

        //return damage dealt
        return damage;
    }

    // -----------------------------------------------------------------------
    // |                               Classes                               |
    // -----------------------------------------------------------------------
    public HeroClass getMainClass() {return progression.getMainClass();}
    public EnumMap<HeroClass, Integer> getClassLevels() {return progression.getClassLevels();}

    public void changeMainClass(HeroClass heroClass) {
        progression.changeMainClass(heroClass);
        abilities.clear();
        abilities.addAll(heroClass.getAbilities());
        effects.clear();
        effects.addAll(heroClass.getEffects());
    }

    public void levelUpClass(HeroClass heroClass) {
        progression.levelUpClass(heroClass); //validation + exp deduction

        //get stat bonuses
        attack += 1 + heroClass.getAttackPerLevel();
        defense += 1 + heroClass.getDefensePerLevel();
        health = maxHealth += 5 + heroClass.getHealthPerLevel();
        mana = maxMana += 2 + heroClass.getManaPerLevel();
    }

    public HeroClass handleClassTransformation() {
        HeroClass newClass = progression.handleClassTransformation();

        //sync abilities and effects with the new class
        if (newClass != null) {
            abilities.clear();
            abilities.addAll(newClass.getAbilities());
            effects.clear();
            effects.addAll(newClass.getEffects());
        }

        return newClass;
    }

    public List<HeroClass> getClassesAvailableForLevelUp() {return progression.getClassesAvailableForLevelUp();}

    // -----------------------------------------------------------------------
    // |                           Levels & Exp                              |
    // -----------------------------------------------------------------------
    public int getLevel() {return progression.getLevel();}

    public boolean canLevelUpClass(HeroClass heroClass) {return progression.canLevelUpClass(heroClass);}

    public boolean canLevelUpAny() {return progression.canLevelUpAny();}

    public int getExperience() {return progression.getExperience();}
    public void addExperience(int experience) {progression.addExperience(experience);}
    public void loseExperience(int experience) {progression.loseExperience(experience);}

    public int expNeededForLvl(int lvl) {return progression.expNeededForLvl(lvl);}


    // -----------------------------------------------------------------------
    // |                               Other                                 |
    // -----------------------------------------------------------------------
    @Override
    public String toString() {
        return String.format("[%s]\tatk: %d|def: %d|hp: %d|mp: %d|lvl: %d|xp: %d", name, attack, defense, health, mana, getLevel(), getExperience());
    }
}