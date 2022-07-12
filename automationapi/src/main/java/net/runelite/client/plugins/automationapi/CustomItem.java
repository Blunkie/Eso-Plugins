package net.runelite.client.plugins.automationapi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.runelite.api.widgets.Widget;

@AllArgsConstructor
@ToString
@Getter
public class CustomItem
{
    private final int id;

    private final int quantity;

    private final int index;

    private final Widget widget;
}
