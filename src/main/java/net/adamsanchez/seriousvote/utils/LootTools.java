package net.adamsanchez.seriousvote.utils;

import java.util.concurrent.ThreadLocalRandom;

public class LootTools {

    /**
     * Takes an input and either returns the numRandRewards, or a range between the given min / max values
     * if the range is less than 0.
     * @param numRandRewards
     * @param minRandRewards
     * @param maxRandRewards
     * @return
     */
    public static int genNumRandRewards(int numRandRewards, int minRandRewards, int maxRandRewards) {
        if (numRandRewards < 0) {
            int nextInt;
            //Inclusive
            if (minRandRewards < 0) minRandRewards = 0;
            if (maxRandRewards > minRandRewards) {
                nextInt = ThreadLocalRandom.current().nextInt(minRandRewards, maxRandRewards);
            } else {
                nextInt = 0;
                U.warn("There seems to be an error in your min/max setting in your configs.");
            }

            U.info("Giving out " + nextInt + " random rewards.");
            return nextInt;
        } else if (numRandRewards > 0) {
            return numRandRewards;
        }
        return 0;
    }

    public static int roll(int chanceMax) {
        //Returns a number within the chance pool bound is lower inclusive upper exclusive
        int nextInt;
        if (chanceMax > 0) {

            nextInt = ThreadLocalRandom.current().nextInt(0, chanceMax + 1);
            U.debug("Rolled a " + nextInt + " out of" + chanceMax + "for table.");
            return nextInt;
        }
        return 0;
    }
}
