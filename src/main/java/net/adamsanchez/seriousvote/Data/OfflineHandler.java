package net.adamsanchez.seriousvote.Data;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class OfflineHandler {

    public  static void initOfflineStorage(){
        U.info("Attempting to load in offline player votes....");
        if (Files.notExists(SeriousVote.getInstance().getOfflineVotesPath())) {
            try {
                OfflineHandler.saveOffline();
            } catch (IOException e) {
                SeriousVote.getInstance().getLogger().error("Could Not Initialize the offlinevotes file! What did you do with it");
                //getLogger().error(e.toString());
            }
        }
    }

    public static void saveOffline() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(SeriousVote.getInstance().getOfflineVotesPath().toFile());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(SeriousVote.getInstance().getOfflineVotes());
        objectOutputStream.close();

    }

    public static HashMap<String,Integer> loadOffline() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(SeriousVote.getInstance().getOfflineVotesPath().toFile());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        HashMap<String,Integer> storedVotes = (HashMap<String, Integer>) objectInputStream.readObject();
        objectInputStream.close();
        return  storedVotes;
    }

    public static void storeLastReset(Date date){
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(SeriousVote.getInstance().getResetDatePath().toFile()));
            writer.write(date.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Date retrieveLastReset(){
        U.debug("Loading date file...");
        try {
            BufferedReader br = new BufferedReader(new FileReader(SeriousVote.getInstance().getResetDatePath().toFile()));
            String s = br.readLine();
            br.close();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", new Locale("us"));
            Date d = sdf.parse(s);
            U.debug("Last Reset- " + d.toString());
            return d;
        } catch (Exception e) {
            U.debug("Date file loading failed!!");
            return new Date(0);
        }
    }

    public static void dumpSQLData(ArrayList<PlayerRecord> records){
        U.info("Attempting to request and dump all db data....");
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()){
            U.info("can't import file... You are not using the voteSpreeSystem");
            return;
        }

        try {
            File f = new File(SeriousVote.getInstance().getSQLDumpPath().toString());
            f.delete();
            BufferedWriter br = new BufferedWriter(new FileWriter(SeriousVote.getInstance().getSQLDumpPath().toString()));

            for(PlayerRecord r : records) {
                U.debug("Writing Player:" + r.toString());
                br.write(r.getPlayerIdentifier() + ',' +
                        r.getTotalVotes() + ',' +
                        r.getVoteSpree() + ',' +
                        r.getLastVote() + ',' +
                        "\n");
            }
            U.info("Wrote " + records.size() + " to file....");
            br.close();
        } catch (IOException e) {
            U.error("Couldn't make file....", e);
        }

    }
}
