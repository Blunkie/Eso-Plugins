package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static net.runelite.client.plugins.automationapi.Game.currentlyLooping;

@Slf4j
@Singleton
public class BankManager
{
    @Inject
    private Client client;

    @Inject
    private Game game;

    @Inject
    private MenuManager menuManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private InventoryManager inventoryManager;

    @Inject
    private CalculationManager calculationManager;

    @Inject
    private ExecutorService executorService;

    public boolean isOpen()
    {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    public void close()
    {
        if (!isOpen())
        {
            return;
        }

        menuManager.setEntry(new NewMenuEntry("", "", 1, MenuAction.CC_OP, 11, 786434, false));

        Widget bankCloseWidget = client.getWidget(WidgetInfo.BANK_PIN_EXIT_BUTTON);

        if (bankCloseWidget != null)
        {
            executorService.submit(() -> mouseManager.handleMouseClick(bankCloseWidget.getBounds()));
            return;
        }

        mouseManager.delayMouseClick(new Point(0, 0), calculationManager.getRandom(15, 95));
    }

    public void depositAll()
    {
        if (!isOpen())
        {
            return;
        }

        executorService.submit(() ->
        {
            Widget depositInventoryWidget = client.getWidget(WidgetInfo.BANK_DEPOSIT_INVENTORY);

            menuManager.setEntry(new NewMenuEntry("", "", 1, MenuAction.CC_OP, -1, 786474, false));

            if (depositInventoryWidget != null) {
                mouseManager.handleMouseClick(depositInventoryWidget.getBounds());
            } else {
                mouseManager.clickRandomPointCenter(-200, 200);
            }
        });
    }

    public void withdrawItem(int itemId)
    {
        Widget item = game.getFromClientThread(() -> getBankItem(itemId));

        if (item != null)
        {
            withdrawItem(item);
        }
    }

    private void withdrawItem(Widget item)
    {
        NewMenuEntry menuEntry = new NewMenuEntry("", "", (game.getFromClientThread(() -> client.getVarbitValue(6590)) == 0) ? 1 : 2,
                MenuAction.CC_OP, item.getIndex(), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false);
        game.doAction(menuEntry, item.getBounds(), 0);
    }

    public void withdrawAllOf(int itemId)
    {
        Widget item = game.getFromClientThread(() -> getBankItem(itemId));

        if (item != null)
        {
            withdrawAllOf(item);
        }
    }

    private void withdrawAllOf(Widget item)
    {
        executorService.submit(() -> {
           menuManager.setEntry(new NewMenuEntry("Withdraw-All" , "", 7, MenuAction.CC_OP, item.getIndex(), WidgetInfo.BANK_ITEM_CONTAINER.getId(), false));
           mouseManager.clickRandomPointCenter(-200, 200);
        });
    }

    public void depositExcept(Collection<Integer> ignoreList)
    {
        if (!isOpen())
        {
            return;
        }

        List<Integer> depositedItems = new ArrayList<>();

        executorService.submit(() ->
        {
            try {
                for (WidgetItem item : inventoryManager.getWidgetItems())
                {
                    if (!ignoreList.contains(item.getId()) && !depositedItems.contains(item.getId()))
                    {
                        depositItem(item);
                        Game.sleep(calculationManager.getRandom(40, 80));
                        depositedItems.add(item.getId());

                    }
                }
                depositedItems.clear();

            } catch (Exception e) {
                depositedItems.clear();
            }

        });
    }

    private void depositItem(WidgetItem item)
    {
        if (!isOpen())
        {
            return;
        }

        NewMenuEntry menuEntry = new NewMenuEntry("", "", 8, MenuAction.CC_OP,item.getIndex(), 983043, false);
        menuManager.setEntry(menuEntry);
        mouseManager.handleMouseClick(item.getCanvasBounds());
    }

    public void withdrawItems(Collection<Integer> itemsToWithdraw)
    {
        if (!isOpen())
        {
            return;
        }

        List<Integer> withdrawnItems = new ArrayList<>();

        executorService.submit(() ->
        {
            try {
                currentlyLooping = true;

                for (int item : itemsToWithdraw)
                {
                    if (!withdrawnItems.contains(item))
                    {
                        withdrawItem(item);
                        Game.sleep(calculationManager.getRandom(40, 80));
                        withdrawnItems.add(item);
                    }
                }
                currentlyLooping = false;
                withdrawnItems.clear();

            } catch (Exception e) {
                currentlyLooping = false;
            }
        });
    }

    public void withdrawItemsAll(Collection<Integer> itemsToWithdraw)
    {
        if (!isOpen())
        {
            return;
        }

        List<Integer> withdrawnItems = new ArrayList<>();

        executorService.submit(() ->
        {
            try {
                currentlyLooping = true;

                for (int item : itemsToWithdraw)
                {
                    if (!withdrawnItems.contains(item))
                    {
                        withdrawAllOf(item);
                        Game.sleep(calculationManager.getRandom(40, 80));
                        withdrawnItems.add(item);
                    }
                }
                currentlyLooping = false;
                withdrawnItems.clear();

            } catch (Exception e) {
                currentlyLooping = false;
                withdrawnItems.clear();
            }
        });
    }

    public Widget getBankItem(int itemId)
    {
        if (!isOpen())
        {
            return null;
        }

        WidgetItem widgetItem = new BankItemQuery().idEquals(itemId).result(client).first();

        if (widgetItem != null) {
            return widgetItem.getWidget();
        } else {
            return null;
        }
    }

    public Widget getBankItem(Collection<Integer> itemIds)
    {
        if (!isOpen())
        {
            return null;
        }

        WidgetItem widgetItem = new BankItemQuery().idEquals(itemIds).result(client).first();

        if (widgetItem != null) {
            return widgetItem.getWidget();
        } else {
            return null;
        }
    }
}
