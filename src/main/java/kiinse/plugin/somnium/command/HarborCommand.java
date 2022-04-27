package kiinse.plugin.somnium.command;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.files.LoadMessagesJson;
import kiinse.plugin.somnium.util.Config;
import kiinse.plugin.somnium.util.Messages;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.annotation.Command;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.interfaces.CommandClass;
import kiinse.plugins.api.darkwaterapi.files.locale.LocaleUtils;
import kiinse.plugins.api.darkwaterapi.utilities.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class HarborCommand implements CommandClass {

    private final Utils utils;
    private final Somnium somnium;
    private final Config config;

    public HarborCommand(@NotNull Somnium somnium) {
        this.somnium = somnium;
        this.utils = new Utils(somnium);
        this.config = somnium.getConfiguration();
    }

    @Override
    @Command(command = "/somnium reload", permission = "somnium.reload", disallowNonPlayer = true)
    public void mainCommand(CommandSender sender, String[] args) {
        try {
            config.reload();
            new LoadMessagesJson(somnium).createFiles();
            utils.sendMessageWithPrefix(sender, Messages.getSomniumMessages(), LocaleUtils.getLocale(sender), "pluginReloaded");
        } catch (Exception e) {
            utils.sendMessageWithPrefix(sender, Messages.getSomniumMessages(), LocaleUtils.getLocale(sender), "errorOnReloading");
            somnium.getLogger().log(Level.WARNING, "Error on plugin reload: " + e.getMessage());
        }
    }
}
