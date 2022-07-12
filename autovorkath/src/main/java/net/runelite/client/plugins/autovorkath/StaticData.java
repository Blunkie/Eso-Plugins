package net.runelite.client.plugins.autovorkath;

import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.automationapi.NewMenuEntry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static net.runelite.api.ItemID.*;
import static net.runelite.api.ItemID.RING_OF_DUELING8;
import static net.runelite.api.NpcID.*;

public class StaticData
{
    public static NewMenuEntry menuEntry;

    public static boolean startBot;
    public static boolean dodgeBomb;
    public static boolean dodgeAcid;
    public static boolean killSpawn;
    public static boolean doneBanking;
    public static boolean doneDeposit;
    public static boolean doneWithdrawingPotions;
    public static boolean doneWithdrawingAll;
    public static boolean widgetVisible;

    public static final Collection<Integer> ANTI_VENOM_POTIONS = Set.of(
            ANTIVENOM4_12913,
            ANTIVENOM3_12915,
            ANTIVENOM2_12917,
            ANTIVENOM1_12919
    );

    public static final Collection<Integer> ANTI_FIRE_POTIONS = Set.of(
            EXTENDED_SUPER_ANTIFIRE4,
            EXTENDED_SUPER_ANTIFIRE3,
            EXTENDED_SUPER_ANTIFIRE2,
            EXTENDED_SUPER_ANTIFIRE1
    );

    public static final Collection<Integer> DIVINE_RANGING_POTIONS = Set.of(
            DIVINE_RANGING_POTION1,
            DIVINE_RANGING_POTION2,
            DIVINE_RANGING_POTION3,
            DIVINE_RANGING_POTION4
    );

    public static final Collection<Integer> RINGS_OF_DUELLING = Set.of(
            RING_OF_DUELING1, RING_OF_DUELING2,
            RING_OF_DUELING3, RING_OF_DUELING4,
            RING_OF_DUELING5, RING_OF_DUELING6,
            RING_OF_DUELING7, RING_OF_DUELING8
    );

    public static List<WorldPoint> acidSpots;
    public static List<WorldPoint> safeTiles;

    public static List<String> lootList;

    public static HashMap<Integer, Integer> itemValues;

    public static WorldPoint bombWorldPoint;
    public static WorldPoint safeTile;

    public static LocalPoint baseTile;

    public static WorldArea RELLEKKA;

    public static final int VORKATH_AWAKE_ID = VORKATH_8061;
    public static final int VORKATH_ASLEEP_ID = VORKATH_8059;
    public static final int VORKATH_WAKING_ID = VORKATH_8058;

    public static int healthAmount;
    public static int tickDelay;
}
