package net.unethicalite.plugins.esopestcontrol;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.Players;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.scene.Tiles;
import net.unethicalite.api.widgets.Prayers;
import net.unethicalite.api.widgets.Widgets;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Extension
@PluginDescriptor(
		name = "Eso Pest Control",
		description = "Pest Control plugin",
		enabledByDefault = false
)
@Slf4j
public class EsoPestControl extends LoopedPlugin
{
	@Inject
	private Client client;

	private static final List<String> monsterList = List.of("Brawler", "Defiler", "Ravager", "Shifter", "Splatter", "Torcher");

	private int randomY;

	@Override
	protected void startUp()
	{
		randomY = ThreadLocalRandom.current().nextInt(15, 20);
	}

	@Override
	protected int loop()
	{
		if (!insideGame())
		{
			if (!insideBoat())
			{
				enterBoat();
				return -1;
			}
			if (Prayers.isEnabled(Prayer.EAGLE_EYE))
			{
				Prayers.toggle(Prayer.EAGLE_EYE);
			}
			return -1;
		}

		if (!atMiddleArea())
		{
			walkToMiddle();
			return -1;
		}

		NPC npc = findValidMonster();

		if (npc != null)
		{
			fightMonster(npc);
		}
		return 35;
	}

	private void enterBoat()
	{
		log.info("Entering boat");
		TileObject plank = TileObjects.getNearest(25631);

		plank.interact("Cross");
		Time.sleep(1_000);
	}

	private void walkToMiddle()
	{
		NPC squire = NPCs.getNearest("Squire");

		if (squire != null && squire.distanceTo(Players.getLocal()) < 8)
		{
			log.info("Walking to middle (from boat)");
			WorldPoint voidKnightArea = new WorldPoint(Players.getLocal().getWorldX(), Players.getLocal().getWorldY() - randomY, 0);

			Time.sleep(405, 1205);

			Movement.walk(voidKnightArea);
			randomY = ThreadLocalRandom.current().nextInt(15, 20);

			Time.sleep(5_005, 7_005);
			return;
		}

		log.info("Walking to middle");
		NPC voidKnight = NPCs.getNearest("Void Knight");
		Movement.walk(voidKnight);
		Time.sleep(2_035, 3_450);
	}

	private void fightMonster(NPC npc)
	{
		if (Players.getLocal().getInteracting() != null)
		{
			if (!Prayers.isEnabled(Prayer.EAGLE_EYE))
			{
				Prayers.toggle(Prayer.EAGLE_EYE);
			}
			return;
		}

		log.info("Attacking monster");
		npc.interact("Attack");
		Time.sleep(350, 650);
	}

	private NPC findValidMonster()
	{
		NPC voidKnight = NPCs.getNearest("Void Knight");

		int voidKnightY = voidKnight.getWorldLocation().getY();

		return NPCs.getNearest(x -> monsterList.contains(x.getName()) && !x.isDead() && x.distanceTo(voidKnight) < 12 && (x.getWorldLocation().getY() + 7) > voidKnightY);
	}

	private boolean insideGame()
	{
		return client.isInInstancedRegion();
	}

	private boolean insideBoat()
	{
		return Players.getLocal().getWorldLocation().getX() < 2_643;
	}

	private boolean atMiddleArea()
	{
		NPC voidKnight = NPCs.getNearest("Void Knight");
		return voidKnight != null && Players.getLocal().distanceTo(voidKnight.getWorldLocation()) < 8;
	}
}
