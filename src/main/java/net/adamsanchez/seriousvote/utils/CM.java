package net.adamsanchez.seriousvote.utils;

import com.google.common.collect.Iterables;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import net.adamsanchez.seriousvote.SeriousVote;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 4/26/2017.
 */
public class CM {


    public static ConfigurationNode RootNode;

    public static boolean getDebugMode(ConfigurationNode node) {
        return node.getNode("config", "debug-mode").getBoolean();
    }

    public static boolean getStackRewards(ConfigurationNode node){
        return node.getNode("config", "stack-reward-names").getBoolean();
    }

    public static List<String> getDailiesSetCommands(ConfigurationNode node, String dailyName) {
        return node.getNode("config", "dailies", dailyName, "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static int[] getEnabledMilestones(ConfigurationNode node) {
        List<String> list = node.getNode("config", "milestones", "records-enabled").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
        int[] array = new int[list.size()];
        for (int ix = 0; ix < list.size(); ix++) {
            array[ix] = Integer.parseInt(list.get(ix));
        }

        return array;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    public static String getDatabaseType(ConfigurationNode node) {
        return node.getNode("config", "database", "dbType").getString();
    }

    public static String getDatabaseName(ConfigurationNode node) {
        return node.getNode("config", "database", "name").getString();
    }

    public static String getDatabaseHostname(ConfigurationNode node) {
        return node.getNode("config", "database", "hostname").getString();
    }

    public static String getDatabasePort(ConfigurationNode node) {
        return node.getNode("config", "database", "port").getString();
    }

    public static String getDatabasePrefix(ConfigurationNode node) {
        return node.getNode("config", "database", "prefix").getString();
    }

    public static String getDatabaseUsername(ConfigurationNode node) {
        return node.getNode("config", "database", "username").getString();
    }

    public static String getDatabasePassword(ConfigurationNode node) {
        return node.getNode("config", "database", "password").getString();
    }

    public static String getMaxActiveConnections(ConfigurationNode node) {
        return node.getNode("config", "database", "maximum-active-connections").getString();
    }

    public static String getMinIdleConnections(ConfigurationNode node) {
        return node.getNode("config", "database", "minimum-idle-connections").getString();
    }

    public static boolean getMilestonesEnabled(CommentedConfigurationNode node) {
        return node.getNode("config", "milestones", "enabled").getBoolean();
    }

    public static boolean getDailiesEnabled(CommentedConfigurationNode node) {
        return node.getNode("config", "dailies", "enabled").getBoolean();
    }

    public static boolean getMonthlyResetEnabled(CommentedConfigurationNode node) {
        return node.getNode("config", "monthly-reset-enabled").getBoolean();
    }

    public static int getMonthlyResetDay(CommentedConfigurationNode node) {
        return node.getNode("config", "monthly-reset-day").getInt();
    }

    public static boolean getMonthlyResetWithOffline(CommentedConfigurationNode node) {
        return node.getNode("config", "monthly-reset-cache-enabled").getBoolean(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> getSetCommands(ConfigurationNode node) {
        return node.getNode("config", "vote-reward", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getRandomCommands(ConfigurationNode node) {
        return node.getNode("config", "vote-reward", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getVoteSites(ConfigurationNode node) {
        return node.getNode("config", "vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    //Returns the string value from the Config for the public message. This must be deserialized
    public static String getPublicMessage(ConfigurationNode node) {
        return node.getNode("config", "broadcast-message").getString();
    }

    public static String getOfflineMessage(ConfigurationNode node) {
        return node.getNode("config", "broadcast-message-offline").getString();
    }

    public static String getVoteSiteMessage(ConfigurationNode node) {
        return node.getNode("config", "vote-sites-message").getString();
    }

    public static boolean getBypassOffline(ConfigurationNode node) {
        return node.getNode("config", "bypass-offline").getBoolean();
    }

    public static boolean getMessageOffline(CommentedConfigurationNode node) {
        return node.getNode("config", "broadcast-offline").getBoolean();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    public static void initConfig(Path pluginDirectory) {
        Asset configAsset = SeriousVote.getInstance().getPlugin().getAsset("seriousvote.conf").orElse(null);
        if (Files.notExists(pluginDirectory)) {
            if (configAsset != null) {
                try {
                    SeriousVote.getInstance().getLogger().info("Copying Default Config");
                    SeriousVote.getInstance().getLogger().info(configAsset.readString());
                    SeriousVote.getInstance().getLogger().info(pluginDirectory.toString());
                    configAsset.copyToFile(pluginDirectory);
                } catch (IOException e) {
                    e.printStackTrace();
                    SeriousVote.getInstance().getLogger().error("Could not unpack the default config from the jar! Maybe your Minecraft server doesn't have write permissions?");
                    return;
                }
            } else {
                SeriousVote.getInstance().getLogger().error("Could not find the default config file in the jar! Did you open the jar and delete it?");
                return;
            }
        }
    }


}
