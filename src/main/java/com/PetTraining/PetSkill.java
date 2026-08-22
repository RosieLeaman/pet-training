/*
 * Copyright (c) 2017, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.PetTraining;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.Skill;

// this is a copy from hiscore plugin because we need the same skill icons
// but not the boss stuff
    @AllArgsConstructor
    @Getter
    public enum PetSkill
    {
        ATTACK("Attack", Skill.ATTACK, SpriteID.Staticons.ATTACK),
        DEFENCE("Defence", Skill.DEFENCE, SpriteID.Staticons.DEFENCE),
        STRENGTH("Strength", Skill.STRENGTH , SpriteID.Staticons.STRENGTH),
        HITPOINTS("Hitpoints", Skill.HITPOINTS , SpriteID.Staticons.HITPOINTS),
        RANGED("Ranged", Skill.RANGED , SpriteID.Staticons.RANGED),
        PRAYER("Prayer", Skill.PRAYER , SpriteID.Staticons.PRAYER),
        MAGIC("Magic", Skill.MAGIC , SpriteID.Staticons.MAGIC),
        COOKING("Cooking", Skill.COOKING , SpriteID.Staticons.COOKING),
        WOODCUTTING("Woodcutting", Skill.WOODCUTTING , SpriteID.Staticons.WOODCUTTING),
        FLETCHING("Fletching", Skill.FLETCHING , SpriteID.Staticons.FLETCHING),
        FISHING("Fishing", Skill.FISHING , SpriteID.Staticons.FISHING),
        FIREMAKING("Firemaking", Skill.FIREMAKING , SpriteID.Staticons.FIREMAKING),
        CRAFTING("Crafting", Skill.CRAFTING , SpriteID.Staticons.CRAFTING),
        SMITHING("Smithing", Skill.SMITHING , SpriteID.Staticons.SMITHING),
        MINING("Mining", Skill.MINING , SpriteID.Staticons.MINING),
        HERBLORE("Herblore", Skill.HERBLORE , SpriteID.Staticons.HERBLORE),
        AGILITY("Agility", Skill.AGILITY , SpriteID.Staticons.AGILITY),
        THIEVING("Thieving", Skill.THIEVING , SpriteID.Staticons.THIEVING),
        SLAYER("Slayer", Skill.SLAYER , SpriteID.Staticons2.SLAYER),
        FARMING("Farming", Skill.FARMING , SpriteID.Staticons2.FARMING),
        RUNECRAFT("Runecraft", Skill.RUNECRAFT , SpriteID.Staticons2.RUNECRAFT),
        HUNTER("Hunter", Skill.HUNTER , SpriteID.Staticons2.HUNTER),
        CONSTRUCTION("Construction", Skill.CONSTRUCTION , SpriteID.Staticons2.CONSTRUCTION),
        SAILING("Sailing", Skill.SAILING , SpriteID.Staticons2.SAILING)
        ;

        private final String name;
        private final Skill skill;
        private final int spriteId;

        PetSkill(String name, Skill skill)
        {
            this.name = name;
            this.skill = skill;
            this.spriteId = -1;
        }
    }

