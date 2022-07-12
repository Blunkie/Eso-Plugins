package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class MenuManager
{
    @Inject
    private Client client;

    public NewMenuEntry menuEntry;

    public void setEntry(NewMenuEntry menuEntry)
    {
        this.menuEntry = menuEntry;
    }

    public void setSelectedSpell(WidgetInfo info)
    {
        Widget widget = client.getWidget(info);

        if (widget != null)
        {
            client.setSelectedSpellWidget(widget.getId());
            client.setSelectedSpellChildIndex(-1);
        }
    }
}
