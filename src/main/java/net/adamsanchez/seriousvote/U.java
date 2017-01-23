package net.adamsanchez.seriousvote;

import org.slf4j.Logger;

/**
 * Created by adam_ on 01/22/17.
 */
public class U {
    private static Logger logger = SeriousVote.getLogger();

    public static void info(String info){
        logger.info(info);
    }
    public static void debug(String debug){
        logger.debug(debug);
    }
    public static void error(String error) {
        logger.error(error);
    }
    public static void error(String error, Exception e){
        logger.error(error,e);
    }
    public static void warn(String warn){
        logger.warn(warn);
    }


}
