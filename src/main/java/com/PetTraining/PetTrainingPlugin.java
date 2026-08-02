package com.PetTraining;

import com.google.inject.Provides;
import com.google.gson.Gson;
import com.google.common.base.Strings;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.EnumMap;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.Experience;
import net.runelite.api.events.StatChanged;
import net.runelite.api.Skill;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ClientShutdown;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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
	private ConfigManager configManager;

	@Inject
	private Gson gson;

	private NPC currentPet;
	private PetLevels currentPetLevels;

	private final Map<Skill, Integer> playerXp = new EnumMap<>(Skill.class);

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Example stopped!");
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
	public void onClientShutdown(ClientShutdown event)
	{
		//savePetStats(this.currentPet.getName(), this.petLevels);
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
		if (npcDespawned.getActor() == this.currentPet) {
			log.debug("Follower despawned");
			//savePetStats(this.currentPet.getName(), this.petLevels);
		}

		updateFollower();
	}

	public void updateFollower() {
		NPC follower = client.getFollower();
		this.currentPet = follower;

		if (this.currentPet != null) {
			log.debug("Current follower is: {}", follower.getName());

			if (this.currentPetLevels == null) {
				this.currentPetLevels = new PetLevels(follower.getName());
				log.debug("made fresh pet levels because it was null");
			} else {
				log.debug("currently saving levels for: {}", this.currentPetLevels.getName());
				if (this.currentPetLevels.getName() != this.currentPet.getName()){
					log.debug("need fresh pet levels because it's new pet: prev {} curr {}", this.currentPetLevels.getName(), this.currentPet.getName());
					this.currentPetLevels = new PetLevels(follower.getName());
				}
			}

			log.debug("XP: CRAFTING {} MINING {}", this.currentPetLevels.getXp(Skill.CRAFTING), this.currentPetLevels.getXp(Skill.MINING));
			log.debug("LEVEL: CRAFTING {} MINING {}", this.currentPetLevels.getLevel(Skill.CRAFTING), this.currentPetLevels.getLevel(Skill.MINING));
		}
	}

	public void savePetStats(String petName, Map<Skill, Integer> petLevels) {
		final String profile = configManager.getRSProfileKey();
		if (profile == null)
		{
			return;
		}

		if (Strings.isNullOrEmpty(profile))
		{
			log.debug("Trying to save pet exp with no profile!");
			return;
		}

		String json = gson.toJson(petLevels);
		configManager.setConfiguration(PetTrainingConfig.GROUP, profile, "levels_" + petName, json);
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		final Skill skill = statChanged.getSkill();
		final int currentXp = statChanged.getXp();
		final int currentPetLevel = currentPetLevels.getLevel(skill);

		log.debug("xp drop {} {}", skill, currentXp);

        playerXp.putIfAbsent(skill, currentXp);

		if (this.currentPet != null) {
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
}
