package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

/**
 * Created by Adam Sanchez on 4/14/2018.
 */
public class CC {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE
    public static final String LINE = "-------------------------------------------------------------";
    public static final String LINE_RED = RED + LINE;

    public static void printSVInfo(){
        SeriousVote.getInstance().getLogger().info(CC.logo());
        SeriousVote.getInstance().getLogger().info(CC.RED + "ONLINE MODE? " + Sponge.getGame().getServer().getOnlineMode() + " "
                + CC.YELLOW_BOLD + "Serious Vote Version: "
                + CC.PURPLE_BOLD + SeriousVote.getInstance().getPlugin().getVersion().get()
                + CC.YELLOW_BOLD + " MC-Version: "
                + CC.PURPLE_BOLD + Sponge.getPlatform().getMinecraftVersion().getName()
                + CC.YELLOW_BOLD + " Sponge-Version: "
                + CC.PURPLE_BOLD + Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + "-"
                + Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown"));
    }

    public static String logo(){
        return  "                                                                                                    \n" +
                "                                                                       " + GREEN + "NN" + CYAN +"                          \n" +
                "                                                                      "+GREEN+"ooo"+CYAN+"                           \n" +
                "                                                                    "+GREEN+"NoooN"+CYAN+"                           \n" +
                "                                                                   "+GREEN+"NoooN"+CYAN+"                            \n" +
                "                                                                  "+GREEN+"ooooN"+CYAN+"                             \n" +
                "     NNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNNNNNNNNNNN"+GREEN+"ooo"+CYAN+"NNNN   NNNNN  NNNNNNNNNNNN     \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNNNNNNNNNNN"+GREEN+"oo"+CYAN+"NNNNN   NNNNN  NNNNNNNNNNNNN    \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNNNNNNNNNNNN NNNNN   NNNNN  NNNNNNNNNNNNN    \n" +
                "    NNNNNN         NNNNN        NNNNNN NNNNNN NNNNN  NNNNN  "+GREEN+"No"+CYAN+"NNNNN NNNNN   NNNNN  NNNNN            \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNNN "+GREEN+"NooN"+CYAN+"NNNN NNNNN   NNNNN  NNNNNNNNNNNN     \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNNN"+GREEN+"NoooN"+CYAN+"NNNN NNNNN   NNNNN  NNNNNNNNNNNNN    \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNNN NNNNN  NNNN"+GREEN+"oooooN"+CYAN+"NNNN NNNNN   NNNNN  NNNNNNNNNNNNN    \n" +
                "     NNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNN  NNNNN  NNNN"+GREEN+"oooooN"+CYAN+"NNNN NNNNN   NNNNN  NNNNNNNNNNNNN    \n" +
                "            NNNNN  NNNNN        NNNNNN NNNNNN NNNNN  NNNN"+GREEN+"oooN"+CYAN+"NNNNNN NNNNNN  NNNNN         NNNNNN    \n" +
                "     NNNNNNNNNNNN  NNNNNNNNNNNN NNNNNN NNNNNN NNNNN  NNNNN"+GREEN+"ooN"+CYAN+"NNNNNN NNNNNNNNNNNNN  NNNNNNNNNNNNN    \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NNNNNN NNNNNN NNNNN  NNNNNNNNNNNNNN NNNNNNNNNNNNN  NNNNNNNNNNNNN    \n" +
                "    NNNNNNNNNNNNN  NNNNNNNNNNNN NN"+GREEN+"NN"+CYAN+"NN NNNNNN NNNNN NNNNNNNNNNNNNNN NNNNNNNNNNNNN  NNNNNNNNNNNNN    \n" +
                "     NNNNNNNNNNNN  NNNNNNNNNNNN N"+GREEN+"NooN"+CYAN+"  NNNNN  NNNNN"+GREEN+"NooooN"+CYAN+"NNNNNNNNN  NNNNNNNNNNNNN  NNNNNNNNNNNN     \n" +
                "                                 "+GREEN+"NoooN            NoooooN"+CYAN+"                                           \n" +
                "                                  "+GREEN+"oooooN          ooooN"+CYAN+"                                             \n" +
                "                                  "+GREEN+"NooooooN       oooooN"+CYAN+"NNNNNNNNNNNN NNNNNNNNNNNNN NNNNNNNNNNNN      \n" +
                "                                    "+GREEN+"NoooooN     NoooooN"+CYAN+"NNNNNNNNNNNN  NNNNNNNNNNNN NNNNNNNNNNNN      \n" +
                "                                     "+GREEN+"Noooooo    ooooo"+CYAN+"NNNNNN   NNNNN     NNNNNN    NNNNNN            \n" +
                "                                      "+GREEN+"Noooooo  ooooo"+CYAN+" NNNNNN   NNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                       "+GREEN+"NoooooNNooooN"+CYAN+" NNNNNN   NNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                        "+GREEN+"ooooooooooN"+CYAN+"  NNNNNN   NNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                         "+GREEN+"ooooooooo"+CYAN+"   NNNNNN   NNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                         "+GREEN+"NoooooooN"+CYAN+"   NNNNNN   NNNNN     NNNNNN    NNNNNN            \n" +
                "                                          "+GREEN+"NoooooN"+CYAN+"    NNNNNNNNNNNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                           "+GREEN+"ooooo"+CYAN+"     NNNNNNNNNNNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                           "+GREEN+"NoooN"+CYAN+"     NNNNNNNNNNNNNN     NNNNNN    NNNNNNNNNNNN      \n" +
                "                                            "+GREEN+"ooo"+CYAN+"                                                     \n" +
                "                                            "+GREEN+"NoN"+CYAN+"                                                     \n" +
                "                                             "+GREEN+"o"+CYAN+"     \n";

    }


}
