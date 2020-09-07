package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 4/26/2017.
 */
public class CM {

    private static CM instance;
    private static ConfigurationNode mainCfgNode;
    ConfigurationLoader<CommentedConfigurationNode> loader;

    public static boolean getDebugMode() {
        return get().mainCfgNode.getNode("config", "debug-mode").getBoolean();
    }

    public static List<String> getWeeklySetCommands() {
        return get().mainCfgNode.getNode("config", "dailies", "weekly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getMonthlySetCommands() {
        return get().mainCfgNode.getNode("config", "dailies", "monthly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getYearlySetCommands() {
        return get().mainCfgNode.getNode("config", "dailies", "yearly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static int[] getEnabledMilestones() {
        List<String> list = get().mainCfgNode.getNode("config", "milestones", "records-enabled").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
        int[] array = new int[list.size()];
        for (int ix = 0; ix < list.size(); ix++) {
            array[ix] = Integer.parseInt(list.get(ix));
        }

        return array;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    public static String getDatabaseType() {
        return get().mainCfgNode.getNode("config", "database", "dbType").getString();
    }

    public static String getDatabaseName() {
        return get().mainCfgNode.getNode("config", "database", "name").getString();
    }

    public static String getDatabaseHostname() {
        return get().mainCfgNode.getNode("config", "database", "hostname").getString();
    }

    public static String getDatabasePort() {
        return get().mainCfgNode.getNode("config", "database", "port").getString();
    }

    public static String getDatabasePrefix() {
        return get().mainCfgNode.getNode("config", "database", "prefix").getString();
    }

    public static String getDatabaseUsername() {
        return get().mainCfgNode.getNode("config", "database", "username").getString();
    }

    public static String getDatabasePassword() {
        return get().mainCfgNode.getNode("config", "database", "password").getString();
    }

    public static String getMaxActiveConnections() {
        return get().mainCfgNode.getNode("config", "database", "maximum-active-connections").getString();
    }

    public static String getMinIdleConnections() {
        return get().mainCfgNode.getNode("config", "database", "minimum-idle-connections").getString();
    }

    public static boolean getMilestonesEnabled() {
        return get().mainCfgNode.getNode("config", "milestones", "enabled").getBoolean();
    }

    public static boolean getDailiesEnabled() {
        return get().mainCfgNode.getNode("config", "dailies", "enabled").getBoolean();
    }

    public static boolean getMonthlyResetEnabled() {
        return get().mainCfgNode.getNode("config", "monthly-reset-enabled").getBoolean();
    }

    public static int getMonthlyResetDay() {
        return get().mainCfgNode.getNode("config", "monthly-reset-day").getInt();
    }

    public static boolean getMonthlyResetWithOffline() {
        return get().mainCfgNode.getNode("config", "monthly-reset-cache-enabled").getBoolean(true);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public static List<String> getSetCommands() {
        return get().mainCfgNode.getNode("config", "vote-reward", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getRandomCommands() {
        return get().mainCfgNode.getNode("config", "vote-reward", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getVoteSites() {
        return get().mainCfgNode.getNode("config", "vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    //Returns the string value from the Config for the public message. This must be deserialized
    public static String getPublicMessage() {
        return get().mainCfgNode.getNode("config", "broadcast-message").getString();
    }

    public static String getOfflineMessage() {
        return get().mainCfgNode.getNode("config", "broadcast-message-offline").getString();
    }

    public static String getVoteSiteMessage() {
        return get().mainCfgNode.getNode("config", "vote-sites-message").getString();
    }

    public static boolean getBypassOffline() {
        return get().mainCfgNode.getNode("config", "bypass-offline").getBoolean();
    }

    public static boolean getMessageOffline() {
        return get().mainCfgNode.getNode("config", "broadcast-offline").getBoolean();
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


    public CM(ConfigurationLoader<CommentedConfigurationNode> loader) {
        instance = this;
        this.loader = loader;
    }

    public boolean loadConfig() {
        try {
            mainCfgNode = loader.load();
            U.info(CC.GREEN + "SV Configuration has been loaded.");
        } catch (IOException e) {
            U.error(CC.RED + "There was an error while reloading your configs");
            U.error(CC.RED_UNDERLINED + "PLEASE CHECK YOUR CONFIG FOR MISSING QUOTES, BRACKETS, OR COMMAS BEFORE ASKING FOR HELP!!");
            U.error(CC.YELLOW + CC.LINE);
            U.error(CC.RED + e.toString());
            return false;
        }
        return true;
    }

    public static CM get() {
        return instance;
    }


}
