package net.adamsanchez.seriousvote;

import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import net.adamsanchez.seriousvote.Data.Milestones;
import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.commands.*;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.utils.ScheduleManager;
import net.adamsanchez.seriousvote.utils.U;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.Listener;

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import org.spongepowered.api.plugin.PluginContainer;

import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;


import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


/**
 * Created by adam_ on 12/08/16.
 */
@SuppressWarnings("unused")
@Plugin(id = "seriousvote",
        name = "SeriousVote",
        version = "4.8.7",
        description = "This plugin enables server admins to give players rewards for voting for their server.",
        dependencies = { @Dependency(id = "nuvotifier", optional = false), @Dependency(id = "placeholderapi",optional = true) })
public class SeriousVote {

    @Inject
    private Game game;

    private Game getGame() {
        return this.game;
    }

    @Inject
    private PluginContainer plugin;

    public PluginContainer getPlugin() {
        return this.plugin;
    }

    @Inject
    private Metrics metrics;
    private static SeriousVote instance;

    @Inject
    Logger logger;

    public Logger getLogger() {
        return logger;
    }


    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;
    private Path offlineVotes;
    private Path resetDatePath;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;
    private CommentedConfigurationNode mainCfgNode;

    ///////////////////////////////////////////////////////
    boolean milestonesEnabled = false, dailiesEnabled = false;
    private Milestones voteSpreeSystem;
    public List<String> monthlySet, yearlySet, weeklySet;
    int[] milestonesUsed;
    ///////////////////////////////////////////////////////
    public String databaseName, databaseHostname, databasePort, databasePrefix, databaseUsername, databasePassword, minIdleConnections, maxActiveConnections;
    ///////////////////////////////////////////////////////
    private LinkedList<String> commandQueue = new LinkedList<String>();
    private LinkedList<String> executingQueue = new LinkedList<String>();
    private LinkedList<Vote> voteQueue = new LinkedList<Vote>();
    private ScheduleManager scheduleManager;

    LinkedHashMap<Integer, List<Map<String, String>>> lootMap = new LinkedHashMap<Integer, List<Map<String, String>>>();

    //Stored Offline Votes
    HashMap<UUID, Integer> storedVotes = new HashMap<UUID, Integer>();
    int randomRewardsNumber;
    int rewardsMin;
    int rewardsMax;
    int randomRewardsGen;
    List<String> setCommands;
    String currentRewards;
    String publicMessage;
    String publicOfflineMessage;
    boolean debug = false;
    boolean hasLoot = false;
    boolean isNoRandom = false;
    boolean bypassOffline = false;
    boolean messageOffline = false;
    private static Optional<UserStorageService> userStorage;
    //////////////////////////////////////////////////////////////////

    String[][] mainRewardTables;
    private int chanceTotal, chanceMax, chanceMin = 0;
    private int[] chanceMap;

    @Listener
    public void onInitialization(GamePreInitializationEvent event) {
        instance = this;
        userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        CC.printSVInfo();
        getLogger().info(CC.YELLOW + "Trying To setup Config Loader");
        storedVotes = new HashMap<UUID, Integer>();
        offlineVotes = Paths.get(privateConfigDir.toString(), "", "offlinevotes.dat");
        resetDatePath = Paths.get(privateConfigDir.toString(),"", "lastReset");
        OfflineHandler.initOfflineStorage();
        CM.initConfig(defaultConfig);
        currentRewards = "";
        reloadConfigs();

    }

    @Listener
    public void onPostInitialization(GamePostInitializationEvent event) {
        instance = this;
    }

