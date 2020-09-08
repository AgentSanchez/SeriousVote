package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Iterator;
import java.util.Set;

public class OutputHelper {

    public static boolean broadCastMessage(String message, String username) {
        if (message == null || message.isEmpty() || message == "" ) return false;
        if(PlaceHolders.apiLoaded){
        SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(
        PlaceHolders.getPapi().replacePlaceholders(
                parseVariables(message, username),
                SeriousVote.getPublicGame().getServer().getConsole(),
                SeriousVote.getPublicGame().getServer().getConsole()));
        } else {
            SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(strToText(parseVariables(message, username)));
        }
        return true;
    }

    public static boolean broadCastMessage(String message, String username, String currentRewards) {
        if(!U.isPlayerOnline(username)) return false;
        if(PlaceHolders.apiLoaded){
            SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(
                    PlaceHolders.getPapi().replacePlaceholders(
                            parseVariables(message, username, currentRewards),
                            SeriousVote.getPublicGame().getServer().getConsole(),
                            SeriousVote.getPublicGame().getServer().getConsole()));
        } else {
            SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(strToText(parseVariables(message, username, currentRewards)));
        }
        return true;
    }

    public static boolean broadCastMessage(String message, VoteRequest workingRequest) {
        if(PlaceHolders.apiLoaded){
            SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(
                    PlaceHolders.getPapi().replacePlaceholders(
                            parseAllVariables(message, workingRequest),
                            SeriousVote.getPublicGame().getServer().getConsole(),
                            SeriousVote.getPublicGame().getServer().getConsole()));
        } else {
            SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(strToText(parseAllVariables(message, workingRequest)));
        }
        return true;
    }

    public static String parseVariables(String string, String username) {
        return string.replace("{player}", username);
    }

    public static String parseVariables(String string, String username, String currentRewards) {
        if (currentRewards == "") {
            return string.replace("{player}", username).replace("{rewards}", "No Random Rewards");
        }
        return string.replace("{player}", username).replace("{rewards}", currentRewards);
    }

    public static String parseAllVariables(String strToParse, VoteRequest workingRecord){
        String workingString = strToParse;
        workingString = workingString.replace("{player}", workingRecord.getUsername());
        if(!workingRecord.getRewardNames().isEmpty() || !workingString.contains("{rewards}")){
            workingString = workingString.replace("{rewards}", listMaker(workingRecord.getRewardNames()));
        } else {
            workingString = workingString.replace("{rewards}", "No Random Rewards");
        }
        if (workingRecord.getServiceName() != null && workingRecord.getServiceName() != "") {
            workingString = workingString.replace("{vote-service}", workingRecord.getServiceName());
        }

        return workingString;
    }



    public static Text strToText(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }

    /**
     * Returns a comma separated grammatically correct list of items.
     *
     * @return
     */
    public static String listMaker(Set<String> setOfWords) {
        if (setOfWords.size() < 1) return "";
        StringBuilder list = new StringBuilder();
        String sepA = ", ";
        String sepB = " and ";
        int counter = 0;
        Iterator<String> it = setOfWords.iterator();
        while (it.hasNext()) {
            if (counter > 0) {
                if (counter == setOfWords.size() - 1) {
                    list.append(sepB);
                } else {
                    list.append(sepA);
                }
            }
            list.append(it.next());
            counter++;
        }
        return list.toString().trim();
    }
}
