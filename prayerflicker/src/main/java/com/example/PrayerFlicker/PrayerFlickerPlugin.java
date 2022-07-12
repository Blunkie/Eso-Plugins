package com.example.PrayerFlicker;

import net.unethicalite.plugins.packetutils.PacketUtilsPlugin;
import net.unethicalite.plugins.packetutils.packets.MousePackets;
import net.unethicalite.plugins.packetutils.packets.WidgetPackets;
import com.google.inject.Inject;
import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.util.HotkeyListener;
import org.pf4j.Extension;

import javax.swing.*;

import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

@PluginDependency(PacketUtilsPlugin.class)
@PluginDescriptor(
        name = "Prayer Flicker",
        enabledByDefault = false
)
@Extension
@Slf4j
public class PrayerFlickerPlugin extends Plugin {
    @Inject
    Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private KeyManager keyManager;

    @Inject
    private PrayerFlickerConfig config;

    @Inject
    WidgetPackets widgetPackets;

    @Inject
    MousePackets mousePackets;

    private final int quickPrayerWidgetID = WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getPackedId();

    @Provides
    public PrayerFlickerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PrayerFlickerConfig.class);
    }

    private void togglePrayer() {
        mousePackets.queueClickPacket();
        widgetPackets.queueWidgetActionPacket(1, quickPrayerWidgetID, -1, -1);
    }

    @Override
    @SneakyThrows
    public void startUp() {
        if (client.getRevision() != PacketUtilsPlugin.CLIENT_REV) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ClientUI.getFrame(), "Prayer flicker out of date");
                try {
                    pluginManager.setPluginEnabled(this, false);
                    pluginManager.stopPlugin(this);
                } catch (PluginInstantiationException ignored) {
                }
            });
            return;
        }
        keyManager.registerKeyListener(prayerToggle);
    }

    @Override
    public void shutDown() {
        keyManager.unregisterKeyListener(prayerToggle);
        toggle = false;
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        clientThread.invoke(() ->
        {
            if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1) {
                togglePrayer();
            }
        });
    }

    boolean toggle;

    public void switchAndUpdatePrayers(int i) {
        mousePackets.queueClickPacket();
        widgetPackets.queueWidgetActionPacket(1, WidgetInfo.QUICK_PRAYER_PRAYERS.getId(), -1, i);
        togglePrayer();
        togglePrayer();
    }

    public void updatePrayers() {
        togglePrayer();
        togglePrayer();
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (toggle) {
            if (event.getParam1() == WidgetInfo.QUICK_PRAYER_PRAYERS.getId()) {
                if (event.getMenuOption().equals("Quick Prayer Update")) {
                    updatePrayers();
                    event.consume();
                    return;
                }
                event.consume();
                switchAndUpdatePrayers(event.getParam0());
            }
        }
        if (config.minimapToggle() && event.getId() == 1 && event.getParam1() == WidgetInfo.MINIMAP_QUICK_PRAYER_ORB.getId()) {
            toggleFlicker();
            event.consume();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }

        if (toggle) {
            if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1) {
                togglePrayer();
            }
            togglePrayer();
        }
    }

    private final HotkeyListener prayerToggle = new HotkeyListener(() -> config.toggle()) {
        @Override
        public void hotkeyPressed() {
            toggleFlicker();
        }
    };

    public void toggleFlicker() {
        toggle = !toggle;
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        if (!toggle) {
            clientThread.invoke(() ->
            {
                if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1) {
                    togglePrayer();
                }
            });
        }
    }

    public void toggleFlicker(boolean on) {
        toggle = on;
        if (client.getGameState() != GameState.LOGGED_IN) {
            return;
        }
        if (!toggle) {
            clientThread.invoke(() ->
            {
                if (client.getVarbitValue(Varbits.QUICK_PRAYER) == 1) {
                    togglePrayer();
                }
            });
        }
    }
}
