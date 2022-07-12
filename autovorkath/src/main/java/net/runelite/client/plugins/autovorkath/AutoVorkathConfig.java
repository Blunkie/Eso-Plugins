package net.runelite.client.plugins.autovorkath;

import net.runelite.client.config.*;

@ConfigGroup("AutoVorkathConfig")
public interface AutoVorkathConfig extends Config
{
    @ConfigItem(
            keyName = "startButton",
            name = "Start/Stop",
            description = "",
            position = 0
    )
    default Button startButton()
    {
        return new Button();
    }

    @ConfigSection(
            keyName = "sleepSection",
            name = "Sleeps",
            description = "",
            position = 1,
            closedByDefault = true
    )
    String sleepSection = "sleepSection";

    @ConfigItem(
            keyName = "minSleep",
            name = "Min",
            description = "",
            position = 2,
            section = "sleepSection"
    )
    default int minSleep()
    {
        return 25;
    }

    @ConfigItem(
            keyName = "maxSleep",
            name = "Max",
            description = "",
            position = 3,
            section = "sleepSection"
    )
    default int maxSleep()
    {
        return 65;
    }

    @ConfigSection(
            keyName = "generalSection",
            name = "General",
            description = "",
            position = 4,
            closedByDefault = true
    )
    String generalSection = "generalSection";

    @ConfigItem(
            keyName = "healthAmount",
            name = "Eat at",
            description = "",
            position = 5,
            section = "generalSection"
    )
    default int healthAmount()
    {
        return 40;
    }

    @ConfigSection(
            keyName = "lootSection",
            name = "Loot",
            description = "",
            position = 6,
            closedByDefault = true
    )
    String lootSection = "lootSection";

    @ConfigItem(
            keyName = "lootList",
            name = "List",
            description = "",
            position = 7,
            section = "lootSection"
    )
    default String lootList()
    {
        return "";
    }
}