package com.PetTraining;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class PetTrainingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(PetTrainingPlugin.class);
		RuneLite.main(args);
	}
}