package net.runelite.client.plugins.automationapi;

import java.util.concurrent.ThreadLocalRandom;

public class CalculationManager
{
    public int getRandom(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1 );
    }
}
