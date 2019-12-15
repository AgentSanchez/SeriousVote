package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.text.serializer.TextSerializers;

public class OutputHelper {

    public static boolean broadCastMessage(String message, String username) {

        if (message == null || message.isEmpty() || message == "" ) return false;
        SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(
                TextSerializers.FORMATTING_CODE.deserialize(parseVariables(message, username)));
        return true;
    }

    public static boolean broadCastMessage(String message, String username, String currentRewards) {

        if(!U.isPlayerOnline(username)) return false;
        if (message == null || message.isEmpty() || message == "" ) return false;
        SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(
                TextSerializers.FORMATTING_CODE.deserialize(parseVariables(message, username, currentRewards)));
        return true;
    }

    public static String parseVariables(String string, String username) {
        return string.replace("{player}", username);
    }

    public static String parseVariables(String string, String username, String currentRewards) {
        if (currentRewards == "") {
            return string.replace("{player}", username).replace("{rewards}", "No Random Rewards");
        }
        return string.replace("{player}", username).replace("{rewards}", currentRewards.substring(0, currentRewards.length() - 2));
    }
}
