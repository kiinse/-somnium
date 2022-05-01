package kiinse.plugin.somnium.files;

import kiinse.plugin.somnium.Somnium;
import kiinse.plugin.somnium.util.Messages;
import kiinse.plugins.api.darkwaterapi.files.Json;
import kiinse.plugins.api.darkwaterapi.files.utils.FileManager;
import kiinse.plugins.api.darkwaterapi.files.utils.FilesPatches;
import java.io.IOException;
import java.util.logging.Level;

public class LoadMessagesJson {

    private final Somnium somnium;

    public LoadMessagesJson(Somnium somnium) {
        this.somnium = somnium;
    }

    public void createFiles() {
        try {
            somnium.getLogger().log(Level.INFO, "Loading messages...");
            FileManager.copyFile(FileManager.accessFile(somnium.getClass(), "messages.json"), FilesPatches.getFile(somnium,"messages", FilesPatches.types.JSON));
            Messages.setSomniumMessages(Json.load(FilesPatches.getFile(somnium,"messages", FilesPatches.types.JSON)));
            somnium.getLogger().log(Level.INFO, "Messages loaded!");
        } catch (IOException e) {
            somnium.getLogger().log(Level.WARNING, "Error on loading messages! Info: " + e.getMessage());
        }
    }
}
