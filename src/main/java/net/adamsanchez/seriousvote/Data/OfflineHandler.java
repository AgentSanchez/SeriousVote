package net.adamsanchez.seriousvote.Data;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.asset.Asset;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class OfflineHandler {

    public  static void initOfflineStorage(){
        if (Files.notExists(SeriousVote.getInstance().getOfflineVotes())) {
            try {
                OfflineHandler.saveOffline();
            } catch (IOException e) {
                SeriousVote.getInstance().getLogger().error("Could Not Initialize the offlinevotes file! What did you do with it");
                //getLogger().error(e.toString());
            }
        }
    }

    public static void saveOffline() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(SeriousVote.getInstance().getOfflineVotes().toFile());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(SeriousVote.getInstance().getStoredVotes());
        objectOutputStream.close();

    }

    public static HashMap<UUID,Integer> loadOffline() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(SeriousVote.getInstance().getOfflineVotes().toFile());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        HashMap<UUID,Integer> storedVotes = (HashMap<UUID, Integer>) objectInputStream.readObject();
        objectInputStream.close();
        return  storedVotes;
    }


}
