package net.adamsanchez.seriousvote;

import com.google.common.collect.Iterables;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 4/26/2017.
 */
public class ConfigUtil {


    public static ConfigurationNode RootNode;

    public static List<String> getWeeklySetCommands(ConfigurationNode node){
        return node.getNode("config","milestones","weekly","set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    public static List<String> getMonthlySetCommands(ConfigurationNode node){
        return node.getNode("config","milestones","monthly","set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    public static List<String> getYearlySetCommands(ConfigurationNode node){
        return node.getNode("config","milestones","yearly","set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    public static String getDatabaseName(ConfigurationNode node){
        return node.getNode("config","database","name").getString();
    }
    public static String getDatabaseHostname(ConfigurationNode node){
        return node.getNode("config","database","hostname").getString();
    }
    public static String getDatabasePort(ConfigurationNode node){
        return node.getNode("config","database","port").getString();
    }
    public static String getDatabasePrefix(ConfigurationNode node){
        return node.getNode("config","database","prefix").getString();
    }
    public static String getDatabaseUsername(ConfigurationNode node){
        return node.getNode("config","database","username").getString();
    }
    public static String getDatabasePassword(ConfigurationNode node){
        return node.getNode("config","database","password").getString();
    }










    ////////////////////////////////////////////////////////////////////////////////////////////


    public static List<String> getSetCommands(ConfigurationNode node) {
        return node.getNode("config","vote-reward","set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    public static List<String> getRandomCommands(ConfigurationNode node) {
        return node.getNode("config","Rewards","random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    public static List<String> getVoteSites(ConfigurationNode node) {
        //TODO code potentially breaking here -- investigate
        return node.getNode("config","vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    //Returns the string value from the Config for the public message. This must be deserialized
    public static String getPublicMessage(ConfigurationNode node){
        return node.getNode("config","broadcast-message").getString();
    }

}
