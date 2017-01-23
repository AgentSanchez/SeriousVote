package net.adamsanchez.seriousvote;

import org.slf4j.Logger;

/**
 * Created by adam_ on 01/22/17.
 */
public class U {
    public static void info(String info){
        SeriousVote.getInstance().getLogger().info(info);
    }
    public static void debug(String debug){
        SeriousVote.getInstance().getLogger().debug(debug);
    }
    public static void error(String error) {
        SeriousVote.getInstance().getLogger().error(error);
    }
    public static void error(String error, Exception e){
        SeriousVote.getInstance().getLogger().error(error,e);
    }
    public static void warn(String warn){
        SeriousVote.getInstance().getLogger().warn(warn);
    }


}
