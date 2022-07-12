package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class PrayerManager
{
    @Inject
    private Client client;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private MenuManager menuManager;

    @Inject
    private ActionManager actionManager;

    @Inject
    private CalculationManager calculationManager;

    public int getPoints()
    {
        return client.getBoostedSkillLevel(Skill.PRAYER);
    }

    public boolean isQuickPrayerActive()
    {
        return client.getVarbitValue(Varbits.QUICK_PRAYER) == 1;
    }

    public void toggleQuickPrayer(boolean enabled)
    {
        Widget prayerOrb = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);

        if (prayerOrb == null)
        {
            return;
        }

        NewMenuEntry activate = new NewMenuEntry("Activate", "Quick-prayers", 1,  MenuAction.CC_OP, -1, prayerOrb.getId(), false);
        NewMenuEntry deactivate = new NewMenuEntry("Deactivate", "Quick-prayers", 1,  MenuAction.CC_OP, -1, prayerOrb.getId(), false);

        Runnable runnable = () ->
        {
            if (enabled) {
                menuManager.setEntry(activate);
            } else {
                menuManager.setEntry(deactivate);
            }
        };

        actionManager.delayAction(calculationManager.getRandom(15, 65), runnable);
    }
}
