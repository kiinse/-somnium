package kiinse.plugin.somnium.listener;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.files.config.Config;
import kiinse.plugin.somnium.files.messages.Message;
import kiinse.plugin.somnium.task.Checker;
import kiinse.plugin.somnium.util.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.jetbrains.annotations.NotNull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class BedListener implements Listener {

    private final Somnium somnium;
    private final PlayerManager playerManager;

    public BedListener(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.playerManager = somnium.getPlayerManager();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();
        if (isMessageSilenced(player)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(somnium, () -> {
            playerManager.setCooldown(player, Instant.now());
            somnium.getMsg().sendWorldChatMessage(event.getBed().getWorld(), player, Message.PLAYER_SLEEPING);
        }, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBedLeave(PlayerBedLeaveEvent event) {
        if (isMessageSilenced(event.getPlayer())) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(somnium, () -> {
            playerManager.setCooldown(event.getPlayer(), Instant.now());
            somnium.getMsg().sendWorldChatMessage(event.getBed().getWorld(), event.getPlayer(), Message.PLAYER_BED_LEFT);
        }, 1);
    }

    private boolean isMessageSilenced(@NotNull Player player) {
        if (somnium.getChecker().isSkipping(player.getWorld())) {
            return true;
        }

        if (Checker.isVanished(player)) {
            return true;
        }

        int cooldown = somnium.getConfiguration().getInt(Config.MESSAGES_CHAT_COOLDOWN);
        return playerManager.getCooldown(player).until(Instant.now(), ChronoUnit.MINUTES) < cooldown;
    }
}
