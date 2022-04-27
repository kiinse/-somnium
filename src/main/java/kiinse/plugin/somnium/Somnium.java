package kiinse.plugin.somnium;

import kiinse.plugin.somnium.command.TabComplete;
import kiinse.plugin.somnium.files.LoadMessagesJson;
import kiinse.plugin.somnium.task.Checker;
import kiinse.plugin.somnium.util.DarkWaterLoader;
import kiinse.plugin.somnium.util.Messages;
import kiinse.plugin.somnium.util.PlayerManager;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.CommandManager;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.command.ForceSkipCommand;
import kiinse.plugin.somnium.command.HarborCommand;
import kiinse.plugin.somnium.listener.BedListener;
import kiinse.plugin.somnium.util.Config;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

public class Somnium extends JavaPlugin {
    private Config config;
    private Checker checker;
    private Messages messages;
    private PlayerManager playerManager;

    public void onEnable() {
        var pluginManager = getServer().getPluginManager();

        DarkWaterLoader.checkVersion(this);
        new LoadMessagesJson(this).createFiles();
        config = new Config(this);
        checker = new Checker(this);
        messages = new Messages(this);
        playerManager = new PlayerManager(this);

        Arrays.asList(
                messages,
                playerManager,
                new BedListener(this)
        ).forEach(listener -> pluginManager.registerEvents(listener, this));

        var commandManager = new CommandManager(this);
        commandManager.registerCommands(new HarborCommand(this));
        commandManager.registerCommands(new ForceSkipCommand(this));
        Objects.requireNonNull(getCommand("somnium")).setTabCompleter(new TabComplete());
        sendInfo();
    }

    @Override
    public void onDisable() {
        for (World world : getServer().getWorlds()) {
            messages.clearBar(world);
        }
    }

    @NotNull
    public Config getConfiguration() {
        return config;
    }

    @NotNull
    public Checker getChecker() {
        return checker;
    }

    @NotNull
    public Messages getMessages() {
        return messages;
    }

    @NotNull
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public void sendInfo() {
        getLogger().log(Level.INFO, "=========================");
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, "Somnium started!");
        getLogger().log(Level.INFO, "Creator: kiinse | Based on Harbor by nkomarn");
        getLogger().log(Level.INFO, "Website: https://github.com/kiinse");
        getLogger().log(Level.INFO, "Plugin version: " + getDescription().getVersion());
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, " ");
        getLogger().log(Level.INFO, "========================");
    }

}
