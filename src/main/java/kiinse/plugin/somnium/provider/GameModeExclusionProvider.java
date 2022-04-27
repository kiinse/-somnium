package kiinse.plugin.somnium.provider;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.api.ExclusionProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class GameModeExclusionProvider implements ExclusionProvider {
    private final Somnium somnium;

    public GameModeExclusionProvider(@NotNull Somnium somnium) {
        this.somnium = somnium;
    }

    @Override
    public boolean isExcluded(Player player) {
        return somnium.getConfig().getBoolean("exclusions.exclude-" + player.getGameMode().toString().toLowerCase(), false);
    }
}
