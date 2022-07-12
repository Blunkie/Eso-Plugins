package net.unethicalite.plugins.esomagic;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.NPC;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.magic.Magic;
import net.unethicalite.api.magic.SpellBook;
import org.pf4j.Extension;

import javax.inject.Inject;

@Extension
@PluginDescriptor(
		name = "Eso Magic",
		description = "Curse-Alch plugin",
		enabledByDefault = false
)
@Slf4j
public class EsoMagicPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private EsoMagicConfig config;

	@Inject
	private ClientThread clientThread;

	private NPC targetNpc;
	private Item targetItem;
	private boolean doAlch;
	private int tickDelay;

	@Override
	protected void startUp()
	{
		resetVariables();
	}

	@Override
	protected void shutDown()
	{
		resetVariables();
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		targetNpc = NPCs.getNearest(config.npcId());
		targetItem = Inventory.getFirst(config.itemId());

		if (targetNpc == null || targetItem == null)
		{
			return;
		}

		if (noRunesLeft())
		{
			log.info("No runes");
			return;
		}

		if (tickDelay > 0)
		{
			tickDelay--;
			return;
		}

		if (!doAlch)
		{
			Magic.cast(config.spell().getSpell(), targetNpc);
			doAlch = true;
			return;
		}

		Magic.cast(SpellBook.Standard.HIGH_LEVEL_ALCHEMY, targetItem);
		doAlch = false;
		tickDelay = 3;
	}

	private boolean noRunesLeft()
	{
		if (!SpellBook.Standard.HIGH_LEVEL_ALCHEMY.haveRunesAvailable())
		{
			return true;
		}

		if (config.spell().getSpell() == SpellBook.Standard.CURSE)
		{
			return !SpellBook.Standard.CURSE.haveRunesAvailable();
		}

		return !Inventory.contains("Soul rune");
	}

	private void resetVariables()
	{
		targetNpc = null;
		targetItem = null;
		doAlch = false;
		tickDelay = 0;
	}

	@Provides
	EsoMagicConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(EsoMagicConfig.class);
	}
}
