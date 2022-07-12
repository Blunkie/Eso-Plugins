package net.unethicalite.plugins.esonmz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("EsoNmzConfig")
public interface EsoNmzConfig extends Config
{
    @ConfigItem(
            keyName = "useSpec",
            name = "Use Spec",
            description = "Enable to use spec weapons",
            position = 0
    )
    default boolean useSpec()
    {
        return false;
    }

    @ConfigItem(
            keyName = "specWeapon",
            name = "Spec Weapon",
            description = "Choose your spec weapon",
            hidden = true,
            unhide = "useSpec",
            position = 1
    )
    default SpecWeapon specWeapon()
    {
        return SpecWeapon.GRANITE_MAUL;
    }

    @ConfigItem(
            keyName = "primaryWeapon",
            name = "Primary Weapon",
            description = "Main-hand weapon to switch back to",
            hidden = true,
            unhide = "useSpec",
            position = 2
    )
    default PrimaryWeapon primaryWeapon()
    {
        return PrimaryWeapon.MAGIC_SHORTBOW;
    }

    @AllArgsConstructor
    @Getter
    enum SpecWeapon
    {
        GRANITE_MAUL(ItemID.GRANITE_MAUL_24225),
        GRANITE_HAMMER(ItemID.GRANITE_HAMMER),
        MAGIC_SHORTBOW(ItemID.MAGIC_SHORTBOW_I);

        private final int specWeaponId;
    }

    @AllArgsConstructor
    @Getter
    enum PrimaryWeapon
    {
        GRANITE_HAMMER(ItemID.GRANITE_HAMMER),
        MAGIC_SHORTBOW(ItemID.MAGIC_SHORTBOW_I);

        private final int primaryWeaponId;
    }
}
