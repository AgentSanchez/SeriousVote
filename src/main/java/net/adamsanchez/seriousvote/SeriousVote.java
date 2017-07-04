package net.adamsanchez.seriousvote;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import jdk.nashorn.internal.runtime.regexp.joni.Config;
import ninja.leaping.configurate.ConfigurationNode;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;

import org.spongepowered.api.entity.living.player.Player;


import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.Listener;

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;

import org.spongepowered.api.plugin.PluginContainer;

import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;


import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



/**
 * Created by adam_ on 12/08/16.
 */
@SuppressWarnings("unused")
@Plugin(id = "seriousvote", name = "SeriousVote", version = "3.0", description = "This plugin enables server admins to give players rewards for voting for their server.", dependencies = @Dependency(id = "nuvotifier", version = "1.0", optional = false) )
public class SeriousVote
{

    @Inject private Game game;
    private Game getGame(){
        return this.game;
    }

    @Inject private PluginContainer plugin;
    private PluginContainer getPlugin(){
        return this.plugin;
    }
    @Inject
    private Metrics metrics;
    private static SeriousVote instance;

    private static SeriousVote seriousVotePlugin;


    @Inject  Logger logger;
    public Logger getLogger()
    {
        return logger;
    }


    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;
    private Path offlineVotes;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;
    private CommentedConfigurationNode rootNode;

    ///////////////////////////////////////////////////////
    private Milestones milestones;
    public List<String> monthlySet, yearlySet, weeklySet;
    ///////////////////////////////////////////////////////
    public String databaseName, databaseHostname,databasePort,databasePrefix,databaseUsername,databasePassword;
    ///////////////////////////////////////////////////////
    private LinkedList<String> commandQueue = new LinkedList<String>();
    private LinkedList<String> executingQueue = new LinkedList<String>();


    LinkedHashMap<Integer, List<Map<String, String>>> lootMap = new LinkedHashMap<Integer, List<Map<String,String>>>();
    HashMap<UUID,Integer> storedVotes = new HashMap<UUID,Integer>();
    int randomRewardsNumber;
    int rewardsMin;
    int rewardsMax;
    int randomRewardsGen;
    List<String> setCommands;
    String currentRewards;
    String publicMessage;
    boolean hasLoot = false;
    boolean isNoRandom = false;
    private static Optional<UserStorageService> userStorage;
 //////////////////////////////////////////////////////////////////

    String[][] mainRewardTables;
    private int chanceTotal,chanceMax, chanceMin = 0;
    private int[] chanceMap;





    @Listener
    public void onInitialization(GamePreInitializationEvent event){
        instance = this;
        userStorage = Sponge.getServiceManager().provide(UserStorageService.class);

        getLogger().info("Serious Vote loading...");
        getLogger().info("Trying To setup Config Loader");

        Asset configAsset = plugin.getAsset("seriousvote.conf").orElse(null);
        Asset offlineVoteAsset = plugin.getAsset("offlinevotes.dat").orElse(null);

        offlineVotes = Paths.get(privateConfigDir.toString(),"", "offlinevotes.dat");





        if (Files.notExists(defaultConfig)) {
            if (configAsset != null) {
                try {
                    getLogger().info("Copying Default Config");
                    getLogger().info(configAsset.readString());
                    getLogger().info(defaultConfig.toString());
                    configAsset.copyToFile(defaultConfig);
                } catch (IOException e) {
                    e.printStackTrace();
                    getLogger().error("Could not unpack the default config from the jar! Maybe your Minecraft server doesn't have write permissions?");
                    return;
                }
            } else {
                getLogger().error("Could not find the default config file in the jar! Did you open the jar and delete it?");
                return;
            }
        }

        if (Files.notExists(offlineVotes)){
            try {
                saveOffline();
            } catch (IOException e) {
                getLogger().error("Could Not Initialize the offlinevotes file! What did you do with it");
                //getLogger().error(e.toString());
            }
        }



        currentRewards = "";

        reloadConfigs();

        //Begin Command Executor
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        Task task = taskBuilder.execute(() -> giveReward())
                .delay(1000, TimeUnit.MILLISECONDS)
                .name("SeriousVote-CommandRewardExecutor")
                .submit(plugin);



    }


