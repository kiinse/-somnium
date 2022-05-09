package kiinse.plugin.somnium.task;

import kiinse.plugin.somnium.files.config.Config;
import kiinse.plugin.somnium.files.messages.Message;
import kiinse.plugins.api.darkwaterapi.files.filemanager.YamlFile;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.Somnium;

public class AccelerateNightTask extends BukkitRunnable {

    private final Somnium somnium;
    private final Checker checker;
    private final World world;

    public AccelerateNightTask(@NotNull Somnium somnium, @NotNull Checker checker, @NotNull World world) {
        this.somnium = somnium;
        this.checker = checker;
        this.world = world;
        somnium.getMsg().sendWorldChatMessage(world, Message.ACTIONBAR_NIGHT_SKIPPING);
        checker.clearWeather(world);
        runTaskTimer(somnium, 1, 1);
    }

    @Override
    public void run() {
        YamlFile config = somnium.getConfiguration();
        long time = world.getTime();
        double timeRate = config.getInt(Config.NIGHT_SKIP_TIME_RATE);
        int dayTime = Math.max(150, config.getInt(Config.NIGHT_SKIP_DAYTIME_TICKS));
        int sleeping = checker.getSleepingPlayers(world).size();

        if (config.getBoolean(Config.NIGHT_SKIP_PROPORTIONAL_ACCELERATION)) {
            timeRate = Math.min(timeRate, Math.round(timeRate / world.getPlayers().size() * Math.max(1, sleeping)));
        }

        if (time >= (dayTime - timeRate * 1.5) && time <= dayTime) {
            if (config.getBoolean(Config.NIGHT_SKIP_CLEAR_PHANTOM_STATISTIC)) {
                world.getPlayers().forEach(player -> player.setStatistic(Statistic.TIME_SINCE_REST, 0));
            }

            checker.resetStatus(world);
            cancel();
            return;
        }

        world.setTime(time + (int) timeRate);
    }
}
