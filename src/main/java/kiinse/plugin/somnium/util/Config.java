package kiinse.plugin.somnium.util;

import kiinse.plugin.somnium.Somnium;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

public class Config {
    private final Somnium somnium;

    public Config(@NotNull Somnium somnium) {
        this.somnium = somnium;
        somnium.saveDefaultConfig();
    }

    @NotNull
    public FileConfiguration getConfig() {
        return somnium.getConfig();
    }

    public void reload() {
        somnium.reloadConfig();
    }

    public boolean getBoolean(@NotNull String location) {
        return getConfig().getBoolean(location, false);
    }

    @NotNull
    public String getString(@NotNull String location) {
        return getConfig().getString(location, "");
    }

    public int getInteger(@NotNull String location) {
        return getConfig().getInt(location, 0);
    }

    public double getDouble(@NotNull String location) {
        return getConfig().getDouble(location, 0.0);
    }

    @NotNull
    public List<String> getStringList(@NotNull String location) {
        return getConfig().getStringList(location);
    }

    public String getCurrentApiVersion() {
        try {
            var input = somnium.getClass().getResourceAsStream(File.separator + "resources" + File.separator + "info.properties");
            InputStream info;
            if (input != null) {
                info = input;
            } else {
                info = somnium.getClass().getClassLoader().getResourceAsStream("info.properties");
            }
            var property = new Properties();
            property.load(info);
            return property.getProperty("api.version");
        } catch (IOException e) {
            somnium.getLogger().log(Level.INFO, "Error on getting api version: " + e.getMessage());
        }
        return null;
    }
}
