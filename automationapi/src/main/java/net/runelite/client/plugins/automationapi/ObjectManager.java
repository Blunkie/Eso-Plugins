package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.GameObjectQuery;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class ObjectManager
{
    @Inject
    private Client client;

    public GameObject findGameObject(int... ids)
    {
        assert client.isClientThread();

        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public GameObject findGameObjectByAction(String action)
    {
        assert client.isClientThread();

        return new GameObjectQuery()
                .filter(o -> ArrayUtils.contains(client.getObjectDefinition(o.getId()).getActions(), action))
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public GameObject findGameObjectAtLocation(WorldPoint worldPoint)
    {
        assert client.isClientThread();

        return new GameObjectQuery()
                .atWorldLocation(worldPoint)
                .result(client)
                .first();
    }

    public GameObject findGameObjectWithin(WorldPoint from, int range, List<Integer> ids)
    {
        assert client.isClientThread();

        return new GameObjectQuery()
                .isWithinDistance(from, range)
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
}
