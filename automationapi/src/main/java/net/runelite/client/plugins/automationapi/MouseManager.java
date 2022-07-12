package net.runelite.client.plugins.automationapi;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;

@Slf4j
@Singleton
public class MouseManager
{
    @Inject
    private Client client;

    @Inject
    private ExecutorService executorService;

    @Inject
    private CalculationManager calculationManager;

    private void mouseEvent(int id, Point p)
    {
        MouseEvent e = new MouseEvent(
                client.getCanvas(), id,
                System.currentTimeMillis(),
                0, p.getX(), p.getY(),
                1, false, 1
        );
        client.getCanvas().dispatchEvent(e);
    }

    public void click(Rectangle r)
    {
        assert !client.isClientThread();

        Point p = getClickPoint(r);
        click(p);
    }

    public void click(Point p)
    {
        assert !client.isClientThread();

        mouseEvent(501, p);
        mouseEvent(502, p);
        mouseEvent(500, p);
    }

    public Point getClickPoint(Rectangle r)
    {
        int x = (int) (r.getX() + calculationManager.getRandom((int) r.getWidth() / 6 * -1, (int) r.getWidth() / 6) + r.getWidth() / 2);
        int y = (int) (r.getY() + calculationManager.getRandom((int) r.getHeight() / 6 * -1, (int) r.getHeight() / 6) + r.getHeight() / 2);

        return new Point(x, y);
    }

    public void handleMouseClick(Rectangle r)
    {
        Point p = getClickPoint(r);
        handleMouseClick(p);
    }

    public void handleMouseClick(Point p)
    {
        int viewportHeight = client.getViewportHeight();
        int viewportWidth = client.getViewportWidth();

        Widget minimapWidget = client.getWidget(164, 20);

        if (minimapWidget != null && minimapWidget.getBounds().contains(p.getX(), p.getY()))
        {
            p = new Point(0, 0);
        }

        if (p.getX() > viewportWidth || p.getY() > viewportHeight || p.getX() < 0 || p.getY() < 0)
        {
            p = new Point(client.getCenterX() + calculationManager.getRandom(-95, 95), client.getCenterY() + calculationManager.getRandom(-95, 95));
        }

        if (!client.isClientThread())
        {
            click(p);
            return;
        }

        Point point = p;

        executorService.submit(() -> click(point));
    }

    public void clickRandomPointCenter(int min, int max)
    {
        assert !client.isClientThread();

        Point p = new Point(client.getCenterX() + calculationManager.getRandom(min, max), client.getCenterY() + calculationManager.getRandom(min, max));
        handleMouseClick(p);
    }

    public void delayMouseClick(Point p, long delay) {
        executorService.submit(() ->
        {
            try {
                Game.sleep(delay);
                handleMouseClick(p);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        });
    }

    public void delayMouseClick(Rectangle r, long delay) {
        Point point = getClickPoint(r);
        delayMouseClick(point, delay);
    }
}
