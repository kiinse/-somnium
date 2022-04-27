package kiinse.plugin.somnium.util;

import com.google.common.base.Enums;
import kiinse.plugins.api.darkwaterapi.files.locale.LocaleUtils;
import kiinse.plugins.api.darkwaterapi.utilities.Utils;
import lombok.Getter;
import lombok.Setter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.json.JSONObject;

import java.util.*;

public class Messages implements Listener {

    @Getter
    @Setter
    private static JSONObject somniumMessages;

    private final Utils utils;
    private final Somnium somnium;
    private final Config config;
    private final Random random;
    private final HashMap<UUID, BossBar> bossBars;
    private final boolean papiPresent;

    public Messages(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.config = somnium.getConfiguration();
        this.random = new Random();
        this.bossBars = new HashMap<>();
        this.utils = new Utils(somnium);
        this.papiPresent = somnium.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");

        for (var world : Bukkit.getWorlds()) {
            if (somnium.getChecker().isBlacklisted(world)) {
                return;
            }
            registerBar(world);
        }
    }

    public void sendWorldChatMessage(@NotNull World world, @NotNull String messagePath) {
        if (!config.getBoolean("messages.chat.enabled")) {
            return;
        }
        for (var player : world.getPlayers()) {
            player.sendMessage(prepareMessage(world, utils.getColorizeMessageWithPrefix(getSomniumMessages(), LocaleUtils.getLocale(player), messagePath)));
        }
    }

    public void sendWorldChatMessage(@NotNull World world, @NotNull Player who, @NotNull String messagePath) {
        if (!config.getBoolean("messages.chat.enabled")) {
            return;
        }
        for (var player : world.getPlayers()) {
            player.sendMessage(prepareMessage(world, prepareMessage(who, utils.getColorizeMessageWithPrefix(getSomniumMessages(), LocaleUtils.getLocale(player), messagePath))));
        }
    }

    public void sendActionBarMessage(@NotNull World world, @NotNull String messagePath) {
        if (!config.getBoolean("messages.actionbar.enabled")) {
            return;
        }
        for (var player : world.getPlayers()) {
            utils.sendActionBar(player, prepareMessage(world, utils.getColorizeMessageWithPrefix(getSomniumMessages(), LocaleUtils.getLocale(player), messagePath)));
        }
    }

    public void sendRandomChatMessage(@NotNull World world, @NotNull String listLocation) {
        var messages = config.getStringList(listLocation);
        if (messages.size() < 1) {
            return;
        }
        sendWorldChatMessage(world, messages.get(random.nextInt(messages.size())));
    }

    public void sendBossBarMessage(@NotNull World world, @NotNull String message, @NotNull String color, double percentage) {
        if (!config.getBoolean("messages.bossbar.enabled")) {
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

        bar.setTitle(somnium.getMessages().prepareMessage(world, message));
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
                "{DISPLAYNAME}:" + utils.getPlayerName(player),
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
