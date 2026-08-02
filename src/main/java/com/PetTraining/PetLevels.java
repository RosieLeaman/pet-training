package com.PetTraining;

import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Experience;
import net.runelite.api.Skill;

import java.util.EnumMap;
import java.util.Map;

public class PetLevels {

    private final Map<Skill, Integer> xp = new EnumMap<>(Skill.class);
    private final Map<Skill, Integer> levels = new EnumMap<>(Skill.class);

    @Getter
    private final String name;

    public PetLevels (String str) {
        name = str;

        for(Skill c : Skill.values())
            xp.put(c, 0);

        calcAllLevels();
    }

    public Integer getXp(Skill skill) {
        return xp.get(skill);
    }

    public Integer getLevel(Skill skill) {
        return levels.get(skill);
    }

    public void calcAllLevels() {
        for(Skill c : Skill.values())
            levels.put(c, Experience.getLevelForXp(xp.get(c)));
    }

    public void addXp (Skill skill, Integer amount) {
        if (xp.get(skill) == null) {
            xp.put(skill, amount);
        }
        else {
            xp.put(skill, xp.get(skill) + amount);

            // don't add exp over max xp
            if (xp.get(skill) > Experience.MAX_SKILL_XP) {
                xp.put(skill, Experience.MAX_SKILL_XP);
            }
        }

        levels.put(skill, Experience.getLevelForXp(xp.get(skill)));
    }

}
