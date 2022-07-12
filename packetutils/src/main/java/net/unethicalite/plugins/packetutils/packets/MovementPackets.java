package net.unethicalite.plugins.packetutils.packets;

import net.unethicalite.plugins.packetutils.PacketDef;
import net.unethicalite.plugins.packetutils.PacketReflection;
import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;

public class MovementPackets {
    @Inject
    Client client;
    @Inject
    PacketReflection packetReflection;

    public void queueMovement(int worldPointX, int worldPointY, boolean ctrlDown) {
        int ctrl = ctrlDown ? 2 : 0;
        packetReflection.sendPacket(PacketDef.MOVE_GAMECLICK, worldPointX, worldPointY, ctrl, 5);
    }

    public void queueMovement(WorldPoint location) {
        queueMovement(location.getX(), location.getY(), false);
    }
}
