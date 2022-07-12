package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static net.runelite.api.ItemID.*;

@Slf4j
@Singleton
public class PlayerManager
{
    @Inject
    private Client client;

    @Inject
    private Game game;

    @Inject
    private InventoryManager inventoryManager;

    @Inject
    private BankManager bankManager;

    @Inject
    private MenuManager menuManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private CalculationManager calculationManager;

    @Inject
    private ExecutorService executorService;

    private final List<Integer> STAMINA_POTIONS = List.of(STAMINA_POTION1, STAMINA_POTION2, STAMINA_POTION3, STAMINA_POTION4);

    public boolean isMoving()
    {
        Player player = client.getLocalPlayer();

        if (player == null)
        {
            return false;
        }
        return player.getIdlePoseAnimation() != player.getPoseAnimation();
    }

    public boolean isAnimating()
    {
        return client.getLocalPlayer().getAnimation() != -1;
    }

    public boolean isRunEnabled()
    {
        return client.getVarpValue(173) == 1;
    }

    public void toggleRun(Rectangle runOrbBounds)
    {
        executorService.submit(() ->
        {
            menuManager.setEntry(new NewMenuEntry("Toggle run", "", 1, MenuAction.CC_OP, -1, 10485783, false));
            mouseManager.delayMouseClick(runOrbBounds, calculationManager.getRandom(15, 95));
        });
    }

    public boolean isItemEquipped(int id)
    {
        assert client.isClientThread();

        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

        if (equipmentContainer != null)
        {
            Item[] items = equipmentContainer.getItems();
            for (Item item : items)
            {
                if (item.getId() == id)
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isItemEquipped(Collection<Integer> ids)
    {
        assert client.isClientThread();

        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

        if (equipmentContainer != null)
        {
            Item[] items = equipmentContainer.getItems();
            for (Item item : items)
            {
                if (ids.contains(item.getId()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSpecActive()
    {
        return client.getVarpValue(VarPlayer.SPECIAL_ATTACK_ENABLED.getId()) == 1;
    }

    public int getSpecialPercent()
    {
        return client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT.getId()) / 10;
    }

    public void toggleSpec()
    {
        Widget specWidget = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);

        if (specWidget != null)
        {
            NewMenuEntry menuEntry = new NewMenuEntry("", "", 1, MenuAction.CC_OP, -1, WidgetInfo.MINIMAP_SPEC_CLICKBOX.getId(), false);
            game.doAction(menuEntry, specWidget.getBounds(), calculationManager.getRandom(25, 105));
        }
    }

    public boolean inDialogue()
    {
        final Widget NPC_NAME = client.getWidget(231, 4);
        final Widget DIALOGUE_NPC = client.getWidget(219, 0);
        final Widget DIALOGUE_PLAYER = client.getWidget(217, 0);

        if (NPC_NAME != null && !NPC_NAME.isHidden())
        {
            return true;
        }

        if (DIALOGUE_NPC != null && !DIALOGUE_NPC.isHidden())
        {
            return true;
        }

        return DIALOGUE_PLAYER != null && !DIALOGUE_PLAYER.isHidden();
    }

    public int getCurrentAmount(Skill skill)
    {
        return client.getBoostedSkillLevel(skill);
    }

    public int getBaseLevel(Skill skill)
    {
        return client.getRealSkillLevel(skill);
    }

    public boolean shouldDrinkStamina(int energy, int minEnergy)
    {
        return (client.getVarbitValue(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && client.getEnergy() < energy) || client.getEnergy() < minEnergy;
    }

    public void drinkStamina()
    {
        if (!bankManager.isOpen())
            return;

        if (!inventoryManager.contains(STAMINA_POTIONS))
        {
            Widget item = bankManager.getBankItem(STAMINA_POTIONS);

            if (item != null)
                bankManager.withdrawItem(item.getItemId());

            return;
        }

        WidgetItem item = inventoryManager.getItem(STAMINA_POTIONS);

        NewMenuEntry menuEntry = new NewMenuEntry("Drink", "", 9, MenuAction.CC_OP_LOW_PRIORITY, item.getIndex(), 983043, true);
        menuManager.setEntry(menuEntry);
        game.doAction(menuEntry, item.getCanvasBounds(), calculationManager.getRandom(100, 300));
    }
}
