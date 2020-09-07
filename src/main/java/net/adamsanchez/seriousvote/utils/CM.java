package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.loot.LootManager;
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
        return mainCfgNode.getNode("config", "debug-mode").getBoolean();
    }

    /////////////////////////////////////Dailies/////////////////////////////////////////////
    public static List<String> getWeeklySetCommands() {
        return mainCfgNode.getNode("config", "dailies", "weekly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getWeeklyRandomCommands() {
        return mainCfgNode.getNode("config", "dailies", "weekly", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static String getWeeklyMessage() {
        return mainCfgNode.getNode("config", "dailies", "weekly", "message").getString();
    }

    public static List<String> getMonthlySetCommands() {
        return mainCfgNode.getNode("config", "dailies", "monthly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getMonthlyRandomCommands() {
        return mainCfgNode.getNode("config", "dailies", "monthly", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static String getMonthlyMessage() {
        return mainCfgNode.getNode("config", "dailies", "monthly", "message").getString();
    }


    public static List<String> getYearlySetCommands() {
        return mainCfgNode.getNode("config", "dailies", "yearly", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getYearlyRandomCommands() {
        return mainCfgNode.getNode("config", "dailies", "yearly", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static String getYearlyMessage() {
        return mainCfgNode.getNode("config", "dailies", "yearly", "message").getString();
    }

    /////////////////////////////////////Milestones/////////////////////////////////////////////

    public static int[] getEnabledMilestones() {
        List<String> list = mainCfgNode.getNode("config", "milestones", "records-enabled").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
        int[] array = new int[list.size()];
        for (int ix = 0; ix < list.size(); ix++) {
            array[ix] = Integer.parseInt(list.get(ix));
        }

        return array;
    }

    public static List<String> getMilestoneRandomRewardByNumber(int number) {
        return mainCfgNode.getNode("config", "milestones", "records", "" + number, "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getMilestoneSetRewardByNumber(int number) {
        return mainCfgNode.getNode("config", "milestones", "records", "" + number, "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }


    public static String getMilestonesMessageByNumber(int number) {
        return mainCfgNode.getNode("config", "milestones", "records", "" + number, "message").getString();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////DATABASE/////////////////////////////////////////////
    public static String getDatabaseType() {
        return mainCfgNode.getNode("config", "database", "dbType").getString();
    }

    public static String getDatabaseName() {
        return mainCfgNode.getNode("config", "database", "name").getString();
    }

    public static String getDatabaseHostname() {
        return mainCfgNode.getNode("config", "database", "hostname").getString();
    }

    public static String getDatabasePort() {
        return mainCfgNode.getNode("config", "database", "port").getString();
    }

    public static String getDatabasePrefix() {
        return mainCfgNode.getNode("config", "database", "prefix").getString();
    }

    public static String getDatabaseUsername() {
        return mainCfgNode.getNode("config", "database", "username").getString();
    }

    public static String getDatabasePassword() {
        return mainCfgNode.getNode("config", "database", "password").getString();
    }

    public static String getMaxActiveConnections() {
        return mainCfgNode.getNode("config", "database", "maximum-active-connections").getString();
    }

    public static String getMinIdleConnections() {
        return mainCfgNode.getNode("config", "database", "minimum-idle-connections").getString();
    }

    //////////////////////////////////Modules/////////////////////////////////////////////////////////

    public static boolean getMilestonesEnabled() {
        return mainCfgNode.getNode("config", "milestones", "enabled").getBoolean();
    }

    public static boolean getDailiesEnabled() {
        return mainCfgNode.getNode("config", "dailies", "enabled").getBoolean();
    }

    public static boolean getMonthlyResetEnabled() {
        return mainCfgNode.getNode("config", "monthly-reset-enabled").getBoolean();
    }

    public static int getMonthlyResetDay() {
        return mainCfgNode.getNode("config", "monthly-reset-day").getInt();
    }

    public static boolean getMonthlyResetWithOffline() {
        return mainCfgNode.getNode("config", "monthly-reset-cache-enabled").getBoolean(true);
    }

    ////////////////////////////////Main Config///////////////////////////////////////////////////

    public static List<String> getSetCommands() {
        return mainCfgNode.getNode("config", "vote-reward", "set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static List<String> getRandomCommands() {
        return mainCfgNode.getNode("config", "vote-reward", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static int getRandomRewardsNumber() {
        return mainCfgNode.getNode("config", "random-rewards-number").getInt();
    }

    public static int getRandomMax() {
        return mainCfgNode.getNode("config", "rewards-max").getInt() + 1;
    }

    public static int getRandomMin() {
        return mainCfgNode.getNode("config", "rewards-min").getInt();
    }

    public static boolean getRandomDisabled() {
        return mainCfgNode.getNode("config", "random-rewards-number").getInt() == 0;
    }

    ////////////////////////////////Random////////////////////////////////////////////////////
    public static List<String> getLootSetByName(String lootTableName) {
        return mainCfgNode.getNode("config", "Tables", lootTableName).getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static boolean getAreLootTablesAvailable() {
        int size = getRandomCommands().size();
        if (size < 1) return false;
        if (size % 2 != 0) return false;
        return true;
    }

    public static List<String> getRewardsByID(String rewardIdentifier) {
        return mainCfgNode.getNode("config", "Rewards", rewardIdentifier, "rewards").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public static String getRewardNameById(String rewardIdentifier) {
        return mainCfgNode.getNode("config", "Rewards", rewardIdentifier, "name").getString();
    }
    ///////////////////////////////Vote Sites////////////////////////////////////////////////////

    public static List<String> getVoteSites() {
        return mainCfgNode.getNode("config", "vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }


    ///////////////////////////////Messages//////////////////////////////////////////////////////
    //Returns the string value from the Config for the public message. This must be deserialized
    public static String getPublicMessage() {
        return mainCfgNode.getNode("config", "broadcast-message").getString();
    }

    public static String getOfflineMessage() {
        return mainCfgNode.getNode("config", "broadcast-message-offline").getString();
    }

    public static boolean getMessageForOffline() {
        return mainCfgNode.getNode("config", "broadcast-offline").getBoolean();
    }

    public static String getVoteSiteMessage() {
        return mainCfgNode.getNode("config", "vote-sites-message").getString();
    }

    ///////////////////////////////Offline////////////////////////////////////////////////////
    public static boolean processIfOffline() {
        return mainCfgNode.getNode("config", "bypass-offline").getBoolean();
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

    public static boolean setDebugState(boolean booleanValue) {
        mainCfgNode.getNode("config", "debug-mode").setValue(booleanValue);
        saveConfig();
        return mainCfgNode.getNode("config", "debug-mode").getBoolean();
    }

    public static boolean updateConfigs(ConfigurationLoader<CommentedConfigurationNode> loader) {
        CM cm;
        if (CM.get() == null) {
            cm = new CM(loader);
        }
        return CM.loadConfig();
    }

    private static boolean saveConfig() {
        try {
            get().loader.save(mainCfgNode);
        } catch (Exception e) {
            U.error("Problem saving the config to file");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static boolean loadConfig() {
        try {
            mainCfgNode = get().loader.load();
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
