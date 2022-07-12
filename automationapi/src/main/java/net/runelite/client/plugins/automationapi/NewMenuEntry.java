package net.runelite.client.plugins.automationapi;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.MenuAction;

@Getter
@Setter
public class NewMenuEntry
{
    private String option;
    private String target;
    private int id;
    private MenuAction menuAction;
    private int opCode;
    private int param0;
    private int param1;
    private boolean forceLeftClick;

    public NewMenuEntry(String option, String target, int id, MenuAction menuAction, int param0, int param1, boolean forceLeftClick)
    {
        this.option = option;
        this.target = target;
        this.id = id;
        this.menuAction = menuAction;
        this.param0 = param0;
        this.param1 = param1;
        this.forceLeftClick = forceLeftClick;
    }

    public NewMenuEntry(String option, String target, int id, int opCode, int param0, int param1, boolean forceLeftClick)
    {
        this.option = option;
        this.target = target;
        this.id = id;
        this.opCode = opCode;
        this.param0 = param0;
        this.param1 = param1;
        this.forceLeftClick = forceLeftClick;
    }

    public int getOpCode()
    {
        if (menuAction != null) {
            return menuAction.getId();
        } else {
            return opCode;
        }
    }
}
