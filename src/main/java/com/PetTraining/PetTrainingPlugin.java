package com.PetTraining;

import com.google.inject.Provides;
import com.google.gson.Gson;
import com.google.common.base.Strings;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.EnumMap;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.events.ConfigSync;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Pet Training",
	description = "Let your pets gain levels by watching you train."
)
public class PetTrainingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PetTrainingConfig config;

	@Inject
	private ClientToolbar clientToolbar;

	private PetTrainingPanel panel;
	private NavigationButton navButton;

	@Inject
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	@Getter
	private NPC currentPet;

	@Getter
	private PetLevels currentPetLevels;

	private final Map<Skill, Integer> playerXp = new EnumMap<>(Skill.class);

	@Override
	protected void startUp() throws Exception
	{
		panel = injector.getInstance(PetTrainingPanel.class);

		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/skill_icons/overall.png");

		navButton = NavigationButton.builder()
				.tooltip("Pet Skills")
				.icon(icon)
				.priority(2)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		panel = null;
		navButton = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			//petXp.replaceAll((k,v) -> 0);
			updateFollower();
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		saveCurrentPetStats();
	}

	@Subscribe
	public void onClientShutdown(ClientShutdown event)
	{
		saveCurrentPetStats();
	}

	@Subscribe
	public void onConfigSync(ConfigSync configSync)
	{
		saveCurrentPetStats();
	}

	@Provides
	PetTrainingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PetTrainingConfig.class);
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event) {
		updateFollower();
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned) {
		if (npcDespawned.getActor() == currentPet) {
			log.debug("Follower despawned");
			// save and then delete the current values
			removeCurrentPet();
		}

		updateFollower();
	}

	public void removeCurrentPet() {
		saveCurrentPetStats();
		this.currentPetLevels = null;
		this.currentPet = null;
	}

	public void updateFollower() {
		NPC follower = client.getFollower();

		if (follower != null && follower != currentPet){
			if (currentPet != null) {
				// some weird situation where our follower got replaced immediately
				// save it and remove it
				removeCurrentPet();
			}
			// now we are confident we have no current pet and can add the new follower which isn't null
			this.currentPet = follower;
			String name = currentPet.getName();
			this.currentPetLevels = getExistingLevelsElseNew(name);

			log.debug("no current pet; create new or pull existing");
			log.debug("XP: CRAFTING {} MINING {}", this.currentPetLevels.getXp(Skill.CRAFTING), this.currentPetLevels.getXp(Skill.MINING));
			log.debug("LEVEL: CRAFTING {} MINING {}", this.currentPetLevels.getLevel(Skill.CRAFTING), this.currentPetLevels.getLevel(Skill.MINING));

			panel.refresh();
		}
	}

	public PetLevels getExistingLevelsElseNew(String name) {
		// this will get the existing pet levels if there are any saved, else return a new PetLevels
		PetLevels existingLevels = loadPetStats(name);

		if (existingLevels == null) {
			log.debug("nonexistent create new");
			return new PetLevels(name);
		}

		log.debug("found existing config; load it");
		return existingLevels;
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		final Skill skill = statChanged.getSkill();
		final int currentXp = statChanged.getXp();

		log.debug("xp drop {} {}", skill, currentXp);

        playerXp.putIfAbsent(skill, currentXp);

		if (this.currentPet != null) {
			final int currentPetLevel = currentPetLevels.getLevel(skill);
			int deltaXp = currentXp - playerXp.get(skill);
			currentPetLevels.addXp(skill, deltaXp);

			log.debug("{} GAINED {} {} XP FOR NEW TOTAL {}", currentPet.getName(), deltaXp, skill, currentPetLevels.getXp(skill));

			int newLevel = currentPetLevels.getLevel(skill);

			for (int i = currentPetLevel + 1; i <= newLevel; i++) {
				showLevelUpMessage(skill, i);
			}
		}

		playerXp.put(skill, currentXp);
	}

	public void showLevelUpMessage(Skill skill, Integer newLevel) {
		String levelUpStr = "%s reached %s Level %d!";
		String msg = String.format(levelUpStr, currentPet.getName(), skill, newLevel);

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
	}

	public void saveCurrentPetStats() {
		if (currentPetLevels != null) {
			final String profile = configManager.getRSProfileKey();
			if (profile == null) {
				return;
			}

			if (Strings.isNullOrEmpty(profile)) {
				log.debug("Trying to save pet exp with no profile!");
				return;
			}

			String json = gson.toJson(currentPetLevels);
			configManager.setConfiguration(PetTrainingConfig.GROUP, profile, "levels_" + currentPetLevels.getName(), json);

		}
	}

	public PetLevels loadPetStats(String name) {
		String profile = configManager.getRSProfileKey();

		if (Strings.isNullOrEmpty(profile))
		{
			log.debug("Trying to get pet exp with no profile!");
			return null;
		}

		String json = configManager.getConfiguration(PetTrainingConfig.GROUP, profile, "levels_" + name);

		if (json == null)
		{
			return null;
		}

		return gson.fromJson(json, PetLevels.class);
	}

	public Integer getCurrentPetSkill(Skill skill) {
		if (currentPet != null) {
			return currentPetLevels.getLevel(skill);
		}
		return null;
	}
}
