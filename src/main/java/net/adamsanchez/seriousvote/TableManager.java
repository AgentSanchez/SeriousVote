package net.adamsanchez.seriousvote;

import net.adamsanchez.seriousvote.utils.U;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 7/4/2017.
 */
public class TableManager {

    public static String[][] makeTableSet(ConfigurationNode node){
        List<String> nodeStrings = node.getChildrenList().stream().map(ConfigurationNode::getString).collect(Collectors.toList());
        if(nodeStrings.size()%2!= 0 || nodeStrings.size() < 1){
            U.error("Please check the Config for errors.");
            return null;
        }
            String[] inputLootSource = nodeStrings.stream().toArray(String[]::new);
            //Create a new Array of the proper size x*2 to hold the tables for choosing later
            String[][] table = new String[2][inputLootSource.length / 2];
            U.info(inputLootSource.length / 2 + " Tables Imported for Rewards");
            for (int ix = 0; ix < inputLootSource.length; ix += 2) {
                table[0][ix / 2] = inputLootSource[ix];
                table[1][ix / 2] = inputLootSource[ix + 1];
                //Initialize chanceMap
            }

        return table;

    }
    public static String chooseTable(ConfigurationNode node){
        String[][] rewardTable = makeTableSet(node);
        return rewardTable != null ? chooseTable(rewardTable) : "";
    }
    private static String chooseTable(String [][] rewardTable){

        int[] chanceMap = new int[rewardTable[0].length];

        for(int ix = 0; ix < rewardTable[0].length; ix++){
            chanceMap[ix] = Integer.parseInt(rewardTable[0][ix]);
            if(ix != 0){
                chanceMap[ix]+= chanceMap[(ix)-1];

            }
        }

        int roll = roll(chanceMap[chanceMap.length-1]);
        int currentChoice = -1;


        for(int ix = 0; ix < chanceMap.length; ix++){

            if(roll <= chanceMap[ix]){

                currentChoice = ix;
                break;
            }
        }

        if(currentChoice < 0 ) U.error("There was a problem while rolling something might be broken");
        return rewardTable[1][currentChoice];
    }

    public static int roll(int upperBound){
        //Returns a number within the chance pool bound is lower inclusive upper exclusive
        int nextInt;
        if(upperBound>0) {
            nextInt = ThreadLocalRandom.current().nextInt(0, upperBound + 1);
            return nextInt;
        }
        return  0;
    }
}
