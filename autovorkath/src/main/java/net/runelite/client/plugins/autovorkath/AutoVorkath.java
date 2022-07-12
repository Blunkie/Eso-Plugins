package net.runelite.client.plugins.autovorkath;

import com.google.inject.Provides;
import com.openosrs.client.game.NPCManager;
import com.openosrs.client.game.NPCStats;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.automationapi.*;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static net.runelite.api.GraphicID.*;
import static net.runelite.api.ItemID.*;
import static net.runelite.api.NpcID.ZOMBIFIED_SPAWN_8063;
import static net.runelite.api.ObjectID.ACID_POOL_32000;
import static net.runelite.client.plugins.autovorkath.State.*;
import static net.runelite.client.plugins.autovorkath.StaticData.*;

@Extension
@PluginDescriptor(name = "Auto Vorkath", description = "Auto Vorkath", enabledByDefault = false)
@PluginDependency(Game.class)
@SuppressWarnings("unused")
@Slf4j
public class AutoVorkath extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private AutoVorkathConfig config;

	@Inject
	private Game game;

	@Inject
	private PlayerManager playerManager;

	@Inject
	private InventoryManager inventoryManager;

	@Inject
	private BankManager bankManager;

	@Inject
	private NpcManager npcManager;

	@Inject
	private NPCManager npcManagerOpenOSRS;

	@Inject
	private ObjectManager objectManager;

	@Inject
	private WalkManager walkManager;

	@Inject
	private CalculationManager calculationManager;

	private Player player;

	public AutoVorkath()
	{
		acidSpots = new ArrayList<>();
		safeTiles = new ArrayList<>();
		lootList = new ArrayList<>();
		itemValues = new HashMap<>();
		bombWorldPoint = null;
		safeTile = null;
		baseTile = new LocalPoint(6208, 7104);
		RELLEKKA = new WorldArea(new WorldPoint(2613, 3645, 0),
				new WorldPoint(2693, 3716, 0));
		dodgeBomb = false;
		dodgeAcid = false;
		killSpawn = false;
		doneBanking = false;
		doneDeposit = false;
		doneWithdrawingPotions = false;
		doneWithdrawingAll = false;
		widgetVisible = false;
		tickDelay = 0;
	}

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		log.info("Auto Vorkath stopped");
		dodgeBomb = false;
		dodgeAcid = false;
		killSpawn = false;
		doneBanking = false;
		doneDeposit = false;
		doneWithdrawingPotions = false;
		doneWithdrawingAll = false;
		widgetVisible = false;
		bombWorldPoint = null;
		safeTiles.clear();
		acidSpots.clear();
		startBot = false;
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		player = client.getLocalPlayer();

		if (player != null && startBot)
		{
			LocalPoint playerLocalLocation = player.getLocalLocation();
			WorldPoint baseTile = WorldPoint.fromLocal(client, StaticData.baseTile);
			NPC vorkathAwake = npcManager.findNpc(VORKATH_AWAKE_ID);
			NPC vorkathAsleep = npcManager.findNpc(VORKATH_ASLEEP_ID);

			if (vorkathAsleep() || !isAtVorkath())
			{
				safeTiles.clear();
				dodgeBomb = false;
				dodgeAcid = false;
				killSpawn = false;
			}

			if (!acidPhaseActive())
			{
				acidSpots.clear();
				dodgeAcid = false;
				safeTile = null;
			}

			Widget runOrb = client.getWidget(WidgetInfo.MINIMAP_RUN_ORB);

			if (tickDelay > 0)
			{
				if (acidPhaseActive())
				{
					if (playerManager.isRunEnabled())
					{
						playerManager.toggleRun(runOrb.getBounds());
					}
				}
				tickDelay--;
				return;
			}

			if (isAtVorkath())
			{
				if (!inventoryManager.contains(DUST_RUNE) || !inventoryManager.contains(CHAOS_RUNE))
				{
					game.sendMsg("No runes");
					return;
				}

				createSafeTiles();

				resetBankingFlags();

				switch (getState())
				{
					case EAT_FOOD:
						log.info("Eating");
						inventoryManager.interactWithItem(SHARK, "Eat", sleepLength());
						break;

					case DRINK_ANTIVENOM:
						log.info("Drinking Antivenom");
						inventoryManager.interactWithItem(inventoryManager.getItem(ANTI_VENOM_POTIONS).getId(), "Drink", sleepLength());
						tickDelay = 1;
						break;

					case DRINK_ANTIFIRE:
						log.info("Drinking Antifire");
						inventoryManager.interactWithItem(inventoryManager.getItem(ANTI_FIRE_POTIONS).getId(), "Drink", sleepLength());
						tickDelay = 1;
						break;

					case DRINK_BOOST:
						log.info("Drinking boost");
						inventoryManager.interactWithItem(inventoryManager.getItem(DIVINE_RANGING_POTIONS).getId(), "Drink", sleepLength());
						tickDelay = 1;
						break;

					case TOGGLE_RUN:
						log.info("Toggling run");
						playerManager.toggleRun(runOrb.getBounds());
						break;

					case POKE_VORKATH:
						log.info("Poking Vorkath");
						if (!playerManager.isMoving())
						{
							game.doNpcAction(vorkathAsleep, MenuAction.NPC_FIRST_OPTION, sleepLength());
							tickDelay = 1;
						}
						break;

					case LOOT_VORKATH:
						if (!inventoryManager.isFull() || (client.getItemComposition(getLoot().getId()).isStackable() && inventoryManager.contains(getLoot().getId()))) {
							if (!playerManager.isMoving())
							{
								lootItem(getLoot());
							}
						} else {
							if (inventoryManager.contains(SHARK))
							{
								inventoryManager.interactWithItem(SHARK, "Eat", sleepLength());
								break;
							}

							if ((!enoughFoodLeft() || !hasVenom()) && itemToRemove(getLoot()) != null)
							{
								prioritizeLoot();
								tickDelay = 1;
							}
						}
						break;

					case DISTANCE_CHECK:
						log.info("Distance check");
						walkManager.sceneWalk(baseTile, 0, sleepLength());
						break;

					case SWITCH_RUBY:
						log.info("Switching to Ruby bolts");
						inventoryManager.interactWithItem(RUBY_BOLTS_E, "Wield", sleepLength());
						break;

					case SWITCH_DIAMOND:
						log.info("Switching to Diamond bolts");
						inventoryManager.interactWithItem(DIAMOND_BOLTS_E, "Wield", sleepLength());
						break;

					case KILL_SPAWN:
						NPC zombifiedSpawn = npcManager.findNpc(ZOMBIFIED_SPAWN_8063);

						if (player.getInteracting() != null && player.getInteracting().getName() != null && player.getInteracting().getName().equalsIgnoreCase("Vorkath"))
						{
							walkManager.sceneWalk(playerLocalLocation, 0, sleepLength());
							return;
						}

						if (zombifiedSpawn != null && player.getInteracting() == null)
						{
							log.info("Killing Zombified Spawn");
							attackSpawn();
							tickDelay = 4;
						}
						break;

					case ACID_WALK:
						log.info("Acid walk");
						if (runOrb != null && playerManager.isRunEnabled() && playerManager.isMoving())
						{
							playerManager.toggleRun(runOrb.getBounds());
						}

						safeTiles.sort(Comparator.comparingInt(tile -> tile.distanceTo(player.getWorldLocation())));

						if (safeTile == null)
						{
							for (int i = 0; i < safeTiles.size(); i++) {
								WorldPoint current = safeTiles.get(i);
								WorldPoint next = new WorldPoint(current.getX(), current.getY() - 1, current.getPlane());

								if (!acidSpots.contains(current) && !acidSpots.contains(next))
								{
									safeTile = next;
									break;
								}
							}
						}

						if (safeTile != null)
						{
							if (player.getWorldLocation().equals(safeTile)) {
								game.doNpcAction(vorkathAwake, MenuAction.NPC_SECOND_OPTION, 0);
							} else {
								LocalPoint safeTileLocalPoint = LocalPoint.fromWorld(client, safeTile);
								if (safeTileLocalPoint != null)
								{
									walkManager.walkTile(safeTileLocalPoint.getSceneX(), safeTileLocalPoint.getSceneY());
								}
							}
						}
						break;

					case DODGE_BOMB:
						log.info("Dodging bomb");
						LocalPoint bombLocalPoint = LocalPoint.fromWorld(client, bombWorldPoint);
						LocalPoint dodgeRight = new LocalPoint(bombLocalPoint.getX() + 256, bombLocalPoint.getY());
						LocalPoint dodgeLeft = new LocalPoint(bombLocalPoint.getX() - 256, bombLocalPoint.getY());
						LocalPoint dodgeReset = new LocalPoint(6208, 7872);

						if (dodgeBomb && !player.getWorldLocation().equals(bombWorldPoint))
						{
							bombWorldPoint = null;
							dodgeBomb = false;
							return;
						}

						if (playerLocalLocation.getY() > 7872)
						{
							walkManager.sceneWalk(dodgeReset, 0, sleepLength());
							dodgeBomb = false;
							tickDelay = 1;
							return;
						}

						if (playerLocalLocation.getX() < 6208) {
							walkManager.sceneWalk(dodgeRight, 0, sleepLength());
						} else {
							walkManager.sceneWalk(dodgeLeft, 0, sleepLength());
						}
						break;

					case ATTACK_VORKATH:
						log.info("Attack Vorkath");
						attackVorkath();
						break;

					case TELEPORT_FEROX_ENCLAVE:
						log.info("Teleport to Ferox Enclave");
						teleportToFeroxEnclave();
						break;
				}
			}

			if (isInFeroxEnclave())
			{
				switch (getState())
				{
					case TOGGLE_RUN:
						log.info("Toggling run");
						playerManager.toggleRun(runOrb.getBounds());
						tickDelay = 1;
						break;

					case USE_POOL:
						GameObject pool = objectManager.findGameObjectByAction("Drink");

						if (pool != null && !playerManager.isMoving())
						{
							log.info("Restoring stats");
							game.doGameObjectAction(pool, MenuAction.GAME_OBJECT_FIRST_OPTION, sleepLength());
						}
						tickDelay = 1;
						break;

					case USE_BANK_FEROX_ENCLAVE:
						log.info("Banking");
						if (!bankManager.isOpen() && player.getAnimation() == -1)
						{
							openBank();
							return;
						}

						if (inventoryManager.isEmpty() && !doneDeposit)
						{
							doneDeposit = true;
							return;
						}

						if (!doneDeposit)
						{
							bankManager.depositAll();
							return;
						}

						bankManager.withdrawItem(MOONCLAN_TELEPORT);
						tickDelay = 1;
						break;

					case TELEPORT_MOONCLAN:
						log.info("Teleporting to Moonclan");
						if (bankManager.isOpen())
						{
							bankManager.close();
							return;
						}

						if (player.getAnimation() == -1)
						{
							inventoryManager.interactWithItem(MOONCLAN_TELEPORT, "Break", sleepLength());
							tickDelay = 1;
							return;
						}

						break;
				}
			}

			if (isInLunarIsle())
			{
				switch (getState())
				{
					case TOGGLE_RUN:
						log.info("Toggling run");
						playerManager.toggleRun(runOrb.getBounds());
						tickDelay = 1;
						break;

					case USE_BANK_MOONCLAN:
						useBank();
						break;

					case LEAVE_MOONCLAN:
						log.info("Leaving Moonclan");
						leaveMoonClan();
				}
			}

			switch (getState())
			{
				case TOGGLE_RUN:
					playerManager.toggleRun(runOrb.getBounds());
					break;

				case USE_BOAT:
					GameObject boat = objectManager.findGameObject(29917);

					if (boat != null && !playerManager.isMoving() && !widgetVisible) {
						game.doGameObjectAction(boat, MenuAction.GAME_OBJECT_FIRST_OPTION, sleepLength());
						tickDelay = 2;
					}
					break;

				case USE_OBSTACLE:
					GameObject obstactle = objectManager.findGameObject(31990);

					if (obstactle != null && !playerManager.isMoving() && !widgetVisible)
					{
						game.doGameObjectAction(obstactle, MenuAction.GAME_OBJECT_FIRST_OPTION, sleepLength());
						tickDelay = 2;
					}
					break;
			}
		}
	}

	private State getState()
	{
		if (isAtVorkath())
		{
			NPC vorkathAwake = npcManager.findNpc(VORKATH_AWAKE_ID);
			WorldPoint baseTile = WorldPoint.fromLocal(client, StaticData.baseTile);

			if (player.getAnimation() == 839)
			{
				return TICK_DELAY;
			}

			if (dodgeAcid)
			{
				return ACID_WALK;
			}

			if (dodgeBomb)
			{
				return DODGE_BOMB;
			}

			if (killSpawn)
			{
				return KILL_SPAWN;
			}

			if (shouldEat())
			{
				if (!vorkathAsleep() && vorkathAwake != null && !vorkathAwake.isDead() && !inventoryManager.contains(SHARK) && playerManager.getCurrentAmount(Skill.HITPOINTS) < 40)
				{
					return TELEPORT_FEROX_ENCLAVE;
				}

				if (inventoryManager.contains(SHARK))
				{
					return EAT_FOOD;
				}
			}

			if (shouldDrinkAntivenom())
			{
				if (!vorkathAsleep() && vorkathAwake != null && !vorkathAwake.isDead() && calculateHealth(vorkathAwake) > 125 && !inventoryManager.contains(ANTI_VENOM_POTIONS))
				{
					return TELEPORT_FEROX_ENCLAVE;
				}

				if (inventoryManager.contains(ANTI_VENOM_POTIONS))
				{
					return DRINK_ANTIVENOM;
				}
			}

			if (shouldDrinkAntifire())
			{
				if (!vorkathAsleep() && vorkathAwake != null && !vorkathAwake.isDead() && !inventoryManager.contains(ANTI_FIRE_POTIONS))
				{
					return TELEPORT_FEROX_ENCLAVE;
				}

				if (inventoryManager.contains(ANTI_FIRE_POTIONS))
				{
					return DRINK_ANTIFIRE;
				}
			}

			if (shouldDrinkBoost())
			{
				if (inventoryManager.contains(DIVINE_RANGING_POTIONS))
				{
					return DRINK_BOOST;
				}
			}

			if (!playerManager.isRunEnabled() & !dodgeAcid)
			{
				return TOGGLE_RUN;
			}

			if ((vorkathAwake != null || vorkathWaking())
				&& !dodgeBomb
				&& !dodgeAcid
				&& !killSpawn
				&& !shouldLoot()
				&& !playerManager.isMoving()
				&& baseTile.distanceTo(player.getWorldLocation()) >= 4)
			{
				return DISTANCE_CHECK;
			}

			if ((vorkathAwake == null || vorkathAwake.isDead()) && playerManager.isItemEquipped(DIAMOND_BOLTS_E) && inventoryManager.contains(RUBY_BOLTS_E))
			{
				return SWITCH_RUBY;
			}

			if (vorkathAwake != null && !vorkathAwake.isDead()
					&& playerManager.isItemEquipped(RUBY_BOLTS_E)
					&& inventoryManager.contains(DIAMOND_BOLTS_E)
					&& calculateHealth(vorkathAwake) > 0
					&& calculateHealth(vorkathAwake) < 260
					&& vorkathAwake.getAnimation() != 7960
					&& vorkathAwake.getAnimation() != 7957)
			{
				return SWITCH_DIAMOND;
			}

			if (isAtVorkath()
					&& player.getInteracting() == null
					&& vorkathAwake != null && !vorkathAwake.isDead()
					&& !dodgeBomb
					&& !dodgeAcid
					&& !killSpawn
					&& !vorkathWaking())
			{
				return ATTACK_VORKATH;
			}

			if (shouldLoot())
			{
				return LOOT_VORKATH;
			}

			if (vorkathAsleep()
					&& !shouldLoot()
					&& playerManager.getCurrentAmount(Skill.HITPOINTS) > playerManager.getBaseLevel(Skill.HITPOINTS) - 45
					&& enoughFoodLeft()
					&& hasVenom())
			{
				return POKE_VORKATH;
			}

			if (vorkathAsleep() && !shouldLoot() && (!enoughFoodLeft() || !hasVenom()))
			{
				return TELEPORT_FEROX_ENCLAVE;
			}
		}

		if (isInFeroxEnclave())
		{
			if (!playerManager.isRunEnabled())
			{
				return TOGGLE_RUN;
			}

			if (playerManager.getCurrentAmount(Skill.HITPOINTS) < playerManager.getBaseLevel(Skill.HITPOINTS))
			{
				return USE_POOL;
			}

			if (!inventoryManager.contains(MOONCLAN_TELEPORT))
			{
				return USE_BANK_FEROX_ENCLAVE;
			}

			return TELEPORT_MOONCLAN;
		}

		if (isInLunarIsle())
		{
			if (!doneBanking)
			{
				return USE_BANK_MOONCLAN;
			}

			return LEAVE_MOONCLAN;
		}

		if (shouldClickBoat() && !playerManager.isMoving())
		{
			return USE_BOAT;
		}

		if (shouldClickObstacle() && !playerManager.isMoving())
		{
			return USE_OBSTACLE;
		}

		return DEFAULT;
	}

	private void leaveMoonClan()
	{
		if (!playerManager.inDialogue() && !widgetVisible)
		{
			GameObject b = objectManager.findGameObjectAtLocation(new WorldPoint(2098, 3920, 0));

			if (b != null && !playerManager.isMoving())
			{
				game.doGameObjectAction(b, MenuAction.GAME_OBJECT_SECOND_OPTION, sleepLength());
				tickDelay = 5;
			}
		}
	}

	private boolean isInFeroxEnclave()
	{
		WorldPoint feroxEnclave = new WorldPoint(3139, 3629, 0);
		return feroxEnclave.distanceTo(player.getWorldLocation()) < 100;
	}

	private boolean isInLunarIsle()
	{
		NPC npc = npcManager.findNpc("Rimae Sirsalis");
		return npc != null;
	}

	private boolean shouldDrinkBoost()
	{
		return playerManager.getCurrentAmount(Skill.RANGED) == playerManager.getBaseLevel(Skill.RANGED)
				&& player.getAnimation() != 839 && (playerManager.getCurrentAmount(Skill.HITPOINTS) > 45);
	}

	private boolean shouldDrinkAntivenom()
	{
		return client.getVarpValue(VarPlayer.POISON.getId()) > 0 && player.getAnimation() != 839;
	}

	private boolean shouldDrinkAntifire()
	{
		return client.getVarbitValue(6101) == 0 && player.getAnimation() != 839;
	}

	private boolean shouldEat()
	{
		return (playerManager.getCurrentAmount(Skill.HITPOINTS) < healthAmount)
				|| (vorkathAsleep() && playerManager.getCurrentAmount(Skill.HITPOINTS) < playerManager.getBaseLevel(Skill.HITPOINTS) - 40);
	}

	private boolean enoughFoodLeft()
	{
		return inventoryManager.getCount(SHARK, false) > 4;
	}

	private boolean hasVenom()
	{
		return inventoryManager.contains(ANTI_VENOM_POTIONS) || client.getVarpValue(VarPlayer.IS_POISONED.getId()) < -40;
	}

	private void attackVorkath()
	{
		NPC npc = npcManager.findNpc(VORKATH_AWAKE_ID);

		if (npc != null && npc.getAnimation() != 7957 && (npc.getAnimation() != 7949 || !npc.isDead()))
		{
			if (npc.getAnimation() != 7949)
				game.doNpcAction(npc, MenuAction.NPC_SECOND_OPTION, sleepLength());
		}
	}

	private void attackSpawn()
	{
		NPC zombifiedSpawn = npcManager.findNpc(ZOMBIFIED_SPAWN_8063);

		if (zombifiedSpawn != null && !zombifiedSpawn.isDead())
		{
			menuEntry = new NewMenuEntry("Cast", "", zombifiedSpawn.getIndex(), MenuAction.WIDGET_TARGET_ON_NPC, 0, 0, false);
			game.oneClickCastSpell(WidgetInfo.SPELL_CRUMBLE_UNDEAD, menuEntry, zombifiedSpawn.getConvexHull().getBounds(), sleepLength());
		}
	}

	private void teleportToFeroxEnclave()
	{
		menuEntry = new NewMenuEntry("Ferox Enclave", "", 4, MenuAction.CC_OP, -1, 25362456, false);
		if (player.getAnimation() == -1)
		{
			game.doAction(menuEntry, new Point(0, 0), sleepLength());
		}
	}

	private void useBank()
	{
		if (!bankManager.isOpen())
		{
			openBank();
			return;
		}

		if (madeMistakeBanking())
		{
			bankManager.depositAll();
			return;
		}

		if (!inventoryManager.contains(RUBY_BOLTS_E) && !playerManager.isItemEquipped(RUBY_BOLTS_E))
		{
			bankManager.withdrawAllOf(RUBY_BOLTS_E);
			return;
		}

		if (!inventoryManager.contains(DIAMOND_BOLTS_E) && !playerManager.isItemEquipped(DIAMOND_BOLTS_E))
		{
			bankManager.withdrawAllOf(DIAMOND_BOLTS_E);
			return;
		}

		if (!inventoryManager.contains(RING_OF_DUELING8) && !playerManager.isItemEquipped(RINGS_OF_DUELLING))
		{
			bankManager.withdrawItem(RING_OF_DUELING8);
			tickDelay = 1;
			return;
		}

		if (inventoryManager.contains(RING_OF_DUELING8) && !playerManager.isItemEquipped(RINGS_OF_DUELLING))
		{
			if (bankManager.isOpen())
			{
				bankManager.close();
			}
			inventoryManager.interactWithItem(RING_OF_DUELING8, "Wear", sleepLength());
			tickDelay = 1;
			return;
		}

		if (!doneWithdrawingPotions)
		{
			bankManager.withdrawItems(List.of(EXTENDED_SUPER_ANTIFIRE4, ANTIVENOM4_12913, DIVINE_RANGING_POTION4));
			doneWithdrawingPotions = true;
			return;
		}

		if (!doneWithdrawingAll)
		{
			bankManager.withdrawItemsAll(List.of(DUST_RUNE, ItemID.CHAOS_RUNE, SHARK));
			doneWithdrawingAll = true;
			return;
		}

		doneBanking = true;
	}

	private boolean madeMistakeBanking()
	{
		return inventoryManager.getCount(RING_OF_DUELING8, false) > 1
				|| inventoryManager.getCount(DIVINE_RANGING_POTION4, false) > 1
				|| inventoryManager.getCount(EXTENDED_SUPER_ANTIFIRE4, false) > 1
				|| inventoryManager.getCount(ANTIVENOM4_12913, false) > 1;
	}

	private void openBank()
	{
		if (isInFeroxEnclave())
		{
			GameObject b = objectManager.findGameObject(26711);

			if (b != null && !playerManager.isMoving())
			{
				game.doGameObjectAction(b, MenuAction.GAME_OBJECT_FIRST_OPTION, sleepLength());
			}
		}

		if (isInLunarIsle())
		{
			GameObject b = objectManager.findGameObjectAtLocation(new WorldPoint(2099, 3920, 0));

			if (b != null && !playerManager.isMoving())
			{
				game.doGameObjectAction(b, MenuAction.GAME_OBJECT_SECOND_OPTION, sleepLength());
			}
		}
	}

	private boolean isAtVorkath()
	{
		NPC vorkath = npcManager.findNpc("Vorkath");
		return client.isInInstancedRegion() && vorkath != null;
	}

	private boolean vorkathAsleep()
	{
		NPC vorkathAsleep = npcManager.findNpc(VORKATH_ASLEEP_ID);
		return isAtVorkath() && vorkathAsleep != null;
	}

	private boolean vorkathWaking()
	{
		NPC vorkathWaking = npcManager.findNpc(VORKATH_WAKING_ID);
		return isAtVorkath() && vorkathWaking != null;
	}

	private boolean acidPhaseActive()
	{
		GameObject pool = objectManager.findGameObject(ACID_POOL_32000);
		NPC vorkathAwake = npcManager.findNpc(VORKATH_AWAKE_ID);
		return pool != null || (vorkathAwake != null && vorkathAwake.getAnimation() == 7957);
	}

	private boolean shouldClickObstacle()
	{
		return !client.isInInstancedRegion() && objectManager.findGameObject(31990) != null;
	}

	private boolean shouldClickBoat()
	{
		return RELLEKKA.contains(player.getWorldLocation());
	}

	private void createSafeTiles()
	{
		if (isAtVorkath())
		{
			if (safeTiles.size() > 8)
			{
				safeTiles.clear();
			}

			LocalPoint sw = new LocalPoint(5824, 7104);
			WorldPoint base = WorldPoint.fromLocal(client, sw);

			for (int i = 0; i < 7; i++) {
				safeTiles.add(new WorldPoint(base.getX() + i, base.getY(), base.getPlane()));
			}
		}

		if (!isAtVorkath() && !safeTiles.isEmpty())
		{
			safeTiles.clear();
		}
	}

	private void addAcidSpot(WorldPoint worldPoint)
	{
		if (!acidSpots.contains(worldPoint))
		{
			acidSpots.add(worldPoint);
		}
	}

	private int calculateHealth(NPC npc)
	{
		if (npc == null || npc.getName() == null)
		{
			return -1;
		}

		final int healthScale = npc.getHealthScale();
		final int healthRatio = npc.getHealthRatio();
		final int maxHealth = 750;

		if (healthRatio < 0 || healthScale <= 0)
		{
			return -1;
		}

		return (int)((maxHealth * healthRatio / healthScale) + 0.5f);
	}

	private void lootItem(TileItem item)
	{
		if (item != null)
		{
			menuEntry = new NewMenuEntry("", "", item.getId(), MenuAction.GROUND_ITEM_THIRD_OPTION,  item.getTile().getSceneLocation().getX(), item.getTile().getSceneLocation().getY(), false);
			game.doAction(menuEntry, item.getTile().getSceneLocation(), sleepLength());
		}
	}

	private boolean shouldLoot()
	{
		if (!vorkathAsleep() || getLoot() == null)
		{
			return false;
		}

		if (getLoot().getId() == SUPERIOR_DRAGON_BONES)
		{
			if (inventoryManager.isFull() && enoughFoodLeft() && hasVenom())
			{
				return false;
			}
		}

		if (inventoryManager.isFull() && inventoryManager.contains(SHARK) && itemToRemove(getLoot()) != null)
		{
			return true;
		}

		return !inventoryManager.isFull()
				|| (client.getItemComposition(getLoot().getId()).isStackable() && inventoryManager.contains(getLoot().getId()))
				|| ((!enoughFoodLeft() || !hasVenom()) && itemToRemove(getLoot()) != null);
	}

	private TileItem getLoot()
	{
		Set<TileItem> items = Game.loadedTileItems;

		if (items.isEmpty())
		{
			return null;
		}

		List<TileItem> filteredList = items.stream().filter(a ->
		{
			int value = 0;
			if (itemValues.containsKey(a.getId())) {
				value = itemValues.get(a.getId()) * a.getQuantity();
			} else {
				itemValues.put(a.getId(), game.getItemPrice(a.getId()));
			}

			String name = client.getItemComposition(a.getId()).getName().toLowerCase();
			return lootList.stream().anyMatch(name::contains) || value > 15_000 || (a.getId() == (BLUE_DRAGONHIDE + 1)) || (a.getId() == SUPERIOR_DRAGON_BONES);

		}).sorted(Comparator.comparingInt(b -> itemValues.get(b.getId()) * b.getQuantity())).collect(Collectors.toList());

		Collections.reverse(filteredList);

		if (!filteredList.isEmpty()) {
			if (filteredList.get(0).getId() == SUPERIOR_DRAGON_BONES)
			{
				return filteredList.stream().filter(a -> a.getId() == SUPERIOR_DRAGON_BONES).min(Comparator.comparingInt(b -> b.getTile().getWorldLocation().distanceTo(player.getWorldLocation()))).get();
			}

			if (filteredList.get(0).getId() == (BLUE_DRAGONHIDE + 1))
			{
				return filteredList.stream().filter(a -> a.getId() == (BLUE_DRAGONHIDE + 1)).min(Comparator.comparingInt(b -> b.getTile().getWorldLocation().distanceTo(player.getWorldLocation()))).get();
			}

			return filteredList.get(0);
		}
		return null;
	}

	private void prioritizeLoot()
	{
		WidgetItem itemToRemove = itemToRemove(getLoot());

		if (itemToRemove != null)
		{
			if (itemToRemove.getId() == SHARK)
			{
				inventoryManager.interactWithItem(SHARK, "Eat", sleepLength());
				return;
			}
			inventoryManager.interactWithItem(itemToRemove.getId(), "Drop", sleepLength());
		}
	}

	private WidgetItem itemToRemove(TileItem loot)
	{
		int value = itemValues.get(loot.getId()) * loot.getQuantity();

		for (WidgetItem item : inventoryManager.getWidgetItems())
		{
			String itemName = client.getItemComposition(item.getId()).getName();
			String lootName = client.getItemComposition(loot.getId()).getName();

			if (item == null || itemName.equalsIgnoreCase(lootName) || !client.getItemComposition(item.getId()).isTradeable())
			{
				continue;
			}

			if (itemValues.containsKey(item.getId())) {
				if ((itemValues.get(item.getId()) * item.getQuantity()) < (value - 5_000))
				{
					return item;
				}
			} else {
				itemValues.put(item.getId(), game.getItemPrice(item.getId()));
			}
		}
		return null;
	}

	private long sleepLength()
	{
		return calculationManager.getRandom(config.minSleep(), config.maxSleep());
	}

	private void resetBankingFlags()
	{
		doneBanking = false;
		doneDeposit = false;
		doneWithdrawingPotions = false;
		doneWithdrawingAll = false;
	}

	@Subscribe
	private void onAnimationChanged(AnimationChanged event)
	{
		if (player != null && isAtVorkath())
		{
			Actor actor = event.getActor();

			if (actor.getAnimation() == 7949)
			{
				dodgeAcid = false;
				killSpawn = false;
			}

			if (actor.getAnimation() == 7889)
			{
				killSpawn = false;
				tickDelay = 3;
				return;
			}

			if (actor.getName() != null && actor.getName().equalsIgnoreCase("Vorkath") && actor.getAnimation() == 7957)
			{
				tickDelay = 1;
			}
		}
	}

	@Subscribe
	private void onProjectileSpawned(ProjectileSpawned event)
	{
		if (player != null && isAtVorkath())
		{
			Projectile projectile = event.getProjectile();

			if (projectile.getId() == VORKATH_BOMB_AOE)
			{
				bombWorldPoint = player.getWorldLocation();
				dodgeBomb = true;
			}

			if (projectile.getId() == VORKATH_POISON_POOL_AOE)
			{
				dodgeAcid = true;
			}

			if (projectile.getId() == VORKATH_ICE)
			{
				killSpawn = true;
			}
		}
	}

	@Subscribe
	private void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();
		WorldPoint worldPoint = WorldPoint.fromLocal(client, event.getPosition());

		if (player != null && isAtVorkath())
		{
			if (projectile.getId() == VORKATH_POISON_POOL_AOE)
			{
				addAcidSpot(worldPoint);
			}
		}
	}

	@Subscribe
	private void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == 174)
		{
			widgetVisible = true;
		}
	}

	@Subscribe
	private void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == 174)
		{
			widgetVisible = false;
		}
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked event)
	{
		if (!event.getGroup().equals("AutoVorkathConfig"))
		{
			return;
		}

		if (event.getKey().equals("startButton"))
		{
			if (!startBot)
			{
				log.info("Auto Vorkath started");
				lootList = Arrays.asList(config.lootList().toLowerCase().split("\\s*,\\s*"));
				healthAmount = calculationManager.getRandom(config.healthAmount() - 5, config.healthAmount() + 5);
				startBot = true;
				return;
			}
			shutDown();
		}
	}

	@Provides
	AutoVorkathConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AutoVorkathConfig.class);
	}
}