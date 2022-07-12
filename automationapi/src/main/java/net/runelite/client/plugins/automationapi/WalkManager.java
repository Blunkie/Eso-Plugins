package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.rs.api.RSClient;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class WalkManager
{
    @Inject
    private Client client;

    @Inject
    private Game game;

    @Inject
    private CalculationManager calculationManager;

    public static int x;
    public static int y;

    public void walkTile(int x, int y)
    {
        RSClient rsClient = (RSClient) client;
        rsClient.setSelectedSceneTileX(x);
        rsClient.setSelectedSceneTileY(y);
        rsClient.setViewportWalking(true);
        rsClient.setCheckClick(false);
    }

    public void sceneWalk(LocalPoint p, int random, long delay)
    {
        x = p.getSceneX() + calculationManager.getRandom(-Math.abs(random), Math.abs(random));
        y = p.getSceneY() + calculationManager.getRandom(-Math.abs(random), Math.abs(random));

        game.doAction(new NewMenuEntry("Walk here", "", 0, MenuAction.WALK, 0, 0, false), new Point(0, 0), delay);
    }

    public void sceneWalk(WorldPoint worldPoint, int random, long delay)
    {
        LocalPoint point = LocalPoint.fromWorld(client, worldPoint);

        if (point == null)
        {
            return;
        }

        sceneWalk(point, random, delay);
    }
}
