/*
 * Copyright (c) TO UPDATE!!!
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

import com.google.inject.Inject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.runelite.api.Client;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.SpriteManager;

import static com.PetTraining.PetSkill.*;

public class PetTrainingPanel extends PluginPanel {
        @Inject
        private Client client;

        @Inject
        private EventBus eventBus;

        private final SpriteManager spriteManager;
        private final PetTrainingPlugin plugin;
        private final PetTrainingConfig config;

        private final PetSkill[] SKILLS = {
                ATTACK, HITPOINTS, MINING,
                STRENGTH, AGILITY, SMITHING,
                DEFENCE, HERBLORE, FISHING,
                RANGED, THIEVING, COOKING,
                PRAYER, CRAFTING, FIREMAKING,
                MAGIC, FLETCHING, WOODCUTTING,
                RUNECRAFT, SLAYER, FARMING,
                CONSTRUCTION, HUNTER, SAILING
        };

        private final Map<PetSkill, JLabel> skillLabels = new HashMap<>();

        private String selectedPet = null;

        void init()
        {
            setLayout(new BorderLayout());
            setBackground(ColorScheme.DARK_GRAY_COLOR);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            JPanel versionPanel = new JPanel();
            versionPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            versionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            versionPanel.setLayout(new GridLayout(0, 1));

            final Font smallFont = FontManager.getRunescapeSmallFont();

            JLabel revision = new JLabel();
            revision.setFont(smallFont);

        }


    @Inject
    public PetTrainingPanel(PetTrainingConfig config, SpriteManager spriteManager, PetTrainingPlugin plugin) {
        this.spriteManager = spriteManager;
        this.config = config;
        this.plugin = plugin;

        // Expand sub items to fit width of panel, align to top of panel
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        // Panel with currently displayed pet's name
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel();
        nameLabel.setText("PET NAME");
        namePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        namePanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        namePanel.add(nameLabel);
        add(namePanel, c);
        c.gridy++;

        // Panel that holds skill icons
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(8, 3));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // For each skill on the ingame skill panel, create a Label and add it to the UI
        //for (String skill : skills)
        for (PetSkill skill: SKILLS)
        {
            JPanel panel = makeSkillPanel(skill, 1);
            statsPanel.add(panel);
        }

        add(statsPanel, c);

    }

    private JPanel makeSkillPanel(PetSkill skill, int lvl) {
        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText(String.valueOf(lvl));
        Integer actualLevel = plugin.getCurrentPetSkill(skill.getSkill());
        label.setText(actualLevel == null ? "--" : String.valueOf(actualLevel));

        spriteManager.getSpriteAsync(skill == null ? SpriteID.SideIcons.COMBAT : skill.getSpriteId(), 0, (sprite) ->
                SwingUtilities.invokeLater(() ->
                {
                    // Icons are all 25x25 or smaller, so they're fit into a 25x25 canvas to give them a consistent size for
                    // better alignment. Further, they are then scaled down to 20x20 to not be overly large in the panel.
                    final BufferedImage scaledSprite = ImageUtil.resizeImage(ImageUtil.resizeCanvas(sprite, 25, 25), 20, 20);
                    label.setIcon(new ImageIcon(scaledSprite));
                }));

        label.setIconTextGap(4);

        JPanel skillPanel = new JPanel();
        skillPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        skillPanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        skillLabels.put(skill, label);
        skillPanel.add(label);

        return skillPanel;
    }

    public void refresh() {
        repaint();

        if (plugin.getCurrentPet() == null) {
            return;
        }

        for (Map.Entry<PetSkill, JLabel> entry : skillLabels.entrySet()) {
            PetSkill skill = entry.getKey();
            JLabel label = entry.getValue();

            Integer actualLevel = plugin.getCurrentPetSkill(skill.getSkill());
            label.setText(actualLevel == null ? "--" : String.valueOf(actualLevel));
            }
        }
    }
