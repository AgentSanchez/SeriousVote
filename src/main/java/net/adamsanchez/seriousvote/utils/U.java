package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * Created by adam_ on 01/22/17.
 */
public class U {
    public static void info(String info) {
        SeriousVote.getInstance().getLogger().info(info + CC.RESET);
    }

    public static void debug(String debug) {
        if (SeriousVote.getInstance().isDebug()) {
            SeriousVote.getInstance().getLogger().info("[DEBUG]: " + debug + CC.RESET);
        }
    }

    public static void debug(Map<?, ?> map, String prefix) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            U.debug(prefix + " " + key);
        }
    }
    public static void debug(Set<String> setOfWords) {
        for (String s: setOfWords) {
            U.debug(s);
        }
    }

    public static void error(String error) {
        SeriousVote.getInstance().getLogger().error(error + CC.RESET);
    }

    public static void error(String error, Exception e) {
        SeriousVote.getInstance().getLogger().error(error + CC.RESET, e);
    }

    public static void warn(String warn) {
        SeriousVote.getInstance().getLogger().warn(warn + CC.RESET);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    public static String getName(String playerIdentifier) {
        return getName(playerIdentifier, false);
    }

    public static String getName(String playerIdentifier, boolean bypass) {
        if (bypass == false && SeriousVote.isServerOnline() == false) return playerIdentifier;
        if (SeriousVote.isServerOnline() && !isUUID(playerIdentifier)) {
            U.error("Non UUID player identifier provided while server was in online mode!! identifier: " + playerIdentifier);
            return playerIdentifier;
        }
        //id getname returns and if identifier are equal return identifier
        Optional<UserStorageService> userStorage = SeriousVote.getUserStorage();
        U.debug("Attempting to get name from UUID...");
        Optional<User> user = userStorage.get().get(UUID.fromString(playerIdentifier));
        if (!user.isPresent()) {
            if (isUsername(playerIdentifier)) {
                U.debug("UUID not found, but this does appear to be a name....");
                return playerIdentifier;
            }
            U.debug("System was unable to retrieve name from UUID: " + playerIdentifier.toString());
            return "";
        } else {
            U.debug("System was able to retrieve name from UUID for: " + user.get().getName());
            return user.get().getName();
        }
    }

    /**
     * Returns a systemwide playerIdentifier depending on if the game is in online or offline mode
     *
     * @param nameOrID
     * @return
     */
    public static String getPlayerIdentifier(String nameOrID) throws PlayerNotFoundException {
        U.debug("Retrieving playerIdentifier for input + \"" + nameOrID + "\".");
        String result = nameOrID;
        if (SeriousVote.isServerOnline() == false) {
            result = nameOrID;
        } else {
            try {
                result = U.getIdFromName(nameOrID).toString();
            } catch (Exception e) {
                U.error("Player not found!");
                throw new PlayerNotFoundException("The username could not be found in the system, is it possible they haven't joined yet?", e);
            }
        }
        U.debug("Player Identifier returned as \"" + result + "\"");
        return result;
    }

    public static boolean isPlayerOnline(String username) {
        return SeriousVote.getPublicGame().getServer().getPlayer(username).isPresent();
    }

    public static String getIdFromName(String name) {
        Optional<UserStorageService> userStorage = SeriousVote.getUserStorage();
        if ((userStorage.get().get(name).isPresent())) {
            U.debug("returning ID from name");
            return userStorage.get().get(name).get().getUniqueId().toString();
        } else {
            U.debug("Unable to get ID from name... It seems this player hasn't joined the server before");
            return null;
        }
    }

    public static Text convertStringToLink(String link) {
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize(link);
        try {
            return textLink.toBuilder().onClick(TextActions.openUrl(new URL(textLink.toPlain()))).build();
        } catch (MalformedURLException e) {
            U.debug("Malformed URL - Was that intentional? -- " + link);
        }
        return Text.of(link);
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void bcast(String msg, String username) {
        OutputHelper.broadCastMessage(msg, username);
    }

    public static String convertIDToName(String playerIdentifier) {
        if (!isUUID(playerIdentifier)) return playerIdentifier;
        String s = getName(playerIdentifier, true);
        U.debug("convertIDToName returns: " + s);
        return s;
    }

    public static String convertNameToID(String playerIdentifier) {
        if (isUUID(playerIdentifier)) return playerIdentifier;
        String s = getIdFromName(playerIdentifier);
        if (s != null && s != "") {
            U.debug("Converted Successfully!! to: " + s);
            return s;
        }
        U.debug("Conversion Failed returning original identifier: " + playerIdentifier + "Please convert manually.");
        return playerIdentifier;
    }

    public static boolean isUUID(String id) {
        try {
            UUID.fromString(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isUsername(String playerIdentifier) {
        try {
            if (SeriousVote.getUserStorage().get().get(playerIdentifier).isPresent()) return true;
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static ConsoleSource getConsole() {
        return SeriousVote.getPublicGame().getServer().getConsole();
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
        U.debug(CC.RED + "Making List   " + setOfWords.size());
        U.debug(setOfWords);
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
