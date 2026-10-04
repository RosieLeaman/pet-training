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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import net.runelite.api.Client;
import net.runelite.api.Skill;
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
        private final JLabel nameLabel;
        private final JPanel dropdownPanel;
        public GridBagConstraints constraints;

        private Boolean displayedIsFollower = false;
        private String displayName;
        private PetLevels displayedPetLevels;

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
    public PetTrainingPanel(SpriteManager spriteManager, PetTrainingPlugin plugin) {
        this.spriteManager = spriteManager;
        this.plugin = plugin;

        // Expand sub items to fit width of panel, align to top of panel
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.insets = new Insets(0, 0, 10, 0);

        this.constraints = c;

        // Panel with currently displayed pet's name
        JPanel namePanel = new JPanel();
        JLabel nameLabel = new JLabel();
        nameLabel.setText("No pet selected");

        namePanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        namePanel.setBorder(new EmptyBorder(2, 0, 2, 0));
        namePanel.add(nameLabel);
        this.nameLabel = nameLabel;
        add(namePanel, c);
        c.gridy++;

        // Panel that holds skill icons
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(8, 3));
        statsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        statsPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        // For each skill on the in-game skill panel, create a Label and add it to the UI
        //for (String skill : skills)
        for (PetSkill skill: SKILLS)
        {
            JPanel panel = makeSkillPanel(skill);
            statsPanel.add(panel);
        }

        add(statsPanel, c);
        c.gridy++;

        // Panel that holds the dropdown menu
        JPanel dropdownPanel = new JPanel();
        dropdownPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        dropdownPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.dropdownPanel = dropdownPanel;
        add(dropdownPanel, c);
    }

    private Integer getSkillLevel(PetSkill skill){
        Integer level = 0;

        if (displayedIsFollower) {
            level = plugin.getCurrentPetLevels().getLevel(skill.getSkill());
        }
        else if (displayedPetLevels != null) {
            level = displayedPetLevels.getLevel(skill.getSkill());
        }

        return level;
    }

    private JPanel makeSkillPanel(PetSkill skill) {
        JLabel label = new JLabel();
        label.setToolTipText(skill == null ? "Combat" : skill.getName());
        label.setFont(FontManager.getRunescapeSmallFont());
        label.setText("1");

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

    public void setDropdown() {
        dropdownPanel.removeAll(); // get rid of any existing if present


        // Drop down selection box to change the pet
        List<String> dropdownOptions = getDropdownOptions();

        if (dropdownOptions == null) {
            dropdownOptions = new ArrayList<>();
            dropdownOptions.add("No pets to select");
        }
        String[] dropdownArray = dropdownOptions.toArray(new String[0]);

        JComboBox<String> dropdown = new JComboBox<>(dropdownArray);

        this.dropdownPanel.add(dropdown);

        dropdown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                String selectedName = dropdown.getSelectedItem().toString();
                if (selectedName != null) {
                    changeDisplayedPet(selectedName);
                }
            }
        });

        add(dropdownPanel, this.constraints);

        dropdownPanel.repaint();
        dropdownPanel.revalidate();
    }


    public void refreshStatPanel(Boolean forceUpdate) {
        repaint();

        if (!forceUpdate) {
            if (displayName != null && !Objects.equals(plugin.getCurrentPetName(), displayName)) {
                return;
            }
        }

        if (displayName == null) {
            this.nameLabel.setText("No current follower");
        }
        else {
            this.nameLabel.setText(displayName);
        }

        // now set the skill values
        for (Map.Entry<PetSkill, JLabel> entry : skillLabels.entrySet()) {
            PetSkill skill = entry.getKey();
            JLabel label = entry.getValue();

            Integer actualLevel = getSkillLevel(skill);
            label.setText(actualLevel == null ? "--" : String.valueOf(actualLevel));

            if (displayedIsFollower) {
                label.setToolTipText(skillToolTip(skill, plugin.getCurrentPetLevels()));
            }
            else if (displayedPetLevels != null) {
                label.setToolTipText(skillToolTip(skill, displayedPetLevels));
            }
            else {
                label.setToolTipText("");
            }
        }

    }

    private String skillToolTip(PetSkill petSkill, PetLevels levels){
        String openingTags = "<html><body style = 'padding: 5px;color:#989898'>";
        String closingTags = "</html><body>";

        String content = "";

        Skill skill = petSkill.getSkill();
        Integer currXp = levels.getXp(skill);
        Integer nextLevel = levels.getExpNextLevel(skill);
        Integer remaining = levels.getRemainingExpToNextLevel(skill);

        content += "<p><span style = 'color:white'>" + skill.getName() + " XP:</span> " + currXp + "</p>";
        content += "<p><span style = 'color:white'>Next level at:</span> " + nextLevel + "</p>";
        content += "<p><span style = 'color:white'>Remaining XP:</span> " + remaining + "</p>";

        return openingTags + content + closingTags;
    }

    private List<String> getDropdownOptions(){
        List<String> availablePets = plugin.getAllSavedPets();

        if (availablePets == null) {
            return null;
        }
        Collections.sort(availablePets);

        return availablePets;
    }

    public void resetDisplayedPet(String name, PetLevels currentPetLevels) {
        displayedIsFollower = false;
        displayName = name;
        displayedPetLevels = currentPetLevels;
    }

    public void changeDisplayedPet(String selectedName){
        // conditions where nothing changes
        if (Objects.equals(selectedName, "No pets to select")) {
            return;
        }

        displayName = selectedName;

        if (Objects.equals(plugin.getCurrentPetName(), displayName)) {
            displayedIsFollower = true;
            displayedPetLevels = null;
        }
        else {
            displayedIsFollower = false;
            displayedPetLevels = plugin.loadPetStats(selectedName);
        }

        refreshStatPanel(true);
        }

    }

