package net.adamsanchez.seriousvote;

import javafx.scene.control.Tab;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 4/26/2017.
 */
public class LootTable {
    private ConfigurationNode tableSource;
    private boolean activated = false;
    private String tableName = "";

    private String[][] Table;

    private int chanceTotal,chanceMax, chanceMin = 0;
    private int[] chanceMap;


    //TODO This class will be the instantiable version to allow quick creation and deflation of the rewards system

    public LootTable(String rewardSet, ConfigurationNode tableSource) {
        //Gather Reward Set From Configurations
        this.tableSource = tableSource.getNode("config","Tables", rewardSet);
        this.tableName = rewardSet;
        List<String> nodeStrings = this.tableSource.getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());

        if(nodeStrings.size()%2!= 0 || nodeStrings.size()<1){
            U.error("Please check the Config for Table: " + rewardSet + " It might not be formatted correctly.");
            activated = false;
        } else {
            //Convert List to Array
            String[] inputLootSource = nodeStrings.stream().toArray(String[]::new);
            //Create a new Array of the proper size x*2
            Table = new String[2][inputLootSource.length/2];
            chanceMap = new int[inputLootSource.length/2];

            for(int ix = 0; ix < inputLootSource.length; ix+=2){
                 Table[0][ix/2] = inputLootSource[ix];
                 Table[1][ix/2] = inputLootSource[ix+1];
                 //Initialize chanceMap
                 chanceMap[ix/2] = Integer.parseInt(Table[0][ix/2]);
                 if(ix != 0){
                     chanceMap[ix/2]+= chanceMap[(ix/2)-1];

                 }
            }
            chanceTotal = chanceMap.length-1;
            chanceMin = chanceMap[0];
            chanceMax = chanceMap[chanceTotal];


        }

    }

    /*
    Input is an integer for which algorithm, for now there will only be the standard
    seed is for inputting a randomization for the algorithm
     */
    public String chooseReward(){
        //compare
        int roll = this.roll();
        int currentChoice = -1;
        U.debug("Choosing from " + chanceMap.length +"items.");
        for(int ix = 0; ix < chanceMap.length; ix++){
            if(roll <= chanceMap[ix]){
                currentChoice = ix;
                break;
            }
        }
        if(currentChoice < 0 ) U.error("There was a problem while rolling something might be broken");
        String chosenReward = Table[1][currentChoice];
        return chosenReward;
    }

    public int roll(){
        if(chanceMax == 0) return 0;
        //Returns a number within the chancepool inclusive to 0
        int response = ThreadLocalRandom.current().nextInt(0,chanceMax + 1);
        U.debug("Rolled a " + response + " out of " + chanceMax);
       return  response;
    }
    public boolean isActivated(){
        return activated;
    }

    public String getTableName(){
        return tableName;
    }

}
