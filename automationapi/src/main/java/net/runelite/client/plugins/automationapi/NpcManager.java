package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.queries.NPCQuery;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Slf4j
@Singleton
public class NpcManager
{
    @Inject
    private Client client;

    public NPC findNpc(int... ids)
    {
        assert client.isClientThread();

        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public NPC findNpc(String name)
    {
        assert client.isClientThread();

        return new NPCQuery()
                .nameEquals(name)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public NPC findNpcWithin(WorldPoint from, int range, List<Integer> ids)
    {
        assert client.isClientThread();

        return new NPCQuery()
                .idEquals(ids)
                .isWithinDistance(from, range)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
}
