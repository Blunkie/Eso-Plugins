package net.unethicalite.plugins.esonmz;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.LoopedPlugin;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@Extension
@PluginDescriptor(
		name = "Eso Nmz",
		description = "Auto Nmz plugin",
		enabledByDefault = false
)
@Slf4j
public class EsoNmzPlugin extends LoopedPlugin
{
	@Inject
	private Client client;

	@Inject
	private EsoNmzConfig config;

	private boolean useSpec, activateSpec;
	private int specWeapon, primaryWeapon;
	private static final int[] NMZ_MAP_REGION = { 9033 };
	private static final int[] OVERLOAD_POTIONS = { ItemID.OVERLOAD_1, ItemID.OVERLOAD_2, ItemID.OVERLOAD_3, ItemID.OVERLOAD_4 };

	@Override
	protected void startUp()
	{
		useSpec = config.useSpec();
		specWeapon = config.specWeapon().getSpecWeaponId();
		primaryWeapon = config.primaryWeapon().getPrimaryWeaponId();
	}

	@Override
	protected int loop()
	{
		if (!isInNightmareZone())
		{
			log.info("Not in Nightmare Zone");
			return -1;
		}

		if (Players.getLocal().isMoving())
		{
			return - 1;
		}

		if (activateSpec && useSpec)
		{
			if (!Equipment.contains(specWeapon))
			{
				log.info("Equip spec weapon");
				Inventory.getFirst(specWeapon).interact("Wield");
				return randomBetween(625, 845);
			}

			if (!Combat.isSpecEnabled())
			{
				Combat.toggleSpec();
			}

			return randomBetween(205, 305);
		}

		if (!Equipment.contains(primaryWeapon) && useSpec)
		{
			log.info("Equip primary weapon");
			Inventory.getFirst(primaryWeapon).interact("Wield");
			return randomBetween(625, 845);
		}

		if (client.getBoostedSkillLevel(Skill.STRENGTH) != client.getRealSkillLevel(Skill.STRENGTH))
		{
			return -1;
		}

		if (!Inventory.contains(OVERLOAD_POTIONS))
		{
			return -1;
		}

		if (Players.getLocal().getAnimation() != AnimationID.CONSUMING)
		{
			log.info("Drinking overload potion");
			Inventory.getFirst(OVERLOAD_POTIONS).interact("Drink");
		}

		return randomBetween(405, 905);
	}

	private boolean isInNightmareZone()
	{
		return client.getLocalPlayer().getPlane() > 0 && Arrays.equals(client.getMapRegions(), NMZ_MAP_REGION);
	}

	private int randomBetween(int min, int max)
	{
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	@Subscribe
	private void onGameObjectSpawned(GameObjectSpawned e)
	{
		if (!useSpec)
		{
			return;
		}

		GameObject object = e.getGameObject();

		if (object.getId() != ObjectID.POWER_SURGE)
		{
			return;
		}

		if (Players.getLocal().isMoving())
		{
			return;
		}

		log.info("Activating surge");
		object.interact("Activate");
	}

	@Subscribe
	private void onChatMessage(ChatMessage e)
	{
		String msg = e.getMessage();

		if (msg.equals("You feel a surge of special attack power!"))
		{
			activateSpec = true;
		}

		if (msg.equals("Your surge of special attack power has ended."))
		{
			activateSpec = false;
		}
	}

	@Provides
	EsoNmzConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EsoNmzConfig.class);
	}
}
