package kiinse.plugin.somnium.util;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugins.api.darkwaterapi.utilities.Utils;

import java.util.logging.Level;

public class DarkWaterLoader {

    public static void checkVersion(Somnium somnium) {
        somnium.getLogger().log(Level.INFO, "Loading DarkWaterAPI...");
        var utils = new Utils(somnium);
        var currentVersion = utils.getApiVersion();
        var requestVersion = new Config(somnium).getCurrentApiVersion();
        if(utils.formatVersion(requestVersion) > utils.formatVersion(currentVersion) ) {
            throw new NullPointerException("DarkWaterAPI is deprecated! You have version " + currentVersion + ", but plugin require version " + requestVersion + " and above.\nDownload it at: https://github.com/kiinse/somnium/releases");
        } else {
            somnium.getLogger().log(Level.INFO, "DarkWaterAPI loaded");
        }
    }
}