    @Listener
    public void onPostInitalization(GamePostInitializationEvent event){
        instance = this;
    }

    @Listener
    public void onServerStart(GameInitializationEvent event)
    {
        seriousVotePlugin = this;
        registerCommands();
        getLogger().info("Serious Vote Has Loaded\n\n\n\n");

        if(!(databaseHostname=="" || databaseHostname == null)){
            milestones = new Milestones();
        } else {
            milestones = null;
        }



    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event)
    {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////COMMAND MANAGER//////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void registerCommands(){

        //////////////////////COMMAND BUILDERS///////////////////////////////////////////////
        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("Reload your configs for seriousvote"))
                .permission("seriousvote.commands.admin.reload")
                .executor(new SVoteReload())
                .build();
        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("seriousvote.commands.vote")
                .executor(new SVoteVote())
                .build();

        CommandSpec giveVote = CommandSpec.builder()
                .description(Text.of("For admins to give a player a vote"))
                .permission("seriousvote.commands.admin.give")
                .arguments(GenericArguments.onlyOne(GenericArguments.player(Text.of("player"))))
                .executor(new SVoteGiveVote())
                .build();

        //////////////////////////COMMAND REGISTER////////////////////////////////////////////
        Sponge.getCommandManager().register(this, vote, "vote");
        Sponge.getCommandManager().register(this, reload,"svreload","seriousvotereload");
        Sponge.getCommandManager().register(this, giveVote, "givevote" );
    }

    //////////////////////////////COMMAND EXECUTOR CLASSES/////////////////////////////////////
    public class SVoteReload implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            if (reloadConfigs()) {
                src.sendMessage(Text.of("Reloaded successfully!"));
            } else {
                src.sendMessage(Text.of("Could not reload properly :( did you break your config?"));
            }
            return CommandResult.success();
        }
    }

    public class SVoteGiveVote implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

            Player player = args.<Player>getOne("player").get();

            player.sendMessage(Text.of("An administrator has awarded you a vote!"));
            giveVote(player.getName());
            currentRewards = "";
            src.sendMessage(Text.of("You have successfully given " + player.getName() + " a vote"));


            return CommandResult.success();
        }
    }

    public class SVoteVote implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            src.sendMessage(Text.of("Thank You! Below are the places you can vote!").toBuilder().color(TextColors.GOLD).build());
            ConfigUtil.getVoteSites(rootNode).forEach(site -> {
                src.sendMessage(convertLink(site));
            });
            return CommandResult.success();



        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////CONFIGURATION METHODS//////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    public boolean reloadConfigs(){
        //try loading from file
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            U.error("There was an error while reloading your configs");
            U.error(e.toString());
            return false;
        }

        //update variables and other instantiations
        publicMessage = ConfigUtil.getPublicMessage(rootNode);
        randomRewardsNumber = getRewardsNumber(rootNode);
        updateLoot(rootNode);
        setCommands = ConfigUtil.getSetCommands(rootNode);
        U.debug("Here's your commands");
        for(String ix : ConfigUtil.getRandomCommands(rootNode)){
            U.debug(ix);
        }


        //Load Offline votes
        U.info("Trying to load offline player votes from ... " + offlineVotes.toString());
        try {
            loadOffline();
        } catch (IOException e) {
            U.error("ahahahahaha We Couldn't load up the stored offline player votes",e);
        } catch (ClassNotFoundException e) {
            U.error("Well crap that is noooot a hash map! GO slap the dev!");
        }

        //Reload DB configuration
        databaseHostname = ConfigUtil.getDatabaseHostname(rootNode);
        databaseName = ConfigUtil.getDatabaseName(rootNode);
        databasePassword = ConfigUtil.getDatabasePassword(rootNode);
        databasePrefix = ConfigUtil.getDatabasePrefix(rootNode);
        databaseUsername = ConfigUtil.getDatabaseUsername(rootNode);
        databasePort = ConfigUtil.getDatabasePort(rootNode);

        if (milestones != null){
            milestones.reloadDB();

        }
        /////////Load Up Milestones/////////
        monthlySet = ConfigUtil.getMonthlySetCommands(rootNode);
        yearlySet = ConfigUtil.getYearlySetCommands(rootNode);
        weeklySet = ConfigUtil.getWeeklySetCommands(rootNode);


        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    private int getRewardsNumber(ConfigurationNode node){
        int number = node.getNode("config", "random-rewards-number").getInt();
        isNoRandom = number == 0? true:false;
        rewardsMin = node.getNode("config", "rewards-min").getInt();
        rewardsMax = node.getNode("config", "rewards-max").getInt() + 1;
        return number;
    }

    public int generateRandomRewardNumber(){
        int nextInt;
        if(randomRewardsNumber < 0 ) {
            //Inclusive
            if(rewardsMin < 0) rewardsMin = 0;
            if (rewardsMax > rewardsMin){
                nextInt =  ThreadLocalRandom.current().nextInt(rewardsMin,rewardsMax);
            } else {
                nextInt = 0;
                U.warn("There seems to be an error in your min/max setting in your configs.");
            }

            U.info("Giving out " + nextInt + " random rewards.");
            return nextInt;
        } else if(randomRewardsNumber < 0){
            return 0;
        }
        return 0;
    }

    public void updateLoot(ConfigurationNode node){
        List<String> nodeStrings = node.getNode("config","vote-reward","random").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
        if(nodeStrings.size()%2!= 0){
            U.error("Please check the Config for your main random rewards, to make sure they are formatted correctly");
        } else {
            hasLoot = true;
            String[] inputLootSource = nodeStrings.stream().toArray(String[]::new);
            //Create a new Array of the proper size x*2 to hold the tables for choosing later
            String[][] table = new String[2][inputLootSource.length/2];
            chanceMap = new int[inputLootSource.length/2];
            U.info(inputLootSource.length/2 + " Tables Imported for Rewards");

            for(int ix = 0; ix < inputLootSource.length; ix+=2){
                table[0][ix/2] = inputLootSource[ix];
                table[1][ix/2] = inputLootSource[ix+1];
                //Initialize chanceMap
                chanceMap[ix/2] = Integer.parseInt(table[0][ix/2]);
                if(ix != 0){
                    chanceMap[ix/2]+= chanceMap[(ix/2)-1];

                }
            }
            mainRewardTables = table;
            chanceTotal = chanceMap.length-1;
            chanceMin = chanceMap[0];
            chanceMax = chanceTotal;


        }

    }





    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////LISTENERS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    @Listener
    public void onVote(VotifierEvent event)
    {
        Vote vote = event.getVote();
        String username = vote.getUsername();
        U.info("Vote Registered From " +vote.getServiceName() + " for "+ username);

        giveVote(username);

        if(isOnline(username)) {
            broadCastMessage(publicMessage, username);
        }



    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event){
        UUID playerID = event.getTargetEntity().getUniqueId();
        String username = event.getTargetEntity().getName();

        if(storedVotes.containsKey(playerID)){
            broadCastMessage(publicMessage, username);

            for(int ix = 0; ix < storedVotes.get(playerID).intValue(); ix ++){
                giveVote(username);
            }

            storedVotes.remove(playerID);
            try {
                saveOffline();
            } catch (IOException e) {
                U.error("Error while saving offline votes file", e);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    public boolean giveReward(){
        //Execute Commands
        //executingQueue = commandQueue;
        commandQueue = new LinkedList<String>();

        for (String command: executingQueue)
        {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }
        executingQueue = null;
        return true;

    }
    public boolean giveReward(List<String> commands){
        //Execute Commands

        for (String command: commands)
        {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }

        return true;

    }

    public String chooseTable(){

        //compare
        int roll = this.roll();
        int currentChoice = -1;
        for(int ix = 0; ix < chanceMap.length; ix++){

            if(roll <= chanceMap[ix]){

                currentChoice = ix;
                break;
            }
        }

        if(currentChoice < 0 ) U.error("There was a problem while rolling something might be broken");
        String chosenReward = mainRewardTables[1][currentChoice];
        return chosenReward;
    }

    public boolean giveVote(String username){
        LootTable mainLoot;
        currentRewards = "";
        ArrayList<String> commandQueue = new ArrayList<String>();
        if(hasLoot && !isNoRandom && randomRewardsNumber >= 1) {
            for (int i = 0; i < randomRewardsNumber; i++) {
                mainLoot = new LootTable(chooseTable(),rootNode);
                U.info("Choosing a random reward.");
                String chosenReward = mainLoot.chooseReward();

                currentRewards = currentRewards + rootNode.getNode("config","Rewards",chosenReward,"name").getString() + ", ";
                for(String ix: rootNode.getNode("config","Rewards",chosenReward,"rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())){
                    commandQueue.add(parseVariables(ix,username));
                }
            }
        } else if(hasLoot && !isNoRandom){
            randomRewardsGen = generateRandomRewardNumber();
            for (int i = 0; i < randomRewardsGen; i++) {
                mainLoot = new LootTable(chooseTable(),rootNode);
                U.info("Choosing a random reward.");

                String chosenReward = mainLoot.chooseReward();
                                currentRewards = currentRewards + rootNode.getNode("config", "Rewards",chosenReward,"name").getString() + ", ";
                for(String ix: rootNode.getNode("config", "Rewards", chosenReward,"rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())){
                commandQueue.add(parseVariables(ix,username));


                }
            }

        }
        //Get Set Rewards
        for(String setCommand: setCommands){
            commandQueue.add(parseVariables(setCommand, username, currentRewards));
        }


        if (isOnline(username)) {
            giveReward(commandQueue);
            if(!(milestones == null)){
                milestones.addVote(game.getServer().getPlayer(username).get().getUniqueId());
            }

        }
        else
        {
            UUID playerID;

            if(userStorage.get().get(username).isPresent()){
                playerID = userStorage.get().get(username).get().getUniqueId();

                //Write to File
                if(storedVotes.containsKey(playerID)) {
                    storedVotes.put(playerID, storedVotes.get(playerID).intValue() + 1);
                } else {
                    storedVotes.put(playerID, new Integer(1));
                }
                try {
                    saveOffline();
                } catch (IOException e) {
                    U.error("Woah did that just happen? I couldn't save that offline player's vote!", e);
                }
            }



        }

        return true;
    }
    //Adds a reward(command) to the queue which is scheduled along with the main thread.
    //Bypass for Async NuVotifier
    public boolean queueReward(){

        return true;
    }
    public boolean broadCastMessage(String message, String username){
        if (message.isEmpty()) return false;
        game.getServer().getBroadcastChannel().send(
                TextSerializers.FORMATTING_CODE.deserialize(parseVariables(message, username, currentRewards)));
        return true;
    }

    public Text convertLink(String link){
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize(link);
        try {
            return textLink.toBuilder().onClick(TextActions.openUrl(new URL(textLink.toPlain()))).build();
        } catch (MalformedURLException e) {
            U.error("Malformed URL");
            U.error(e.toString());
        }
        return Text.of("Malformed URL - Inform Administrator");
    }
    public String parseVariables(String string, String username){
        return string.replace("{player}",username);
    }
    public String parseVariables(String string, String username, String currentRewards){
        if (isNoRandom){
            return parseVariables(string,username);
        } else if(currentRewards == "") {
            return string.replace("{player}",username).replace("{rewards}", "No Random Rewards");
        }
        return string.replace("{player}",username).replace("{rewards}", currentRewards.substring(0,currentRewards.length() -2));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////Utilities/////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    //returns weather a player is online
    private boolean isOnline(String username){
        if(getGame().getServer().getPlayer(username).isPresent()) return true;
        return false;
    }

    private void saveOffline() throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(offlineVotes.toFile());
        ObjectOutputStream objectOutputStream= new ObjectOutputStream(fileOutputStream);

        objectOutputStream.writeObject(storedVotes);
        objectOutputStream.close();

    }
    private void loadOffline() throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream  = new FileInputStream(offlineVotes.toFile());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

        storedVotes = (HashMap<UUID, Integer>) objectInputStream.readObject();
        objectInputStream.close();
    }

    public int roll(){
        //Returns a number within the chance pool bound is lower inclusive upper exclusive
        int nextInt;
        if(chanceMax>0) {
            nextInt = ThreadLocalRandom.current().nextInt(0, chanceMax + 1);
            return nextInt;
        }
        return  0;
    }

    public static SeriousVote getInstance(){
        return instance;
    }

    public static Optional<UserStorageService> getUserStorage(){
        return userStorage;
    }





}
