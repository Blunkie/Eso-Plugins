package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Singleton
@Extension
@PluginDescriptor(name = "Automation API", description = "API for creating automated plugins")
@Slf4j
public class Game extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private MenuManager menuManager;

	@Inject
	private MouseManager mouseManager;

	@Inject
	private ActionManager actionManager;

	@Inject
	private WalkManager walkManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	public static boolean currentlyLooping;

	public static Set<TileItem> loadedTileItems = new HashSet<>();

	public Game()
	{
		currentlyLooping = false;
		loadedTileItems.clear();
	}

	@Subscribe
	private void onGameTick(GameTick event)
	{
		actionManager.onGameTick(event);
	}

	@Subscribe
	private void onClientTick(ClientTick event)
	{
		actionManager.onClientTick(event);
	}

	public void doAction(NewMenuEntry menuEntry, Rectangle r, long delay)
	{
		Point p = mouseManager.getClickPoint(r);
		doAction(menuEntry, p, delay);
	}

	public void doAction(NewMenuEntry menuEntry, Point p, long delay)
	{
		Runnable r = () ->
		{
			menuManager.setEntry(menuEntry);
			mouseManager.handleMouseClick(p);
		};

		actionManager.delayAction(delay, r);
	}

	public void doGameObjectAction(GameObject object, MenuAction menuAction, long delay)
	{
		if (object != null && object.getConvexHull() != null)
		{
			Rectangle r = object.getConvexHull().getBounds() != null ? object.getConvexHull().getBounds() : new Rectangle(client.getCenterX() - 45, client.getCenterY() - 45, 95, 95);
			NewMenuEntry menuEntry = new NewMenuEntry("", "", object.getId(), menuAction, object.getSceneMinLocation().getX(), object.getSceneMinLocation().getY(), false);
			doAction(menuEntry, r, delay);
		}
	}

	public void doNpcAction(NPC npc, MenuAction menuAction, long delay)
	{
		if (npc != null && npc.getConvexHull() != null)
		{
			Rectangle r = npc.getConvexHull().getBounds() != null ? npc.getConvexHull().getBounds() : new Rectangle(client.getCenterX() - 45, client.getCenterY() - 45, 95, 95);
			NewMenuEntry menuEntry = new NewMenuEntry("", "", npc.getIndex(), menuAction, 0, 0, false);
			doAction(menuEntry, r, delay);
		}
	}

	public void oneClickCastSpell(WidgetInfo info, NewMenuEntry menuEntry, Rectangle bounds, long delay)
	{
		menuManager.setEntry(menuEntry);
		menuManager.setSelectedSpell(info);
		mouseManager.delayMouseClick(bounds, delay);
	}

	public int getItemPrice(int id)
	{
		return itemManager.getItemPriceWithSource(id, true);
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN && event.getGameState() != GameState.CONNECTION_LOST)
		{
			loadedTileItems.clear();
		}
	}

	@Subscribe
	private void onItemSpawned(ItemSpawned event)
	{
		loadedTileItems.add(event.getItem());
	}

	@Subscribe
	private void onItemDespawned(ItemDespawned event)
	{
		loadedTileItems.remove(event.getItem());
	}

	@Subscribe
	private void onMenuEntryAdded(MenuEntryAdded event)
	{
		if (menuManager.menuEntry != null)
		{
			client.createMenuEntry(menuManager.menuEntry.getOption(), menuManager.menuEntry.getTarget(), menuManager.menuEntry.getOpCode(), menuManager.menuEntry.getId(),
					menuManager.menuEntry.getParam0(), menuManager.menuEntry.getParam1(), menuManager.menuEntry.isForceLeftClick());
		}
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (menuManager.menuEntry != null)
		{
			if (menuManager.menuEntry.getOption().equals("Walk here"))
			{
				event.consume();
				walkManager.walkTile(WalkManager.x, WalkManager.y);
				menuManager.menuEntry = null;
				return;
			}

			menuAction(event, menuManager.menuEntry.getOption(), menuManager.menuEntry.getTarget(), menuManager.menuEntry.getId(),
					menuManager.menuEntry.getMenuAction(), menuManager.menuEntry.getParam0(), menuManager.menuEntry.getParam1());

			menuManager.menuEntry = null;
		}
	}

	public void menuAction(MenuOptionClicked event, String option, String target, int id, MenuAction menuAction, int param0, int param1)
	{
		event.setMenuOption(option);
		event.setMenuTarget(target);
		event.setId(id);
		event.setMenuAction(menuAction);
		event.setParam0(param0);
		event.setParam1(param1);
	}

	public static void sleep(long amount)
	{
		try {
			long start = System.currentTimeMillis();
			Thread.sleep(amount);

			long now;

			while (start + amount > (now = System.currentTimeMillis()))
			{
				Thread.sleep(start + amount - now);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public <T> T getFromClientThread(Supplier<T> supplier)
	{
		if (!client.isClientThread()) {
			CompletableFuture<T> future = new CompletableFuture<>();

			clientThread.invoke(() ->
			{
				future.complete(supplier.get());
			});
			return future.join();
		} else {
			return supplier.get();
		}
	}

	public void sendMsg(String msg)
	{
		String chatMessage = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append(msg)
				.build();

		chatMessageManager
				.queue(QueuedMessage.builder()
						.type(ChatMessageType.CONSOLE)
						.runeLiteFormattedMessage(chatMessage)
						.build());
	}

	public List<Integer> stringToIntList(String s)
	{
		if (s == null || s.trim().equals(""))
			return List.of(0);

		return Arrays.stream(s.split(",")).map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
	}
}