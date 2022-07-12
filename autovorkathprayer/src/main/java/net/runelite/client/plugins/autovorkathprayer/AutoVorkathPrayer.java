package net.runelite.client.plugins.autovorkathprayer;

import net.unethicalite.plugins.packetutils.packets.MousePackets;
import net.unethicalite.plugins.packetutils.packets.WidgetPackets;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.automationapi.Game;
import net.runelite.client.plugins.automationapi.NpcManager;
import net.runelite.client.plugins.automationapi.PrayerManager;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(name = "Auto Vorkath Prayer", description = "Auto Vorkath Prayer", enabledByDefault = false)
@PluginDependency(Game.class)
@Slf4j
public class AutoVorkathPrayer extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Game game;

	@Inject
	private NpcManager npcManager;

	@Inject
	private PrayerManager prayerManager;

	@Inject
	private KeyManager keyManager;

	@Inject
	private WidgetPackets widgetPackets;

	@Inject
	private MousePackets mousePackets;

	private boolean activateFlicker;

	private final int quickPrayerWidgetId = WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId();

	@Override
	protected void startUp()
	{

	}

	@Override
	protected void shutDown()
	{
		activateFlicker = false;

		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		clientThread.invoke(() ->
		{
			if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1)
			{
				togglePrayer();
			}
		});
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (isAtVorkath() && !prayerManager.isQuickPrayerActive())
		{
			activateFlicker = true;
		}

		if (!isAtVorkath() && prayerManager.isQuickPrayerActive())
		{
			activateFlicker = false;
			toggleFlicker();
		}

		if (activateFlicker)
		{
			if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1)
			{
				togglePrayer();
			}
			togglePrayer();
		}
	}

	private void togglePrayer()
	{
		mousePackets.queueClickPacket();
		widgetPackets.queueWidgetActionPacket(1, quickPrayerWidgetId, -1, -1);
	}

	public void switchAndUpdatePrayers(int i) {
		mousePackets.queueClickPacket();
		widgetPackets.queueWidgetActionPacket(1, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), -1, i);
		togglePrayer();
		togglePrayer();
	}

	public void updatePrayers() {
		togglePrayer();
		togglePrayer();
	}

	private void toggleFlicker()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!activateFlicker)
		{
			clientThread.invoke(() ->
			{
				if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1)
				{
					togglePrayer();
				}
			});
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event) {
		if (activateFlicker)
		{
			if (event.getParam1() == WidgetInfo.QUICK_PRAYER_PRAYERS.getId())
			{
				if (event.getMenuOption().equals("Quick Prayer Update"))
				{
					updatePrayers();
					event.consume();
					return;
				}

				event.consume();
				switchAndUpdatePrayers(event.getParam0());
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{

	}

	private boolean isAtVorkath()
	{
		NPC vorkath = npcManager.findNpc("Vorkath");
		return client.isInInstancedRegion() && vorkath != null && !vorkath.isHidden();
	}
}