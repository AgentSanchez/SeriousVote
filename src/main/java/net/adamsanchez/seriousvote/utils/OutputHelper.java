package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

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
        if(!workingRecord.getRewardNames().isEmpty()){
            workingString = workingString.replace("{rewards}", U.listMaker(workingRecord.getRewardNames()));
        } else {
            workingString = workingString.replace("rewards}", "No Random Rewards");
        }
        workingString = workingString.replace("{vote-service}", workingRecord.getServiceName());
        return workingString;
    }

    public static Text strToText(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }
}
