package net.unethicalite.plugins.esomagic;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.unethicalite.api.magic.SpellBook;

@ConfigGroup("EsoMagicConfig")
public interface EsoMagicConfig extends Config
{
    @Getter
    @AllArgsConstructor
    enum CustomSpell
    {
        CURSE(SpellBook.Standard.CURSE, "Curse"),
        VULNERABILITY(SpellBook.Standard.VULNERABILITY, "Vulnerability"),
        ENFEEBLE(SpellBook.Standard.ENFEEBLE, "Enfeeble"),
        STUN(SpellBook.Standard.STUN, "Stun");

        private final SpellBook.Standard spell;
        private final String name;

        @Override
        public String toString()
        {
            return name;
        }
    }

    @ConfigItem(
            keyName = "spell",
            name = "Spell",
            description = "",
            position = 0
    )
    default CustomSpell spell()
    {
        return CustomSpell.CURSE;
    }

    @ConfigItem(
            keyName = "itemId",
            name = "Item",
            description = "Id of item to alch",
            position = 1
    )
    default int itemId()
    {
        return 0;
    }

    @ConfigItem(
            keyName = "npcId",
            name = "Npc",
            description = "Id of npc",
            position = 1
    )
    default int npcId()
    {
        return 0;
    }
}
