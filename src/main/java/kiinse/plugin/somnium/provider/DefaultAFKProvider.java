package kiinse.plugin.somnium.provider;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.api.AFKProvider;
import kiinse.plugin.somnium.files.config.Config;
import kiinse.plugin.somnium.listener.AfkListener;
import kiinse.plugins.api.darkwaterapi.files.filemanager.YamlFile;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class DefaultAFKProvider implements AFKProvider, Listener {
    private final boolean enabled;
    private Map<UUID, Instant> playerActivity;
    private final AfkListener listener;
    private final YamlFile config;
    private final int timeout;
    private final Somnium somnium;

    public DefaultAFKProvider(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.config = somnium.getConfiguration();
        enabled = (config.getBoolean(Config.AFK_DETECTION_ENABLED));
        if (enabled) {
            timeout = config.getInt(Config.AFK_DETECTION_TIMEOUT);
            listener = new AfkListener(this);
            enableListeners();
        } else {
            somnium.getLogger().info("Not registering fallback AFK detection system.");
            listener = null;
            timeout = -1;
        }
    }

    @Override
    public boolean isAFK(Player player) {
        if (!enabled || !playerActivity.containsKey(player.getUniqueId())) {
            return false;
        }

        long minutes = playerActivity.get(player.getUniqueId()).until(Instant.now(), ChronoUnit.MINUTES);
        return minutes >= timeout;
    }

    public void updateActivity(@NotNull Player player) {
        playerActivity.put(player.getUniqueId(), Instant.now());
    }

    public void enableListeners() {
        if (enabled) {
            somnium.getLogger().log(Level.FINE, "Enabling listeners for Default AFK Provider");
            playerActivity = new HashMap<>();
            listener.start();
        }
    }

    public void disableListeners() {
        if (enabled) {
            somnium.getLogger().log(Level.FINE, "Disabling listeners for Default AFK Provider");
            listener.stop();
            playerActivity = null;
        }
    }


    public void removePlayer(UUID uniqueId) {
        playerActivity.remove(uniqueId);
    }

    @NotNull
    public Somnium getSomnium() {
        return somnium;
    }
}
