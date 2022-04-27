package kiinse.plugin.somnium.listener;

import kiinse.plugin.somnium.Somnium;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.provider.DefaultAFKProvider;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class AfkListener implements Listener {
    private final DefaultAFKProvider afkProvider;
    private Queue<AfkPlayer> players;
    private PlayerMovementChecker movementChecker;
    private final Somnium somnium;
    private boolean status;

    public AfkListener(@NotNull DefaultAFKProvider afkProvider) {
        this.afkProvider = afkProvider;
        this.somnium = afkProvider.getSomnium();
        somnium.getLogger().info("Initializing fallback AFK detection system. Fallback AFK system is not enabled at this time");
        status = false;
    }

    public void start() {
        if(!status) {
            status = true;
            players = new ArrayDeque<>();
            movementChecker = new PlayerMovementChecker();
            players.addAll(Bukkit.getOnlinePlayers().stream().map((Function<Player, AfkPlayer>) AfkPlayer::new).collect(Collectors.toSet()));
            Bukkit.getServer().getPluginManager().registerEvents(this, somnium);
            movementChecker.runTaskTimer(somnium, 0, 1);
            somnium.getLogger().info("Fallback AFK detection system is enabled");
        } else {
            somnium.getLogger().info("Fallback AFK detection system was already enabled");
        }
    }

    public void stop() {
        if(status) {
            status = false;
            movementChecker.cancel();
            HandlerList.unregisterAll(this);
            players = null;
            somnium.getLogger().info("Fallback AFK detection system is disabled");
        } else {
            somnium.getLogger().info("Fallback AFK detection system was already disabled");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        afkProvider.updateActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        afkProvider.updateActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        afkProvider.updateActivity((Player) event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        players.add(new AfkPlayer(event.getPlayer()));
        afkProvider.updateActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        players.remove(new AfkPlayer(event.getPlayer()));
        afkProvider.removePlayer(event.getPlayer().getUniqueId());
    }

    private final class PlayerMovementChecker extends BukkitRunnable {
        private double checksToMake = 0;
        @Override
        public void run() {
            if(players.isEmpty()){
                checksToMake = 0;
                return;
            }
            for (checksToMake += players.size() / 20D; checksToMake > 0 && !players.isEmpty(); checksToMake--) {
                AfkPlayer afkPlayer = players.poll();
                if (afkPlayer.changed()) {
                    afkProvider.updateActivity(afkPlayer.player);
                }
                players.add(afkPlayer);
            }
        }
    }

    private static final class AfkPlayer {
        private final Player player;
        private int locationHash;

        public AfkPlayer(Player player) {
            this.player = player;
            locationHash = player.getEyeLocation().hashCode();
        }

        boolean changed() {
            int previousLocation = locationHash;
            locationHash = player.getEyeLocation().hashCode();
            return previousLocation != locationHash;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AfkPlayer afkPlayer = (AfkPlayer) o;
            return player.getUniqueId().equals(afkPlayer.player.getUniqueId());
        }

        @Override
        public int hashCode() {
            return player.getUniqueId().hashCode();
        }
    }
}
