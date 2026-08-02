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

	private final Map<Skill, Integer> playerXp = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> petXp = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> petLevels = new EnumMap<>(Skill.class);

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
			petXp.replaceAll((k,v) -> 0);

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
			savePetStats(this.currentPet.getName(), this.petLevels);
		}

		updateFollower();
	}

	public void updateFollower() {
		NPC follower = client.getFollower();
		if (follower != null) {
			log.debug("Current follower is: {}", follower.getName());
		}
		this.currentPet = follower;
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

		if (this.currentPet != null) {
			if (playerXp.get(skill) != null) {
				// if it was previously null then the gain is on load so we don't add it to the pets exp
				log.debug("Gained exp in: {} {}", skill, currentXp);

				int deltaXp = currentXp - playerXp.get(skill);
				if (petXp.get(skill) != null) {
					petXp.put(skill, petXp.get(skill) + deltaXp);

					// don't add exp over max xp
					if (petXp.get(skill) > Experience.MAX_SKILL_XP) {
						petXp.put(skill, Experience.MAX_SKILL_XP);
					}
				}
				else {
					petXp.put(skill, deltaXp);
				}

				log.debug("{} GAINED {} {} XP FOR NEW TOTAL {}", currentPet.getName(), deltaXp, skill, petXp.get(skill));
			}

			playerXp.put(skill, currentXp);

			checkLevelUp(skill);
		}
	}

	public void checkLevelUp(Skill skill) {
		if (petLevels.get(skill) != null) {
			int skillLevel = Experience.getLevelForXp(petXp.get(skill));

			if ((skillLevel > petLevels.get(skill)) && (skillLevel < Experience.MAX_REAL_LEVEL)) {
				String levelUpStr = "%s reached Level %d %s!";
				String msg = String.format(levelUpStr, this.currentPet.getName(), skillLevel, skill);
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
			}
			if ((skillLevel > petLevels.get(skill)) && (skillLevel == Experience.MAX_REAL_LEVEL)) {
				String levelUpStr = "%s maxed %s!";
				String msg = String.format(levelUpStr, this.currentPet.getName(), skill);
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", msg, null);
			}

			petLevels.put(skill, skillLevel);
		}
		else {
			petLevels.put(skill, 1);
		}
	}
}
