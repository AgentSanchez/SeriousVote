package net.adamsanchez.seriousvote.Data;

import net.adamsanchez.seriousvote.SeriousVote;

import java.io.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class OfflineHandler {

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
