package kiinse.plugin.somnium.command;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.util.Messages;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.annotation.Command;
import kiinse.plugins.api.darkwaterapi.commands.moderncommands.interfaces.CommandClass;
import kiinse.plugins.api.darkwaterapi.files.locale.LocaleUtils;
import kiinse.plugins.api.darkwaterapi.utilities.Utils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ForceSkipCommand implements CommandClass {

    private final Somnium somnium;
    private final Utils utils;

    public ForceSkipCommand(@NotNull Somnium somnium) {
        this.utils = new Utils(somnium);
        this.somnium = somnium;
    }

    @Override
    @Command(command = "/forceskip", permission = "somnium.forceskip", disallowNonPlayer = true)
    public void mainCommand(CommandSender sender, String[] args) {
        var world = utils.getPlayer(sender).getWorld();
        var checker = somnium.getChecker();
        if (checker.isSkipping(world)) {
            utils.sendMessageWithPrefix(sender, Messages.getSomniumMessages(), LocaleUtils.getLocale(sender), "forceskipAlreadySkipping");
        } else {
            utils.sendMessageWithPrefix(sender, Messages.getSomniumMessages(), LocaleUtils.getLocale(sender), "forceskipSkipping");
            checker.forceSkip(world);
        }
    }
}
