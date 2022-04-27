package kiinse.plugin.somnium.api;

import org.bukkit.entity.Player;

public interface ExclusionProvider {
    boolean isExcluded(Player player);
}
