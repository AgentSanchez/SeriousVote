package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
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
            SeriousVote.getInstance().getLogger().info(debug + CC.RESET);
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

    public static String getName(UUID player){
        Optional<UserStorageService> userStorage =  SeriousVote.getUserStorage();
        U.debug("Attempting to get name from UUID...");
        String name = userStorage.get().get(player).get().getName();
        if(name == "" || name == null){
            U.debug("System was unable to retrieve name from UUID: " + player.toString());
            return "UNKNOWN";
        } else {
            U.debug("System was able to retrieve name from UUID for: " + name);
            return name;
        }


    }

    public static boolean isOnline(String username) {
        return SeriousVote.getInstance().getPublicGame().getServer().getPlayer(username).isPresent() ? true : false;
    }

    public static UUID getIdFromName(String name){
        Optional<UserStorageService> userStorage =  SeriousVote.getUserStorage();
        if((userStorage.get().get(name).isPresent())){
            U.debug("returning ID from name");
            return userStorage.get().get(name).get().getUniqueId();
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




}
