package net.adamsanchez.seriousvote;

import com.google.inject.Inject;

import com.vexsoftware.votifier.sponge.event.VotifierEvent;

import net.adamsanchez.seriousvote.Data.VoteSpreeSystem;
import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.commands.*;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import net.adamsanchez.seriousvote.loot.LootTable;
import net.adamsanchez.seriousvote.utils.*;
import net.adamsanchez.seriousvote.vote.Status;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

import org.spongepowered.api.event.game.GameReloadEvent;
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

import java.io.*;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;


import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by adam_ on 12/08/16.
 */
@SuppressWarnings("unused")
@Plugin(id = "seriousvote",
        name = "SeriousVote",
        version = "4.8.9",
        description = "This plugin enables server admins to give players rewards for voting for their server.",
        dependencies = {@Dependency(id = "nuvotifier", optional = false), @Dependency(id = "placeholderapi", optional = true)})
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

    private static SeriousVote instance;

    @Inject
    Logger logger;

    public Logger getLogger() {
        return logger;
    }

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;
    private Path offlineVotesPath;
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
    private VoteSpreeSystem voteSpreeSystem;
    int[] milestonesUsed;
    ///////////////////////////////////////////////////////
    public String databaseType, databaseName, databaseHostname, databasePort, databasePrefix, databaseUsername, databasePassword, minIdleConnections, maxActiveConnections;
    ///////////////////////////////////////////////////////
    private LinkedList<VoteRequest> processedVoteQueue = new LinkedList<VoteRequest>();
    private LinkedList<String> executingQueue = new LinkedList<String>();
    private List<VoteRequest> voteQueue = new LinkedList<VoteRequest>();
    private ScheduleManager scheduleManager;

    LinkedHashMap<Integer, List<Map<String, String>>> lootMap = new LinkedHashMap<Integer, List<Map<String, String>>>();

    //Stored Offline Votes
    HashMap<String, Integer> offlineVotes = new HashMap<String, Integer>();
    int numRandRewards;
    int minRandRewards;
    int maxRandRewards;
    String publicMessage;
    String publicOfflineMessage;
    boolean debug = false;
    boolean lootTablesAvailable = false;
    boolean randomDisabled = false;
    boolean processIfOffline = false;
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
        offlineVotes = new HashMap<String, Integer>();
        offlineVotesPath = Paths.get(privateConfigDir.toString(), "", "offlinevotes.dat");
        resetDatePath = Paths.get(privateConfigDir.toString(), "", "lastReset");
        OfflineHandler.initOfflineStorage();
        CM.initConfig(defaultConfig);
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
            voteSpreeSystem = new VoteSpreeSystem(mainCfgNode);
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

    @Listener
    public void reload(GameReloadEvent event) {
        reloadConfigs();
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
            U.error(CC.RED_UNDERLINED + "PLEASE CHECK YOUR CONFIG FOR MISSING QUOTES, BRACKETS, OR COMMAS BEFORE ASKING FOR HELP!!");
            U.error(CC.YELLOW + CC.LINE);
            U.error(CC.RED + e.toString());
            return false;
        }

        //update variables and other instantiations
        debug = CM.getDebugMode(mainCfgNode);
        publicMessage = CM.getPublicMessage(mainCfgNode);
        publicOfflineMessage = CM.getOfflineMessage(mainCfgNode);
        processIfOffline = CM.getBypassOffline(mainCfgNode);
        messageOffline = CM.getMessageOffline(mainCfgNode);
        numRandRewards = updateRewardsNumbers(mainCfgNode);
        updateLoot(mainCfgNode);
        U.debug("Here's your commands");
        for (String ix : CM.getRandomCommands(mainCfgNode)) {
            U.debug(ix);
        }


        //Load Offline votes
        U.info(CC.YELLOW + "Trying to load offline player votes from ... " + offlineVotesPath.toString());
        try {
            offlineVotes = OfflineHandler.loadOffline();
        } catch (EOFException e) {
            offlineVotes = new HashMap<>();
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
        databaseType = CM.getDatabaseType(mainCfgNode);
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

        /////////Load Up VoteSpreeSystem/////////
        milestonesUsed = CM.getEnabledMilestones(mainCfgNode);

        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    private int updateRewardsNumbers(ConfigurationNode node) {
        int number = node.getNode("config", "random-rewards-number").getInt();
        randomDisabled = number == 0 ? true : false;
        minRandRewards = node.getNode("config", "rewards-min").getInt();
        maxRandRewards = node.getNode("config", "rewards-max").getInt() + 1;
        return number;
    }


    /**
     * Checks for and imports the random loot settings from the config. It creates chanceMaps of tables.
     *
     * @param node The base config node used for checking for loot.
     */
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
            lootTablesAvailable = true;
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
        //Workflow Stage 1
        synchronized (voteQueue) {
            voteQueue.add(new VoteRequest(event.getVote()));
        }
    }

    public void processVotes() {
        //Workflow Stage 2 - Timed Task Main thread
        List<VoteRequest> localQueue = new LinkedList<>();
        synchronized (voteQueue) {
            localQueue.addAll(voteQueue);
            voteQueue.clear();
        }

        //TODO Determine validity of vote -> if offline / online - if found in system
        //  Separate out the loot gathering code (command making)
        //  Separate out Online checking and add state

        for (VoteRequest vr : localQueue) {
            VoteRequest workingRequest = vr;
            workingRequest.setVoteStatus(Status.IN_PROCESS);
            ///NEW CODE ////
            //Get Online Status
            workingRequest = workflowOnlineState(workingRequest);

            //Use online status to decide which route to take
            switch (workingRequest.getVoteStatus()) {
                case BUILD_ONLINE:
                    //if online gather rewards
                    workingRequest.setVoteStatus(Status.GATHER_REWARDS);
                    workingRequest = processVoteChanceTables(workingRequest);
                    break;
                case BUILD_OFFLINE:
                    if (processIfOffline) {
                        workingRequest.setVoteStatus(Status.GATHER_REWARDS);
                        workingRequest = processVoteChanceTables(workingRequest);
                    } else {
                        workingRequest.setVoteStatus(Status.SAVE_OFFLINE);
                        workingRequest = storeOfflineVote(workingRequest);
                        if (workingRequest.getVoteStatus() == Status.ERROR) {
                            U.error("There was an error processing that last vote.");
                        }
                    }
                    break;
                default:
                    U.error("Something was wrong with that vote? Was it offline?");
                    continue;
            }

            //Take the state changed request and see whether to broadcast a message or not.
            switch (workingRequest.getVoteStatus()) {
                case REWARDS_GATHERED:
                    OutputHelper.broadCastMessage(publicMessage, vr.getUsername(), U.listMaker(workingRequest.getRewardNames()));
                    break;
                case OFFLINE_SAVED:
                    OutputHelper.broadCastMessage(publicOfflineMessage, vr.getUsername());
                    break;
                default:
                    U.error("Error with that vote's state...Uh Oh!");
                    continue;
            }

            if (voteSpreeSystem != null) {
                if (U.isPlayerOnline(vr.getUsername())) {
                    voteSpreeSystem.addVote(U.getPlayerIdentifier(vr.getUsername()));
                } else {
                    if (userStorage.get().get(vr.getUsername()).isPresent()) {
                        voteSpreeSystem.addVote(U.getPlayerIdentifier(vr.getUsername()));
                    }
                }
            }
            workingRequest.setVoteStatus(Status.COMPLETED);
            processedVoteQueue.add(workingRequest);
        }
        executeCommands();
    }

    public VoteRequest workflowOnlineState(VoteRequest vr) {
        if (U.isPlayerOnline(vr.getUsername())) {
            vr.setVoteStatus(Status.BUILD_ONLINE);
        } else {
            vr.setVoteStatus(Status.BUILD_OFFLINE);
        }
        return vr;
    }

    public void reloadDB() {
        if (dailiesEnabled || milestonesEnabled) {
            U.info("Attempting to reload database...");
            if (voteSpreeSystem != null) {
                voteSpreeSystem.shutdown();
            }
            voteSpreeSystem = new VoteSpreeSystem(mainCfgNode);
            return;
        }
        U.info("Attempting to reload database, but it is not enabled!");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        String playerID = event.getTargetEntity().getUniqueId().toString();
        String username = event.getTargetEntity().getName();

        if (offlineVotes.containsKey(username)) {
            U.debug("Offline votes found for player with ID " + playerID);
            List<VoteRequest> voteCollection = new LinkedList<VoteRequest>();
            for (int ix = 0; ix < offlineVotes.get(username).intValue(); ix++) {
                VoteRequest workingRequest = new VoteRequest();
                workingRequest.setUsername(username);
                voteCollection.add(processVoteChanceTables(workingRequest));
            }
            //Collect all the reward names into one to prevent spam.
            Set<String> rewardNames = new HashSet<String>();
            for(VoteRequest vr: voteCollection){
                rewardNames.addAll(vr.getRewardNames());
            }

            OutputHelper.broadCastMessage(publicMessage, username, U.listMaker(rewardNames));
            processedVoteQueue.addAll(voteCollection);
            offlineVotes.remove(username);
            executeCommands();
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
        U.debug(CC.CYAN + "Emptying Queue");
        for (VoteRequest vr : processedVoteQueue) {
            for (String command : vr.getRewards()) {
                game.getCommandManager().process(game.getServer().getConsole(), command);
            }
        }
        processedVoteQueue.clear();
    }

    public VoteRequest processVoteChanceTables(VoteRequest vr) {
        //Workflow Level 4
        VoteRequest workingRequest = vr;
        U.debug("Adding SetCommands to the process queue");
        for (String setCommand : CM.getSetCommands(mainCfgNode)) {
            workingRequest.addReward(OutputHelper.parseVariables(setCommand, workingRequest.getUsername()));
        }
        if (!lootTablesAvailable || randomDisabled) {
            workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
            return workingRequest;
        }
        //Setup Loot Table and gather rewards
        int maxNumberOfRewards = LootTools.genNumRandRewards(numRandRewards, minRandRewards, maxRandRewards);
        for (int i = 0; i < maxNumberOfRewards; i++) {
            LootTable mainLoot = new LootTable(LootTools.chooseTable(chanceMap, mainRewardTables), mainCfgNode);
            U.debug("Choosing a random reward.");
            String chosenReward = mainLoot.chooseReward();
            U.debug("Chose: " + chosenReward);
            workingRequest.addRewardName(mainCfgNode.getNode("config", "Rewards", chosenReward, "name").getString());
            for (String ix : mainCfgNode.getNode("config", "Rewards", chosenReward, "rewards").getChildrenList().stream()
                    .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                workingRequest.addReward(OutputHelper.parseVariables(ix, workingRequest.getUsername()));
                U.debug(CC.YELLOW + "QUEUED: " + CC.WHITE + ix);
            }
        }
        workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
        return workingRequest;
    }

    /**
     * This processes loot tables and then moves to the  command execution workflow
     */
    public void forceGiveVote(String username) {
        VoteRequest workingRequest = new VoteRequest();
        workingRequest.setUsername(username);
        workingRequest = processVoteChanceTables(workingRequest);
        processedVoteQueue.add(workingRequest);
        executeCommands();
    }

    public VoteRequest storeOfflineVote(VoteRequest vr) {
        VoteRequest workingRequest = vr;
        if (workingRequest.getUsername() != null) {
            //Write to File
            if (offlineVotes.containsKey(workingRequest.getUsername())) {
                offlineVotes.put(workingRequest.getUsername(), offlineVotes.get(workingRequest.getUsername()).intValue() + 1);
            } else {
                offlineVotes.put(workingRequest.getUsername(), new Integer(1));
            }
            try {
                OfflineHandler.saveOffline();
                workingRequest.setVoteStatus(Status.OFFLINE_SAVED);
            } catch (IOException e) {
                U.error("Woah did that just happen? I couldn't save that offline player's vote!", e);
                workingRequest.setVoteStatus(Status.ERROR);
            }
        } else {
            U.error("That vote didn't have a playername :(");
            workingRequest.setVoteStatus(Status.ERROR);
        }
        return workingRequest;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////Accessors and Modifiers/////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public static SeriousVote getInstance() {
        return instance;
    }

    public static Optional<UserStorageService> getUserStorage() {
        return userStorage;
    }

    public static Game getPublicGame() {
        return getInstance().getGame();
    }

    public boolean isDailiesEnabled() {
        return dailiesEnabled;
    }

    public boolean usingVoteSpreeSystem() {
        if (voteSpreeSystem != null) return true;
        return false;
    }

    public boolean isMilestonesEnabled() {
        return milestonesEnabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean toggleDebug() {
        debug = !debug;
        return debug;
    }

    public void setDebug(boolean _debug) {
        debug = _debug;
    }

    public VoteSpreeSystem getVoteSpreeSystem() {
        return voteSpreeSystem;
    }

    public HashMap<String, Integer> getOfflineVotes() {
        return offlineVotes;
    }

    public static boolean isServerOnline() {
        return getPublicGame().getServer().getOnlineMode();
    }

    public Path getOfflineVotesPath() {
        return offlineVotesPath;
    }

    public void triggerSave() {
        try {
            OfflineHandler.saveOffline();
        } catch (IOException e) {
            U.debug("Could not save file in a triggered save :(");
            e.printStackTrace();
        }
    }

    public Path getResetDatePath() {
        return resetDatePath;
    }

    public Path getSQLDumpPath() {
        return Paths.get(privateConfigDir.toString(), "", "sqlExport.csv");
    }

    public boolean hasUnprocessedVotes() {
        return !voteQueue.isEmpty();
    }

    public CommentedConfigurationNode getMainCfgNode() {
        return mainCfgNode;
    }

}
