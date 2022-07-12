package net.unethicalite.plugins.packetutils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Buffer;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class PacketReflection {
    @Inject
    Client client;
    @Inject
    public static Class classWithgetPacketBufferNode = null;
    public static Method getPacketBufferNode = null;
    public static Class ClientPacket = null;
    public static Field EVENT_MOUSE_CLICK = null;
    public static Class isaacClass = null;
    public static Class PacketBufferNode = null;
    public static Field PACKETWRITER = null;
    public static Object isaac = null;
    @SneakyThrows
    public boolean LoadPackets() {
        try
        {
                classWithgetPacketBufferNode = client.getClass().getClassLoader().loadClass(ObfuscatedNames.CLASSWITHGETPACKETBUFFERNODE);
                ClientPacket = client.getClass().getClassLoader().loadClass(ObfuscatedNames.CLIENTPACKETCLASS);

                EVENT_MOUSE_CLICK = ClientPacket.getDeclaredField(ObfuscatedNames.EVENT_MOUSE_CLICK);
                EVENT_MOUSE_CLICK.setAccessible(true);

                PACKETWRITER = client.getClass().getDeclaredField(ObfuscatedNames.PACKETWRITERCLIENTFIELD);
                PacketBufferNode = client.getClass().getClassLoader().loadClass(ObfuscatedNames.PACKETBUFFERNODECLASS);
                PACKETWRITER.setAccessible(true);
                Field isaac2 = PACKETWRITER.get(null).getClass().getDeclaredField(ObfuscatedNames.ISAACFIELD);
                isaac2.setAccessible(true);
                isaac = isaac2.get(PACKETWRITER.get(null));
                isaacClass = client.getClass().getClassLoader().loadClass(ObfuscatedNames.ISAACCLASS);
                getPacketBufferNode = Arrays.stream(classWithgetPacketBufferNode.getDeclaredMethods()).filter(m -> m.getReturnType().equals(PacketBufferNode)).collect(Collectors.toList()).get(0);
                getPacketBufferNode.setAccessible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.warn("Failed to load Packets Into Client");
            return false;
        }
        log.warn("Successfully loaded Packets Into Client");
        return true;
    }

    @SneakyThrows
    public void writeObject(String obfname, Buffer buffer, Object input) {
        Object convertedInput = input;

        Class inputType = String.class;
        if (input instanceof Integer) {
            int converter = (Integer) input;
            convertedInput = converter;
            inputType = int.class;
        }

        Integer garbageValue = ObfuscatedNames.BUFFER_METHODS.get(obfname);
        Integer garbageValueToCompare = Math.abs(garbageValue);

        int garbageValueInt = garbageValue;
        Object convertedGarbage = garbageValue;

        Class garbageType = null;
        if (garbageValueToCompare < 256) {
            garbageType = byte.class;
            convertedGarbage = garbageValue.byteValue();
        }
        if (garbageValueToCompare > 256 && garbageValueToCompare < 32768) {
            garbageType = short.class;
            convertedGarbage = (short) garbageValueInt;
        }
        if (garbageValueToCompare > 32768 && garbageValueToCompare < Integer.MAX_VALUE) {
            garbageType = int.class;
            convertedGarbage = garbageValueInt;
        }
        Method method = client.getClass().getClassLoader().loadClass(ObfuscatedNames.BUFFERCLASS).getDeclaredMethod(obfname,
                inputType, garbageType);
        method.invoke(buffer, convertedInput, convertedGarbage);
    }

    @SneakyThrows
    public void sendPacket(PacketDef def, Object... objects) {
        Object packetBufferNode = getPacketBufferNode.invoke(null, fetchPacketField(def.name).get(ClientPacket),
                isaac, ObfuscatedNames.PACKETBUFFERNODEGARBAGE);
        Buffer buffer = (net.runelite.api.Buffer) packetBufferNode.getClass().getDeclaredField(ObfuscatedNames.BUFFERFROMPACKETBUFFERNODE).get(packetBufferNode);
        List<String> params = null;
        if(def.type == PacketType.RESUME_PAUSEBUTTON){
            params = List.of("widgetId", "childId");
        }
        if (def.type == PacketType.IF_BUTTON) {
            params = List.of("widgetId", "childId", "itemId");
        }
        if (def.type == PacketType.OPLOC) {
            params = List.of("objectId", "worldPointX", "worldPointY", "ctrlDown");
        }
        if (def.type == PacketType.OPNPC) {
            params = List.of("npcIndex", "ctrlDown");
        }
        if (def.type == PacketType.OPPLAYER) {
            params = List.of("playerIndex", "ctrlDown");
        }
        if (def.type == PacketType.OPOBJ) {
            params = List.of("groundItemId", "worldPointX", "worldPointY", "ctrlDown");
        }
        if (def.type == PacketType.EVENT_MOUSE_CLICK) {
            params = List.of("mouseInfo", "x", "y");
        }
        if (def.type == PacketType.MOVE_GAMECLICK) {
            params = List.of("worldPointX", "worldPointY", "ctrlDown", "5");
        }
        if (def.type == PacketType.IF_BUTTONT) {
            params = List.of("sourceWidgetId", "sourceSlot", "sourceItemId", "destinationWidgetId",
                    "destinationSlot", "destinationItemId");
        }
        if (def.type == PacketType.OPLOCT) {
            params = List.of("objectId", "worldPointX", "worldPointY", "sourceSlot", "sourceItemId", "sourceWidgetId", "ctrlDown");
        }
        if (def.type == PacketType.OPPLAYERT) {
            params = List.of("playerIndex", "sourceItemId", "sourceSlot", "sourceWidgetId", "ctrlDown");
        }
        if (def.type == PacketType.OPNPCT) {
            params = List.of("npcIndex", "sourceItemId", "sourceSlot", "sourceWidgetId", "ctrlDown");
        }
        if (params != null) {
            for (Map.Entry<String, String> stringEntry : def.fields.entrySet()) {
                if (params.indexOf(stringEntry.getKey()) != -1) {
                    writeObject(stringEntry.getValue(), buffer, objects[params.indexOf(stringEntry.getKey())]);
                }
            }
            Method addNode = PACKETWRITER.get(null).getClass().getMethod(ObfuscatedNames.ADDNODE, PacketBufferNode, byte.class);
            addNode.setAccessible(true);
            addNode.invoke(PACKETWRITER.get(null), packetBufferNode, ObfuscatedNames.ADDNODEGARBAGE.byteValue());
        }
    }

    @SneakyThrows
    Field fetchPacketField(String name) {
        return ClientPacket.getDeclaredField(name);
    }
}