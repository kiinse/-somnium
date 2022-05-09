package kiinse.plugin.somnium;

import kiinse.plugin.somnium.command.TabComplete;
import kiinse.plugin.somnium.task.Checker;
import kiinse.plugin.somnium.util.SomniumMessages;
import kiinse.plugin.somnium.util.PlayerManager;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.CommandManager;
import kiinse.plugins.api.darkwaterapi.loader.DarkWaterJavaPlugin;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import kiinse.plugin.somnium.command.ForceSkipCommand;
import kiinse.plugin.somnium.listener.BedListener;

import java.util.Arrays;
import java.util.Objects;

public class Somnium extends DarkWaterJavaPlugin {
    private Checker checker;
    private SomniumMessages messages;
    private PlayerManager playerManager;

    @Override
    public void onStart() throws Exception {
        var pluginManager = getServer().getPluginManager();
        checker = new Checker(this);
        messages = new SomniumMessages(this);
        playerManager = new PlayerManager(this);
        Arrays.asList(
                messages,
                playerManager,
                new BedListener(this)
        ).forEach(listener -> pluginManager.registerEvents(listener, this));
        var commandManager = new CommandManager(this);
        commandManager.registerCommands(new ForceSkipCommand(this));
        Objects.requireNonNull(getCommand("somnium")).setTabCompleter(new TabComplete());
    }

    @Override
    public void onStop() throws Exception {
        for (World world : getServer().getWorlds()) {
            messages.clearBar(world);
        }
    }

    @NotNull
    public Checker getChecker() {
        return checker;
    }

    @NotNull
    public SomniumMessages getMsg() {
        return messages;
    }

    @NotNull
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

}
