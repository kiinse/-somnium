package kiinse.plugin.somnium.util;

import kiinse.plugin.somnium.api.AFKProvider;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.provider.DefaultAFKProvider;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerManager implements Listener {
    private final Map<UUID, Instant> cooldowns;
    private final Set<AFKProvider> andedProviders;
    private final Set<AFKProvider> oredProviders;
    private final DefaultAFKProvider defaultProvider;

    public PlayerManager(@NotNull Somnium somnium) {
        this.cooldowns = new HashMap<>();
        this.andedProviders = new HashSet<>();
        this.oredProviders = new HashSet<>();
        this.defaultProvider = new DefaultAFKProvider(somnium);

        updateListeners();
    }

    public Instant getCooldown(@NotNull Player player) {
        return cooldowns.getOrDefault(player.getUniqueId(), Instant.MIN);
    }

    public void setCooldown(@NotNull Player player, Instant cooldown) {
        cooldowns.put(player.getUniqueId(), cooldown);
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }

    public boolean isAfk(@NotNull Player player) {
        if(oredProviders.isEmpty() && andedProviders.isEmpty()){
            return defaultProvider.isAFK(player);
        }
        return oredProviders.stream().anyMatch(provider -> provider.isAFK(player)) ||
                (!andedProviders.isEmpty() && andedProviders.stream().allMatch(provider -> provider.isAFK(player)));
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        var uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
    }

    private void updateListeners() {
        if (andedProviders.isEmpty() && oredProviders.isEmpty()) {
            defaultProvider.enableListeners();
        } else {
            defaultProvider.disableListeners();
        }
    }
}
