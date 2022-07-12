package net.runelite.client.plugins.automationapi;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Singleton
public class ActionManager
{
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    private final List<DelayedAction> delayedActions = new ArrayList<>();

    public void onGameTick(GameTick event)
    {
        runDelayedActions();
    }

    public void onClientTick(ClientTick event)
    {
        runDelayedActions();
    }

    public void runDelayedActions()
    {
        Iterator<DelayedAction> it = delayedActions.iterator();

        while (it.hasNext())
        {
            DelayedAction a = it.next();

            if (a.shouldRun.get())
            {
                a.runnable.run();
                it.remove();
            }
        }
    }

    public void runLater(Supplier<Boolean> condition, Runnable runnable)
    {
        clientThread.invoke(() ->
        {
            if (condition.get()) {
                runnable.run();
            } else {
                delayedActions.add(new DelayedAction(condition, runnable));
            }
            return true;
        });
    }

    public void delayAction(long delay, Runnable runnable)
    {
        long when = System.currentTimeMillis() + delay;
        runLater(() -> System.currentTimeMillis() >= when, runnable);
    }

    @Value
    public static class DelayedAction
    {
        Supplier<Boolean> shouldRun;
        Runnable runnable;
    }
}
