package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;


/**
 * Created by adam_ on 01/22/17.
 */
public class U {
    public static void info(String info){
        SeriousVote.getInstance().getLogger().info(info + CC.RESET);
    }
    public static void debug(String debug){
        if(SeriousVote.getInstance().isDebug()) {
            SeriousVote.getInstance().getLogger().info("[DEBUG]: " + debug + CC.RESET);
        }
    }
    public static void error(String error) {
        SeriousVote.getInstance().getLogger().error(error + CC.RESET);
    }
    public static void error(String error, Exception e){
        SeriousVote.getInstance().getLogger().error(error + CC.RESET,e);
    }
    public static void warn(String warn){
        SeriousVote.getInstance().getLogger().warn(warn + CC.RESET);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getName(String playerIdentifier){
        getName(playerIdentifier, false);
    }
    public static String getName(String playerIdentifier, boolean bypass){
        if(bypass == false && SeriousVote.isServerOnline() == false) return playerIdentifier;
        Optional<UserStorageService> userStorage =  SeriousVote.getUserStorage();
        U.debug("Attempting to get name from UUID...");
        String name = userStorage.get().get(playerIdentifier).get().getName();
        if(name == "" || name == null){
            U.debug("System was unable to retrieve name from UUID: " + playerIdentifier.toString());
            return "UNKNOWN";
        } else {
            U.debug("System was able to retrieve name from UUID for: " + name);
            return name;
        }
    }

    /**
     * Returns a systemwide playerIdentifier depending on if the game is in online or offline mode
     * @param nameOrID
     * @return
     */
    public static String getPlayerIdentifier(String nameOrID){
        U.debug("Retrieving playerIdentifier for input + \"" + nameOrID + "\"." );
        String result = nameOrID;
        if(SeriousVote.isServerOnline() == false){
            result = nameOrID;
        } else {
            result = U.getIdFromName(nameOrID).toString();
        }
        U.debug("Player Identifier returned as \"" + result + "\"");
        return result;
    }

    public static boolean isOnline(String username) {
        return SeriousVote.getInstance().getPublicGame().getServer().getPlayer(username).isPresent() ? true : false;
    }

    public static String getIdFromName(String name){
        Optional<UserStorageService> userStorage =  SeriousVote.getUserStorage();
        if((userStorage.get().get(name).isPresent())){
            U.debug("returning ID from name");
            return userStorage.get().get(name).get().getUniqueId().toString();
        } else {
            U.debug("Unable to get ID from name...");
            return null;
        }
    }

    public static Text convertStringToLink(String link) {
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize(link);
        try {
            return textLink.toBuilder().onClick(TextActions.openUrl(new URL(textLink.toPlain()))).build();
        } catch (MalformedURLException e) {
            U.error("Malformed URL");
            U.error(e.toString());
        }
        return Text.of("Malformed URL - Inform Administrator");
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void bcast(String msg, String username){
        OutputHelper.broadCastMessage(msg,username);
    }

    public static String convertIDToName(String playerIdentifier){
        String s = getName(playerIdentifier, true);
        U.debug("convertIDToName returns: " + s);
        return s;
    }




}
