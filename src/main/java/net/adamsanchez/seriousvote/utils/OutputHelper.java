package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.integration.MagiBridgeAPI;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Objects;

public class OutputHelper {

    public static boolean broadCastMessage(String message, String username) {
        if (message == null || message.isEmpty()) return false;
        Text broadcastMessage = PlaceHolders.apiLoaded ?
                PlaceHolders.getPapi().replacePlaceholders(
                        parseVariables(message, username),
                        SeriousVote.getPublicGame().getServer().getConsole(),
                        SeriousVote.getPublicGame().getServer().getConsole()) :
                strToText(parseVariables(message, username));

        SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(broadcastMessage);
        if (MagiBridgeAPI.isEnabled)
            MagiBridgeAPI.makeBroadCast(broadcastMessage);
        return true;
    }

    public static boolean broadCastMessage(String message, String username, String currentRewards) {
        if(!U.isPlayerOnline(username)) return false;
        Text broadcastMessage;
        if(PlaceHolders.apiLoaded){
            broadcastMessage = PlaceHolders.getPapi().replacePlaceholders(
                    parseVariables(message, username, currentRewards),
                    SeriousVote.getPublicGame().getServer().getConsole(),
                    SeriousVote.getPublicGame().getServer().getConsole());
        } else {
            broadcastMessage = strToText(parseVariables(message, username, currentRewards));
        }
        SeriousVote.getPublicGame().getServer().getBroadcastChannel().send(broadcastMessage);
        if(MagiBridgeAPI.isEnabled)
            MagiBridgeAPI.makeBroadCast(broadcastMessage);
        return true;
    }

    public static String parseVariables(String string, String username) {
        return string.replace("{player}", username);
    }

    public static String parseVariables(String string, String username, String currentRewards) {
        if (Objects.equals(currentRewards, "")) {
            return string.replace("{player}", username).replace("{rewards}", "No Random Rewards");
        }
        return string.replace("{player}", username).replace("{rewards}", currentRewards.substring(0, currentRewards.length() - 2));
    }

    public static Text strToText(String string){
        return TextSerializers.FORMATTING_CODE.deserialize(string);
    }
}