    @Listener
    public void onServerStart(GameInitializationEvent event) {
        CommandHandler.registerCommands();
        getLogger().info(CC.YELLOW + "Serious Vote Has Loaded");

        if (milestonesEnabled == true | dailiesEnabled == true) {
            voteSpreeSystem = new Milestones(mainCfgNode);
        } else {
            voteSpreeSystem = null;
        }

        //begin any scheduled tasks
        scheduleManager = new ScheduleManager().run();
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {

    }

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        PlaceHolders.initialize(Sponge.getPluginManager().fromInstance(this).get());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////CONFIGURATION METHODS//////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean reloadConfigs() {
        //try loading from file
        try {
            mainCfgNode = loader.load();
        } catch (IOException e) {
            U.error(CC.RED + "There was an error while reloading your configs");
            U.error(e.toString());
            return false;
        }

        //update variables and other instantiations
        debug = CM.getDebugMode(mainCfgNode);
        publicMessage = CM.getPublicMessage(mainCfgNode);
        publicOfflineMessage = CM.getOfflineMessage(mainCfgNode);
        bypassOffline = CM.getBypassOffline(mainCfgNode);
        messageOffline = CM.getMessageOffline(mainCfgNode);
        randomRewardsNumber = getRewardsNumber(mainCfgNode);
        updateLoot(mainCfgNode);
        setCommands = CM.getSetCommands(mainCfgNode);
        U.debug("Here's your commands");
        for (String ix : CM.getRandomCommands(mainCfgNode)) {
            U.debug(ix);
        }


        //Load Offline votes
        U.info(CC.YELLOW + "Trying to load offline player votes from ... " + offlineVotes.toString());
        try {
            storedVotes = OfflineHandler.loadOffline();
        } catch (EOFException e){
            storedVotes = new HashMap<>();
            try {
                U.debug("Trying to save corrected Map.");
                OfflineHandler.saveOffline();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            U.error(CC.RED + "ahahahahaha We Couldn't load up the stored offline player votes", e);
        } catch (ClassNotFoundException e) {
            U.error(CC.RED + "Well crap that is noooot a hash map! GO slap the dev!");
        }

        //Reload DB configuration
        databaseHostname = CM.getDatabaseHostname(mainCfgNode);
        databaseName = CM.getDatabaseName(mainCfgNode);
        databasePassword = CM.getDatabasePassword(mainCfgNode);
        databasePrefix = CM.getDatabasePrefix(mainCfgNode);
        databaseUsername = CM.getDatabaseUsername(mainCfgNode);
        databasePort = CM.getDatabasePort(mainCfgNode);
        minIdleConnections = CM.getMinIdleConnections(mainCfgNode);
        maxActiveConnections = CM.getMaxActiveConnections(mainCfgNode);

        milestonesEnabled = CM.getMilestonesEnabled(mainCfgNode);
        dailiesEnabled = CM.getDailiesEnabled(mainCfgNode);

        reloadDB();

        /////////Load Up Milestones/////////
        monthlySet = CM.getMonthlySetCommands(mainCfgNode);
        yearlySet = CM.getYearlySetCommands(mainCfgNode);
        weeklySet = CM.getWeeklySetCommands(mainCfgNode);
        milestonesUsed = CM.getEnabledMilestones(mainCfgNode);


        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    private int getRewardsNumber(ConfigurationNode node) {
        int number = node.getNode("config", "random-rewards-number").getInt();
        isNoRandom = number == 0 ? true : false;
        rewardsMin = node.getNode("config", "rewards-min").getInt();
        rewardsMax = node.getNode("config", "rewards-max").getInt() + 1;
        return number;
    }

    public int generateRandomRewardNumber() {
        int nextInt;
        if (randomRewardsNumber < 0) {
            //Inclusive
            if (rewardsMin < 0) rewardsMin = 0;
            if (rewardsMax > rewardsMin) {
                nextInt = ThreadLocalRandom.current().nextInt(rewardsMin, rewardsMax);
            } else {
                nextInt = 0;
                U.warn("There seems to be an error in your min/max setting in your configs.");
            }

            U.info("Giving out " + nextInt + " random rewards.");
            return nextInt;
        } else if (randomRewardsNumber < 0) {
            return 0;
        }
        return 0;
    }

    public void updateLoot(ConfigurationNode node) {
        List<String> nodeStrings = node.getNode("config", "vote-reward", "random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
        if (nodeStrings.size() < 1) {
            U.info("There are no random tables to load");
            return;
        }
        if (nodeStrings.size() % 2 != 0) {
            U.error("Please check the Config for your main random rewards, to make sure they are formatted correctly");
        } else {
            hasLoot = true;
            String[] inputLootSource = nodeStrings.stream().toArray(String[]::new);
            //Create a new Array of the proper size x*2 to hold the tables for choosing later
            String[][] table = new String[2][inputLootSource.length / 2];
            chanceMap = new int[inputLootSource.length / 2];
            U.info(CC.PURPLE + inputLootSource.length / 2 + CC.YELLOW + " Tables Imported for Rewards");

            for (int ix = 0; ix < inputLootSource.length; ix += 2) {
                table[0][ix / 2] = inputLootSource[ix];
                table[1][ix / 2] = inputLootSource[ix + 1];
                //Initialize chanceMap
                chanceMap[ix / 2] = Integer.parseInt(table[0][ix / 2]);
                if (ix != 0) {
                    chanceMap[ix / 2] += chanceMap[(ix / 2) - 1];

                }
            }
            mainRewardTables = table;
            chanceTotal = chanceMap.length - 1;
            chanceMin = chanceMap[0];
            chanceMax = chanceMap[chanceTotal];


        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////LISTENERS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    @Listener
    public synchronized void onVote(VotifierEvent event) {
        Vote vote = event.getVote();

        synchronized (voteQueue) {
            voteQueue.add(vote);
        }
    }

    public void processVotes() {
        LinkedList<Vote> localQueue = new LinkedList<>();
        synchronized (voteQueue) {
            localQueue.addAll(voteQueue);
            voteQueue.clear();
        }

        for (Vote vote : localQueue) {

            String username = vote.getUsername();
            U.debug("Vote Registered From " + vote.getServiceName() + " for " + username);
            String currentRewards = giveVote(username);
            if (!currentRewards.equals("offline")) {
                broadCastMessage(publicMessage, username, currentRewards);
            } else if (messageOffline && !bypassOffline){
                broadCastMessage(publicOfflineMessage, username);
            }


            if (voteSpreeSystem != null) {
                if (U.U.isOnline(username)) {
                    voteSpreeSystem.addVote(game.getServer().getPlayer(username).get().getUniqueId());
                } else {
                    if (userStorage.get().get(username).isPresent()) {
                        voteSpreeSystem.addVote(userStorage.get().get(username).get().getUniqueId());
                    }
                }
            }
        }
        executeCommands();
    }

    public void reloadDB() {
        if (dailiesEnabled || milestonesEnabled) {
            U.info("Attempting to reload database...");
            if (voteSpreeSystem != null) {
                voteSpreeSystem.shutdown();
            }
            voteSpreeSystem = new Milestones(mainCfgNode);
        }
        U.info("Attempting to reload database, but it is not enabled!");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        UUID playerID = event.getTargetEntity().getUniqueId();
        String username = event.getTargetEntity().getName();

        if (storedVotes.containsKey(playerID)) {

            String rewardString = "";
            for (int ix = 0; ix < storedVotes.get(playerID).intValue(); ix++) {
                rewardString = giveVote(username);
            }

            broadCastMessage(publicMessage, username, rewardString);
            currentRewards = "";

            storedVotes.remove(playerID);
            try {
                OfflineHandler.saveOffline();
            } catch (IOException e) {
                U.error("Error while saving offline votes file", e);
            }
        }


    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    public void executeCommands() {
        for (String command : commandQueue) {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }
        commandQueue.clear();

    }

    public boolean giveReward(List<String> commands) {
        //Execute Commands
        for (String command : commands) {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }
        return true;
    }

    public String chooseTable() {
        //compare
        int roll = U.roll(chanceMax);
        int currentChoice = -1;
        for (int ix = 0; ix < chanceMap.length; ix++) {
            if (roll <= chanceMap[ix]) {
                currentChoice = ix;
                break;
            }
        }

        if (currentChoice < 0) U.error("There was a problem while rolling something might be broken");
        String chosenReward = mainRewardTables[1][currentChoice];
        return chosenReward;
    }

    public String giveVote(String username) {

        if (U.isOnline(username) || bypassOffline) {
            LootTable mainLoot;
            currentRewards = "";
            ArrayList<String> commandQueue = new ArrayList<String>();
            if (hasLoot && !isNoRandom && randomRewardsNumber >= 1) {
                for (int i = 0; i < randomRewardsNumber; i++) {
                    mainLoot = new LootTable(chooseTable(), mainCfgNode);
                    U.debug("Choosing a random reward.");
                    String chosenReward = mainLoot.chooseReward();

                    currentRewards = currentRewards + mainCfgNode.getNode("config", "Rewards", chosenReward, "name").getString() + ", ";
                    for (String ix : mainCfgNode.getNode("config", "Rewards", chosenReward, "rewards").getChildrenList().stream()
                            .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                        commandQueue.add(parseVariables(ix, username));
                    }
                }
            } else if (hasLoot && !isNoRandom) {
                randomRewardsGen = generateRandomRewardNumber();
                for (int i = 0; i < randomRewardsGen; i++) {
                    mainLoot = new LootTable(chooseTable(), mainCfgNode);
                    U.debug("Choosing a random reward.");

                    String chosenReward = mainLoot.chooseReward();
                    currentRewards = currentRewards + mainCfgNode.getNode("config", "Rewards", chosenReward, "name").getString() + ", ";
                    for (String ix : mainCfgNode.getNode("config", "Rewards", chosenReward, "rewards").getChildrenList().stream()
                            .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                        commandQueue.add(parseVariables(ix, username));


                    }
                }

            }
            //Get Set Rewards
            U.debug("Adding SetCommands to the process queue");
            for (String setCommand : setCommands) {
                commandQueue.add(parseVariables(setCommand, username, currentRewards));
                U.debug("Will process the following commands: " + setCommand);
            }
            this.commandQueue.addAll(commandQueue);

            return currentRewards;
        } else {
            UUID playerID = U.getIdFromName(username);
            if (playerID != null) {
                //Write to File
                if (storedVotes.containsKey(playerID)) {
                    storedVotes.put(playerID, storedVotes.get(playerID).intValue() + 1);

                } else {
                    storedVotes.put(playerID, new Integer(1));
                }
                try {
                    OfflineHandler.saveOffline();
                } catch (IOException e) {
                    U.error("Woah did that just happen? I couldn't save that offline player's vote!", e);
                }
            }
            return "offline";
        }
    }


    public boolean broadCastMessage(String message, String username) {

        if (message == null || message.isEmpty() || message == "" ) return false;
        game.getServer().getBroadcastChannel().send(
                TextSerializers.FORMATTING_CODE.deserialize(parseVariables(message, username)));
        return true;
    }

    public boolean broadCastMessage(String message, String username, String currentRewards) {

        if(!U.isOnline(username)) return false;
        if (message == null || message.isEmpty() || message == "" ) return false;
        game.getServer().getBroadcastChannel().send(
                TextSerializers.FORMATTING_CODE.deserialize(parseVariables(message, username, currentRewards)));
        return true;
    }

    public Text convertLink(String link) {
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize(link);
        try {
            return textLink.toBuilder().onClick(TextActions.openUrl(new URL(textLink.toPlain()))).build();
        } catch (MalformedURLException e) {
            U.error("Malformed URL");
            U.error(e.toString());
        }
        return Text.of("Malformed URL - Inform Administrator");
    }

    public String parseVariables(String string, String username) {
        return string.replace("{player}", username);
    }

    public String parseVariables(String string, String username, String currentRewards) {
        if (isNoRandom) {
            return parseVariables(string, username);
        } else if (currentRewards == "") {
            return string.replace("{player}", username).replace("{rewards}", "No Random Rewards");
        }
        return string.replace("{player}", username).replace("{rewards}", currentRewards.substring(0, currentRewards.length() - 2));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////Utilities/////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    //returns weather a player is online




    public static SeriousVote getInstance() {
        return instance;
    }

    public static Optional<UserStorageService> getUserStorage() {
        return userStorage;
    }

    public Game getPublicGame() {
        return this.getGame();
    }

    public boolean isDailiesEnabled() {
        return dailiesEnabled;
    }

    public boolean usingMilestones() {
        if (voteSpreeSystem != null) return true;
        return false;
    }

    public boolean isMilestonesEnabled() {
        return milestonesEnabled;
    }

    public boolean isDebug(){
        return debug;
    }

    public boolean toggleDebug(){
        debug = !debug;
        return debug;
    }

    public Milestones getVoteSpreeSystem() {
        return voteSpreeSystem;
    }

    public HashMap<UUID, Integer> getStoredVotes() {
        return storedVotes;
    }

    public Path getOfflineVotes(){
        return offlineVotes;
    }

    public Path getResetDatePath(){
        return resetDatePath;
    }

    public void resetCurrentRewards() {
        currentRewards = "";
    }

    public boolean hasUnprocessedVotes(){
        return !voteQueue.isEmpty();
    }
    public CommentedConfigurationNode getMainCfgNode() {
        return mainCfgNode;
    }

}
