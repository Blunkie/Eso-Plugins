package net.unethicalite.plugins.packetutils;

import net.unethicalite.plugins.packetutils.packets.WidgetPackets;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.ClientUI;
import org.pf4j.Extension;

import javax.inject.Inject;
import javax.swing.*;

import static net.runelite.client.externalplugins.ExternalPluginManager.pluginManager;

@Slf4j
@Extension
@Singleton
@PluginDescriptor(
        name = "Packet Utils",
        description = ""
)
public class PacketUtilsPlugin extends Plugin {
    @Inject
    Client client;
    @Inject
    WidgetPackets widgetPacket;
    @Inject
    PacketReflection packetReflection;
    @Inject
    ClientThread thread;
    public static final int CLIENT_REV = 206;
    private static boolean loaded = false;
    @Inject
    private PacketUtilsConfig config;

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN && !loaded) {
            loaded = packetReflection.LoadPackets();
        }
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
            loaded = false;
        }
        if (event.getGameState() == GameState.HOPPING) {
            loaded = false;
        }
        if (event.getGameState() == GameState.CONNECTION_LOST) {
            loaded = false;
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    @Provides
    public PacketUtilsConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(PacketUtilsConfig.class);
    }

    @Override
    @SneakyThrows
    public void startUp() {
        if (client.getRevision() != CLIENT_REV) {
            log.warn("PacketUtils is not compatible with this client version. Please update to the latest version.");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(ClientUI.getFrame(), "PacketUtils not updated for this rev please wait for plugin update");
                try {
                    pluginManager.setPluginEnabled(this, false);
                    pluginManager.stopPlugin(this);
                } catch (PluginInstantiationException ignored) {
                }
            });
            return;
        }
        thread.invoke(() ->
        {
            if (client.getGameState() != null && client.getGameState() == GameState.LOGGED_IN) {
                loaded = packetReflection.LoadPackets();
            }
        });
    }

    @Override
    public void shutDown() {
        log.info("Shutdown");
        loaded = false;
    }
}
