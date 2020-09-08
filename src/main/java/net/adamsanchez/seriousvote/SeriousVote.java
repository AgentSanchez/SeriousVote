package net.adamsanchez.seriousvote;

import com.google.inject.Inject;


import com.vexsoftware.votifier.sponge.event.VotifierEvent;
import net.adamsanchez.seriousvote.Data.VoteSpreeSystem;
import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.commands.*;
import net.adamsanchez.seriousvote.integration.PlaceHolders;
import net.adamsanchez.seriousvote.loot.LootManager;
import net.adamsanchez.seriousvote.loot.LootProcessor;
import net.adamsanchez.seriousvote.utils.*;
import net.adamsanchez.seriousvote.vote.Status;
import net.adamsanchez.seriousvote.vote.VoteRequest;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;

import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
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


/**
 * Created by adam_ on 12/08/16.
 */
//@SuppressWarnings("unused")
@Plugin(id = "seriousvote",
        name = "SeriousVote",
        version = "4.9.0",
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

    private VoteSpreeSystem voteSpreeSystem;
    ///////////////////////////////////////////////////////
    private LinkedList<VoteRequest> processedVoteQueue = new LinkedList<VoteRequest>();
    private List<VoteRequest> voteQueue = new LinkedList<VoteRequest>();
    private ScheduleManager scheduleManager;

    //Stored Offline Votes
    HashMap<String, Integer> offlineVotes = new HashMap<String, Integer>();

    private static Optional<UserStorageService> userStorage;
    //////////////////////////////////////////////////////////////////



    @Listener
    public void onPreInit(GamePreInitializationEvent event){
        instance = this;
        userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
        getLogger().info(CC.YELLOW + "Trying To setup Config Loader");
        offlineVotes = new HashMap<String, Integer>();
        offlineVotesPath = Paths.get(privateConfigDir.toString(), "", "offlinevotes.dat");
        resetDatePath = Paths.get(privateConfigDir.toString(), "", "lastReset");
        OfflineHandler.initOfflineStorage();
        CM.initConfig(defaultConfig);
        reloadConfigs();
        CC.printSVLogo();
        CC.printSVInfo();
    }

    @Listener
    public void onServerStart(GameInitializationEvent event) {
        CommandHandler.registerCommands();
        getLogger().info(CC.YELLOW + "Serious Vote Has Loaded");

        if (CM.getMilestonesEnabled() == true | CM.getDailiesEnabled() == true) {
            voteSpreeSystem = new VoteSpreeSystem();
        } else {
            voteSpreeSystem = null;
        }

        //begin any scheduled tasks
        scheduleManager = new ScheduleManager().run();

        //Enable Metrics
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

        //TODO replace this with CM loadConfig
        //try loading from file
        if (!CM.updateConfigs(loader)) return false;
        LootManager.updateLoot();

        U.debug("Here's your commands");
        for (String ix : CM.getRandomCommands()) {
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
            U.error(CC.RED + "We Couldn't load up the stored offline player votes", e);
        } catch (ClassNotFoundException e) {
            U.error(CC.RED + "Well crap that is noooot a hash map! GO slap the dev!");
        }
        reloadDB();

        return true;
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

        for (VoteRequest vr : localQueue) {
            VoteRequest workingRequest = vr;
            workingRequest.setVoteStatus(Status.IN_PROCESS);

            //Get Online Status
            workingRequest = workflowOnlineState(workingRequest);

            //Use online status to decide which route to take
            switch (workingRequest.getVoteStatus()) {
                case BUILD_ONLINE:
                    //if online gather rewards
                    workingRequest.setVoteStatus(Status.GATHER_REWARDS);
                    workingRequest = LootProcessor.processChanceTables(workingRequest);
                    break;
                case BUILD_OFFLINE:
                    if (CM.processIfOffline()) {
                        workingRequest.setVoteStatus(Status.GATHER_REWARDS);
                        workingRequest = LootProcessor.processChanceTables(workingRequest);
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
            U.debug("Current vote workflow state: " + workingRequest.getVoteStatus());
            switch (workingRequest.getVoteStatus()) {
                case REWARDS_GATHERED:
                    OutputHelper.broadCastMessage(CM.getPublicMessage(), vr);
                    break;
                case OFFLINE_SAVED:
                    if (CM.getOfflineVoteMessageEnabled()) {
                        OutputHelper.broadCastMessage(CM.getOfflineMessage(), vr);
                    }
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
        if (CM.getDailiesEnabled() || CM.getMilestonesEnabled()) {
            U.info("Attempting to reload database...");
            if (voteSpreeSystem != null) {
                voteSpreeSystem.shutdown();
            }
            voteSpreeSystem = new VoteSpreeSystem();
            return;
        }
        U.info("Attempting to reload database, but it is not enabled!");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        String playerID = event.getTargetEntity().getUniqueId().toString();
        String username = event.getTargetEntity().getName();

        /*
        if (event.getTargetEntity().hasPermission("seriousvote.commands.admin.metrics")) {
            if (!areMetricsEnabled()) {
                event.getTargetEntity().sendMessage(OutputHelper.strToText(
                        "&6SeriousVote does not have metrics enabled!! " +
                                "Metrics help me gauge how much usage there is for the plugin, and encourages me to produce better content." +
                                "You can do /svmetrics to enable them now!"
                ));
            }
        }
        */

        if (offlineVotes.containsKey(username)) {
            U.debug("Offline votes found for player with ID " + playerID);
            List<VoteRequest> voteCollection = new LinkedList<VoteRequest>();
            for (int ix = 0; ix < offlineVotes.get(username).intValue(); ix++) {
                VoteRequest workingRequest = new VoteRequest();
                workingRequest.setUsername(username);
                workingRequest.setServiceName("offline");
                voteCollection.add(LootProcessor.processChanceTables(workingRequest));
            }
            //Collect all the reward names into one to prevent spam.
            Set<String> rewardNames = new HashSet<String>();
            for (VoteRequest vr : voteCollection) {
                rewardNames.addAll(vr.getRewardNames());
            }

            VoteRequest collectedVoteRequest = new VoteRequest();
            collectedVoteRequest.setUsername(username);
            collectedVoteRequest.setServiceName("offline");
            collectedVoteRequest.replaceRewardNames(rewardNames);


            OutputHelper.broadCastMessage(CM.getPublicMessage(), collectedVoteRequest);
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
                game.getCommandManager().process(game.getServer().getConsole(), OutputHelper.parseAllVariables(command, vr));
            }
        }
        processedVoteQueue.clear();
    }


    /**
     * This processes loot tables and then moves to the  command execution workflow
     */
    public void forceGiveVote(String username) {
        VoteRequest workingRequest = new VoteRequest();
        workingRequest.setUsername(username);
        workingRequest = LootProcessor.processChanceTables(workingRequest);
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

    public boolean usingVoteSpreeSystem() {
        if (voteSpreeSystem != null) return true;
        return false;
    }

    public boolean isDebug() {
        return CM.getDebugMode();
    }

    public boolean toggleDebug() {
        return CM.setDebugState(!CM.getDebugMode());
    }

    /*
    public boolean toggleMetrics() {
        boolean state = areMetricsEnabled();
        U.debug("Metrics are currently " + state);
        U.debug("Metrics inverse is " + !state + " We should be returning" + !state);
        game.getCommandManager().process(U.getConsole().getCommandSource().get(), "sponge metrics seriousvote " + !state);
        return !state;
    }
    */


    public boolean areMetricsEnabled() {
        //return metricsConfigManager.getCollectionState(plugin).asBoolean();
        return false;
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


}
