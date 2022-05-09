package kiinse.plugin.somnium.util;

import com.google.common.base.Enums;
import kiinse.plugin.somnium.files.config.Config;
import kiinse.plugin.somnium.files.messages.Message;
import kiinse.plugins.api.darkwaterapi.files.filemanager.YamlFile;
import kiinse.plugins.api.darkwaterapi.files.locale.interfaces.PlayerLocale;
import kiinse.plugins.api.darkwaterapi.files.messages.interfaces.Messages;
import kiinse.plugins.api.darkwaterapi.utilities.PlayerUtilsImpl;
import kiinse.plugins.api.darkwaterapi.utilities.UtilsImpl;
import kiinse.plugins.api.darkwaterapi.utilities.interfaces.PlayerUtils;
import kiinse.plugins.api.darkwaterapi.utilities.interfaces.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.Somnium;

import java.util.*;

public class SomniumMessages implements Listener {

    private final Somnium somnium;
    private final Messages messages;
    private final Utils utils;
    private final PlayerLocale locales;
    private final PlayerUtils playerUtils;
    private final YamlFile config;
    private final HashMap<UUID, BossBar> bossBars;
    private final boolean papiPresent;

    public SomniumMessages(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.locales = somnium.getDarkWaterAPI().getLocales();
        this.messages = somnium.getMessages();
        this.utils = new UtilsImpl();
        this.playerUtils = new PlayerUtilsImpl();
        this.config = somnium.getConfiguration();
        this.bossBars = new HashMap<>();
        this.papiPresent = somnium.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        for (var world : Bukkit.getWorlds()) {
            if (somnium.getChecker().isBlacklisted(world)) {
                return;
            }
            registerBar(world);
        }
    }

    public void sendWorldChatMessage(@NotNull World world, @NotNull Message messagePath) {
        if (!config.getBoolean(Config.MESSAGES_CHAT_ENABLED)) {
            return;
        }
        for (var player : world.getPlayers()) {
            player.sendMessage(prepareMessage(world, messages.getStringMessageWithPrefix(locales.getPlayerLocale(player), messagePath)));
        }
    }

    public void sendWorldChatMessage(@NotNull World world, @NotNull Player who, @NotNull Message messagePath) {
        if (!config.getBoolean(Config.MESSAGES_CHAT_ENABLED)) {
            return;
        }
        for (var player : world.getPlayers()) {
            player.sendMessage(prepareMessage(world, prepareMessage(who, messages.getStringMessageWithPrefix(locales.getPlayerLocale(player), messagePath))));
        }
    }

    public void sendActionBarMessage(@NotNull World world, @NotNull Message messagePath) {
        if (!config.getBoolean(Config.MESSAGES_ACTIONBAR_ENABLED)) {
            return;
        }
        for (var player : world.getPlayers()) {
            playerUtils.sendActionBar(player, prepareMessage(world, messages.getStringMessageWithPrefix(locales.getPlayerLocale(player), messagePath)));
        }
    }

    public void sendBossBarMessage(@NotNull World world, @NotNull String message, @NotNull String color, double percentage) {
        if (!config.getBoolean(Config.MESSAGES_BOSSBAR_ENABLED)) {
            return;
        }

        var bar = bossBars.get(world.getUID());

        if (bar == null) {
            return;
        }

        if (percentage == 0) {
            bar.removeAll();
            return;
        }

        bar.setTitle(somnium.getMsg().prepareMessage(world, message));
        bar.setColor(Enums.getIfPresent(BarColor.class, color).or(BarColor.BLUE));
        bar.setProgress(percentage);
        world.getPlayers().forEach(bar::addPlayer);
    }

    @NotNull
    public String prepareMessage(@NotNull World world, @NotNull String message) {
        var checker = somnium.getChecker();
        return utils.replaceWord(message, new String[]{
                "{SLEEPING}:" +  checker.getSleepingPlayers(world).size(),
                "{PLAYERS}:" + checker.getPlayers(world),
                "{NEEDED}:" + checker.getSkipAmount(world),
                "{MORE}:" + checker.getNeeded(world)
        });
    }

    @NotNull
    public String prepareMessage(@NotNull Player player, @NotNull String message) {
        var output = utils.replaceWord(message, new String[]{
                "{PLAYER}:" +  player.getName(),
                "{DISPLAYNAME}:" + playerUtils.getPlayerName(player),
        });

        if (papiPresent) {
            output = PlaceholderAPI.setPlaceholders(player, output);
        }

        return output;
    }

    private void registerBar(@NotNull World world) {
        bossBars.computeIfAbsent(world.getUID(), uuid -> Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID));
    }

    public void clearBar(@NotNull World world) {
        Optional.ofNullable(bossBars.get(world.getUID())).ifPresent(BossBar::removeAll);
    }

    @EventHandler
    public void onWorldLoad(@NotNull WorldLoadEvent event) {
        registerBar(event.getWorld());
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        Optional.ofNullable(bossBars.get(event.getFrom().getUID())).ifPresent(bossBar -> bossBar.removePlayer(event.getPlayer()));
    }
}
