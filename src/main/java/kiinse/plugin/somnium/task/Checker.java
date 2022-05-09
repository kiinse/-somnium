package kiinse.plugin.somnium.task;

import kiinse.plugin.somnium.api.ExclusionProvider;
import kiinse.plugin.somnium.files.config.Config;
import kiinse.plugin.somnium.files.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.provider.GameModeExclusionProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public class Checker extends BukkitRunnable {
    private final Set<ExclusionProvider> providers;
    private final Somnium somnium;
    private final Set<UUID> skippingWorlds;

    public Checker(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.skippingWorlds = new HashSet<>();
        this.providers = new HashSet<>();
        var config = somnium.getConfiguration();
        providers.add(new GameModeExclusionProvider(somnium));
        providers.add(player -> config.getBoolean(Config.EXCLUSIONS_IGNORED_PERMISSION) && player.hasPermission("somnium.ignored"));
        providers.add(player -> config.getBoolean(Config.EXCLUSIONS_EXCLUDE_VANISHED) && isVanished(player));
        providers.add(player -> config.getBoolean(Config.EXCLUSIONS_EXCLUDE_AFK) && somnium.getPlayerManager().isAfk(player));
        int interval = somnium.getConfiguration().getInt(Config.INTERVAL);
        if (interval <= 0)
            interval = 1;
        runTaskTimerAsynchronously(somnium, 0L, interval * 20L);
    }

    @Override
    public void run() {
        Bukkit.getWorlds().stream()
                .filter(this::validateWorld)
                .forEach(this::checkWorld);
    }

    private boolean validateWorld(@NotNull World world) {
        return !skippingWorlds.contains(world.getUID())
                && !isBlacklisted(world)
                && isNight(world);
    }

    private void checkWorld(@NotNull World world) {
        var config = somnium.getConfiguration();
        var messages = somnium.getMsg();

        var sleeping = getSleepingPlayers(world).size();
        var needed = getNeeded(world);

        if (sleeping < 1) {
            messages.clearBar(world);
            return;
        }

        if (needed > 0) {
            var sleepingPercentage = Math.min(1, (double) sleeping / getSkipAmount(world));
            messages.sendActionBarMessage(world, Message.ACTIONBAR_PLAYERS_SLEEPING);
            messages.sendBossBarMessage(world, config.getString(Config.MESSAGES_BOSSBAR_SLEEPING_MESSAGE),
                    config.getString(Config.MESSAGES_BOSSBAR_SLEEPING_COLOR), sleepingPercentage);
        } else if (needed == 0) {
            messages.sendActionBarMessage(world, Message.ACTIONBAR_NIGHT_SKIPPING);
            messages.sendBossBarMessage(world, config.getString(Config.MESSAGES_BOSSBAR_SKIPPING_MESSAGE),
                    config.getString(Config.MESSAGES_BOSSBAR_SKIPPING_COLOR), 1);

            if (!config.getBoolean(Config.NIGHT_SKIP_ENABLED)) {
                return;
            }

            if (config.getBoolean(Config.NIGHT_SKIP_INSTANT_SKIP)) {
                Bukkit.getScheduler().runTask(somnium, () -> {
                    world.setTime(config.getInt(Config.NIGHT_SKIP_DAYTIME_TICKS));
                    clearWeather(world);
                    resetStatus(world);
                });
                return;
            }

            skippingWorlds.add(world.getUID());
            new AccelerateNightTask(somnium, this, world);
        }
    }

    private boolean isNight(@NotNull World world) {
        return world.getTime() > 12950 || world.getTime() < 23950;
    }

    public boolean isBlacklisted(@NotNull World world) {
        var blacklisted = somnium.getConfiguration().getStringList(Config.BLACKLISTED_WORLDS).contains(world.getName());
        return somnium.getConfiguration().getBoolean(Config.WHITELIST_MODE) != blacklisted;
    }

    public static boolean isVanished(@NotNull Player player) {
        return player.getMetadata("vanished").stream().anyMatch(MetadataValue::asBoolean);
    }

    public int getPlayers(@NotNull World world) {
        return Math.max(0, world.getPlayers().size() - getExcluded(world).size());
    }

    @NotNull
    public List<Player> getSleepingPlayers(@NotNull World world) {
        return world.getPlayers().stream()
                .filter(player -> player.getPose() == Pose.SLEEPING)
                .collect(toList());
    }

    public int getSkipAmount(@NotNull World world) {
        return (int) Math.ceil(getPlayers(world) * (somnium.getConfiguration().getDouble(Config.NIGHT_SKIP_PERCENTAGE) / 100));
    }

    public int getNeeded(@NotNull World world) {
        var percentage = somnium.getConfiguration().getDouble(Config.NIGHT_SKIP_PERCENTAGE);
        return Math.max(0, (int) Math.ceil((getPlayers(world)) * (percentage / 100) - getSleepingPlayers(world).size()));
    }

    @NotNull
    private List<Player> getExcluded(@NotNull World world) {
        return world.getPlayers().stream()
                .filter(this::isExcluded)
                .collect(toList());
    }

    private boolean isExcluded(@NotNull Player player) {
        return providers.stream().anyMatch(provider -> provider.isExcluded(player));
    }

    public boolean isSkipping(@NotNull World world) {
        return skippingWorlds.contains(world.getUID());
    }

    public void forceSkip(@NotNull World world) {
        skippingWorlds.add(world.getUID());
        new AccelerateNightTask(somnium, this, world);
    }

    public void resetStatus(@NotNull World world) {
        wakeUpPlayers(world);
        somnium.getServer().getScheduler().runTaskLater(somnium, () -> {
            skippingWorlds.remove(world.getUID());
            somnium.getPlayerManager().clearCooldowns();
            somnium.getMsg().sendWorldChatMessage(world, Message.SKIPPED);
        }, 20L);
    }

    public void wakeUpPlayers(@NotNull World world) {
        ensureMain(() -> world.getPlayers().stream()
                .filter(LivingEntity::isSleeping)
                .forEach(player -> player.wakeup(true)));
    }

    public void clearWeather(@NotNull World world) {
        ensureMain(() -> {
            var config = somnium.getConfiguration();

            if (world.hasStorm() && config.getBoolean(Config.NIGHT_SKIP_CLEAR_RAIN)) {
                world.setStorm(false);
            }

            if (world.isThundering() && config.getBoolean(Config.NIGHT_SKIP_CLEAR_THUNDER)) {
                world.setThundering(false);
            }
        });
    }

    public void ensureMain(@NotNull Runnable runnable) {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(somnium, runnable);
        } else {
            runnable.run();
        }
    }
}
