package kiinse.plugin.somnium.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter {

    public List<String> onTabComplete(CommandSender sender, @NotNull Command cmd, @NotNull String commandLabel, String[] args) {
        if (!sender.hasPermission("somnium.admin")) {
            return null;
        }

        if (args.length != 1) {
            return null;
        }

        return Collections.singletonList("reload");
    }

}
