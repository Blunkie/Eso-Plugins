package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.queries.InventoryItemQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static net.runelite.client.plugins.automationapi.Game.currentlyLooping;

@Slf4j
@Singleton
public class InventoryManager
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private Game game;

    @Inject
    private InventoryAssistant inventoryAssistant;

    @Inject
    private MenuManager menuManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private CalculationManager calculationManager;

    @Inject
    private ExecutorService executorService;

    public boolean isFull()
    {
        return getEmptySlots() <= 0;
    }

    public boolean isEmpty()
    {
        return getEmptySlots() >= 28;
    }

    public int getEmptySlots()
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryAssistant.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    public Collection<WidgetItem> getWidgetItems()
    {
        return inventoryAssistant.getWidgetItems();
    }

    public void interactWithItem(int itemId, String option, long delay)
    {
        NewMenuEntry menuEntry = inventoryAssistant.getNewMenuEntry(itemId, option);

        if (menuEntry == null)
        {
            return;
        }

        WidgetItem widgetItem = inventoryAssistant.getWidgetItem(itemId);

        if (widgetItem == null)
        {
            return;
        }

        game.doAction(menuEntry, widgetItem.getCanvasBounds(), delay);
    }

    public boolean contains(Collection<Integer> itemIds)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }

        return getItems(itemIds).size() >= 1;
    }

    public boolean contains(int itemId)
    {
        if (client.getItemContainer(InventoryID.INVENTORY) == null)
        {
            return false;
        }

        return new InventoryItemQuery(InventoryID.INVENTORY)
                .idEquals(itemId)
                .result(client)
                .size() >= 1;
    }

    public boolean containsAmount(int itemId, int amount, boolean stackable, boolean exact)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        int count = 0;

        if (inventoryWidget != null)
        {
            Collection<WidgetItem> items = inventoryAssistant.getWidgetItems();
            for (WidgetItem item : items)
            {
                if (item.getId() == itemId)
                {
                    if (stackable)
                    {
                        count = item.getQuantity();
                        break;
                    }
                    count++;
                }
            }
        }
        return (!exact || count == amount) && (count >= amount);
    }

    public boolean containsItems(Collection<Integer> requiredItems)
    {
        for (int item : requiredItems)
        {
            if (!contains(item))
            {
                return false;
            }
        }
        return true;
    }

    public boolean onlyContains(Collection<Integer> items)
    {
        for (WidgetItem item : inventoryAssistant.getWidgetItems())
        {
            if (!items.contains(item.getId()))
            {
                return false;
            }
        }
        return true;
    }

    public int getCount(int itemId, boolean stackable)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return -1;
        }

        int count = 0;

        Collection<WidgetItem> items = inventoryAssistant.getWidgetItems();
        for (WidgetItem item : items)
        {
            if (item.getId() == itemId)
            {
                if (stackable)
                {
                    return item.getQuantity();
                }
                count++;
            }
        }
        return count;
    }

    public WidgetItem getItem(int id)
    {
       Collection<WidgetItem> items = inventoryAssistant.getWidgetItems();

       for (WidgetItem item : items)
       {
           if (item.getId() == id)
               return item;
       }
       return null;
    }

    public WidgetItem getItem(Collection<Integer> ids)
    {
        List<WidgetItem> items = getItems(ids);

        if (items.isEmpty())
        {
            return null;
        }
        return items.get(0);
    }

    public List<WidgetItem> getItems(Collection<Integer> ids)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);

        if (inventoryWidget == null)
        {
            return null;
        }

        List<WidgetItem> matchedItems = new ArrayList<>();

        Collection<WidgetItem> items = inventoryAssistant.getWidgetItems();
        for (WidgetItem item : items)
        {
            if (ids.contains(item.getId()))
            {
                matchedItems.add(item);
            }
        }
        return matchedItems;
    }

    public CustomItem createCustomItem(Widget item)
    {
        if (item.getItemId() == 6512)
            return new CustomItem(-1, 0, item.getIndex(), item);

        return new CustomItem(item.getItemId(), item.getItemQuantity(), item.getIndex(), item);
    }

    public Collection<CustomItem> getCustomItems()
    {
        Widget geWidget = client.getWidget(WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER);

        boolean geOpen = geWidget != null && !geWidget.isHidden();
        boolean bankOpen = !geOpen && client.getItemContainer(InventoryID.BANK) != null;

        Widget inventoryWidget = client.getWidget(
                bankOpen ? WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER :
                        geOpen ? WidgetInfo.GRAND_EXCHANGE_INVENTORY_ITEMS_CONTAINER :
                                WidgetInfo.INVENTORY
        );

        if (inventoryWidget == null)
        {
            return new ArrayList<>();
        }

        if (!bankOpen && !geOpen && inventoryWidget.isHidden())
        {
            inventoryAssistant.refreshInventory();
        }

        Widget[] children = inventoryWidget.getDynamicChildren();

        if (children == null)
        {
            return new ArrayList<>();
        }

        Collection<CustomItem> customItems = new ArrayList<>();
        for (Widget item : children)
        {
            customItems.add(createCustomItem(item));
        }

        return customItems;
    }

    public List<CustomItem> getCustomItems(Collection<Integer> ignoreList)
    {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        List<CustomItem> matchedItems = new ArrayList<>();

        if (inventoryWidget != null)
        {
            Collection<CustomItem> items = getCustomItems();
            for (CustomItem item : items) {
                if (!ignoreList.contains(item.getId()))
                {
                    matchedItems.add(item);
                }
            }
            return matchedItems;
        }
        return null;
    }

}
