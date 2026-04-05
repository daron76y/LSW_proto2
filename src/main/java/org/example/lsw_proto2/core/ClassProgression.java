package org.example.lsw_proto2.core;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.*;
@JsonAutoDetect(
        fieldVisibility    = JsonAutoDetect.Visibility.ANY,
        getterVisibility   = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class ClassProgression {
    private final EnumMap<HeroClass, Integer> classLevels;
    private HeroClass mainClass;
    private int experience;

    /** no-arg for Jackson **/
    @JsonCreator
    ClassProgression() {
        this.classLevels = new EnumMap<>(HeroClass.class);
        this.mainClass = null;
        this.experience = 0;
    }

    ClassProgression(HeroClass startingClass) {
        classLevels = new EnumMap<>(HeroClass.class);
        classLevels.put(HeroClass.ORDER, 0);
        classLevels.put(HeroClass.CHAOS, 0);
        classLevels.put(HeroClass.WARRIOR, 0);
        classLevels.put(HeroClass.MAGE, 0);
        mainClass = startingClass;
        classLevels.put(mainClass, 1); //init starting class to level 1
        experience = 0;
    }

    // -----------------------------------------------------------------------
    // |                               Classes                               |
    // -----------------------------------------------------------------------
    public HeroClass getMainClass() {return mainClass;}
    public EnumMap<HeroClass, Integer> getClassLevels() {return classLevels;}

    public void changeMainClass(HeroClass heroClass) {
        if (mainClass == heroClass) throw new IllegalArgumentException("This unit is already a " + heroClass);
        if (mainClass.isHybrid()) throw new IllegalArgumentException("This unit is a permanent hybrid!");
        if (mainClass.isSpecialization() && !heroClass.isHybrid()) throw new IllegalArgumentException("Specialized units may only upgrade to hybrids!");
        if (heroClass.isSpecialization() && classLevels.get(heroClass.getParentA()) < 5) throw new IllegalArgumentException("This unit does not meet the minimum level to specialize into " + heroClass);
        if (heroClass.isHybrid() && (classLevels.get(heroClass.getParentA()) < 5 || classLevels.get(heroClass.getParentB()) < 5)) throw new  IllegalArgumentException("This unit does not meet he minimum levels to hybridize into " + heroClass);

        //unit may change class
        mainClass = heroClass;
    }

    public void levelUpClass(HeroClass heroClass) {
        if (!classLevels.containsKey(heroClass)) throw new IllegalArgumentException("This unit does not have this class");
        if (getLevel() >= 20) throw new IllegalArgumentException("This unit is at the max level: " + getLevel());
        if (experience < expNeededForLvl(classLevels.get(heroClass) + 1)) throw new IllegalArgumentException("Not enough experience!");

        //level up class
        experience -= expNeededForLvl(classLevels.get(heroClass) + 1);
        classLevels.put(heroClass, classLevels.get(heroClass) + 1);
    }

    public HeroClass handleClassTransformation() {
        List<HeroClass> lvl5Classes = classLevels.entrySet().stream()
                .filter(entry -> entry.getValue() >= 5)
                .map(Map.Entry::getKey)
                .toList();

        //transform into specialization if one class is lvl 5 and you're not already a specialization
        if (mainClass.isBase() && lvl5Classes.size() == 1) {
            HeroClass hc = lvl5Classes.getFirst();
            changeMainClass(HeroClass.comboOf(hc, hc));
            return mainClass;
        }

        //if two classes are lvl 5, transform into a hybrid of the two as long as you are currently a specialization
        else if (mainClass.isSpecialization() && lvl5Classes.size() == 2) {
            HeroClass parentA = lvl5Classes.get(0);
            HeroClass parentB = lvl5Classes.get(1);
            changeMainClass(HeroClass.comboOf(parentA, parentB));
            return mainClass;
        }

        //if above two don't work, then do nothing
        return null;
    }

    public List<HeroClass> getClassesAvailableForLevelUp() {
        List<HeroClass> classes = new ArrayList<>();
        for (HeroClass heroClass : classLevels.keySet())
            if (canLevelUpClass(heroClass)) classes.add(heroClass);
        return classes;
    }

    // -----------------------------------------------------------------------
    // |                           Levels & Exp                              |
    // -----------------------------------------------------------------------
    public int getLevel() {
        return classLevels.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public boolean canLevelUpClass(HeroClass heroClass) {
        if (!classLevels.containsKey(heroClass)) return false;
        if (getLevel() >= 20) return false;
        return experience >= expNeededForLvl(classLevels.get(heroClass) + 1);
    }

    public boolean canLevelUpAny() {
        for (HeroClass hc : classLevels.keySet()) {
            if (canLevelUpClass(hc)) return true;
        }
        return false;
    }

    public int getExperience() {return experience;}
    public void addExperience(int experience) {this.experience += experience;}
    public void loseExperience(int experience) {this.experience -= Math.min(experience, this.experience);}

    public int expNeededForLvl(int lvl) {
        if (lvl <= 0) return 0;
        return expNeededForLvl(lvl - 1) + 500 + 75 * lvl + 20 * lvl * lvl;
    }
}