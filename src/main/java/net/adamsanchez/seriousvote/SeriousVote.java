package net.adamsanchez.seriousvote;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import ninja.leaping.configurate.ConfigurationNode;

import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.omg.CORBA.COMM_FAILURE;
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
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;

import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;


import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.Math.incrementExact;
import static java.lang.Math.random;

/**
 * Created by adam_ on 12/08/16.
 */
@SuppressWarnings("unused")
@Plugin(id = "seriousvote", name = "Serious Vote", version = "2.5-BETA", description = "This plugin enables server admins to give players rewards for voting for their server.", dependencies = @Dependency(id = "nuvotifier", version = "1.0", optional = false) )
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

    public static EconomyService economyService;
    private static SeriousVote seriousVotePlugin;


    @Inject Logger logger;
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
    LinkedHashMap<Integer, List<Map<String, String>>> lootMap = new LinkedHashMap<Integer, List<Map<String,String>>>();
    HashMap<UUID,Integer> storedVotes = new HashMap<UUID,Integer>();
    int randomRewardsNumber;
    List<String> setCommands;
    List<Integer> chanceMap;
    String currentRewards;
    String publicMessage;
    boolean hasLoot = false;
    Optional<UserStorageService> userStorage;



    @Listener
    public void onInitialization(GamePreInitializationEvent event){
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
                offlineVoteAsset.copyToFile(offlineVotes);
            } catch (IOException e) {
                getLogger().error("Could Not Initialize the offlinevotes file! What did you do with it");
                getLogger().error(e.toString());
            }
        }



        currentRewards = "";

        reloadConfigs();

    }



    @Listener
    public void onServerStart(GameInitializationEvent event)
    {
        seriousVotePlugin = this;

        registerCommands();


        getLogger().info("Serious Vote Has Loaded\n\n\n\n");

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
            reloadConfigs();
            return CommandResult.success();
        }
    }

    public class SVoteGiveVote implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

            Player player = args.<Player>getOne("player").get();

            player.sendMessage(Text.of("An administrator has awarded you a vote!"));
            src.sendMessage(Text.of("You have successfully given " + player.getName() + " a vote"));

            giveVote(player.getName());

            return CommandResult.success();
        }
    }

    public class SVoteVote implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            src.sendMessage(Text.of("Thank You! Below are the places you can vote!").toBuilder().color(TextColors.GOLD).build());
            getVoteSites(rootNode).forEach(site -> {
                src.sendMessage(convertLink(site));
            });
            return CommandResult.success();



        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////CONFIGURATION METHODS//////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    public void reloadConfigs(){
        //try loading from file
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            getLogger().error("There was an error while reloading your configs");
            getLogger().error(e.toString());
        }

        //update variables and other instantiations
        publicMessage = getPublicMessage(rootNode);
        randomRewardsNumber = getRewardsNumber(rootNode);

        updateLoot(getRandomCommands(rootNode));
        buildChanceMap();
        setCommands = getSetCommands(rootNode);
        getLogger().debug("Here's your commands");
        for(String ix : getRandomCommands(rootNode)){
            getLogger().debug(ix);
        }


        //Load Offline votes
        getLogger().info("Trying to load offline player votes from ... " + offlineVotes.toString());
        try {
            loadOffline();
        } catch (IOException e) {
            getLogger().error("ahahahahaha We Couldn't load up the stored offline player votes",e);
        } catch (ClassNotFoundException e) {
            getLogger().error("Well crap that is noooot a hash map! GO slap the dev!");
        }

    }
    private List<String> getSetCommands(ConfigurationNode node) {
        return node.getNode("config","Rewards","set").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    private List<String> getRandomCommands(ConfigurationNode node) {
           return node.getNode("config","Rewards","random").getChildrenList().stream()
                   .map(ConfigurationNode::getString).collect(Collectors.toList());
    }
    private List<String> getVoteSites(ConfigurationNode node) {
        return node.getNode("config","vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    //Returns the string value from the Config for the public message. This must be deserialized
    private String getPublicMessage(ConfigurationNode node){
        return node.getNode("config","broadcast-message").getString();
    }
    private int getRewardsNumber(ConfigurationNode node){
        int number = node.getNode("config", "random-rewards-number").getInt();
        if(number < 0 && lootMap.size()==0){
            return ThreadLocalRandom.current().nextInt(node.getNode("config", "rewards-min").getInt(),node.getNode("config", "rewards-max").getInt());
        } else if(number < 0) {
            return 0;
        }
        return number;
    }
    public void updateLoot(List<String> lootTable){

        String[] inputLootTable = lootTable.stream().toArray(String[]::new);
        lootMap = new LinkedHashMap<Integer, List<Map<String,String>>>();
        chanceMap = new ArrayList<Integer>();
        //count to get the correct size of the lootMap
        for (int i = 0; i < inputLootTable.length; i+=3)
        {
            //get the current integer add it to the table, Since it is a Map duplicates will be removed
            lootMap.put(Integer.parseInt(inputLootTable[i]), new ArrayList<Map<String,String>>());
        }

        for (int i = 0; i < inputLootTable.length; i+=3)
        {
            //add in all the commands
            List lootList = lootMap.get(Integer.parseInt(inputLootTable[i]));
            Map<String,String>  lootEntry = new LinkedHashMap<String,String>();
            lootEntry.put(inputLootTable[i+1],inputLootTable[i+2]);
            lootList.add(lootEntry);
        }


        if (lootMap.size() == 0) {
            getLogger().error("The lootMap Hasn't been loaded Check your config for errors!");
            hasLoot = false;
            return;        }
        hasLoot = true;
        getLogger().info("Rewards for seriousVote Have been loaded successfully");


    }
    void buildChanceMap() {

        if (!hasLoot) {
            getLogger().error("The lootMap Hasn't been loaded Check your config for errors!");
            return;
        } else {
            getLogger().info("There are currently " + lootMap.size() + " Loot Tables");
            for (int i = 0; i < lootMap.size(); i++) {
                Map.Entry currentSet = Iterables.get(lootMap.entrySet(), i);
                Integer currentKey = Integer.parseInt(currentSet.getKey().toString());
                getLogger().info("Gathering Table " + i + " of type" + currentKey);

                for (int ix = 0; ix < currentKey.intValue(); ix++) {
                    chanceMap.add(currentKey.intValue());
                }

            }
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
        getLogger().info("Vote Registered From " +vote.getServiceName() + " for "+ username);
        giveVote(username);
        if(isOnline(username)){
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
                getLogger().error("Error while saving offline votes file", e);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    public boolean giveReward(String username, List<String> rewardList){
        //Execute Commands

        for (String command : rewardList)
        {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }

        return true;
    }

    public boolean giveVote(String username){
        currentRewards = "";
        List<String> rewardsList = new LinkedList<String>();

        if(hasLoot) {
            for (int i = 0; i < randomRewardsNumber; i++) {
                getLogger().info("Choosing a random reward.");
                rewardsList.add(chooseReward(username));
            }
        }
        //Get Set Rewards
        for(String setCommand: setCommands){
            rewardsList.add(parseVariables(setCommand, username));
        }


        if (isOnline(username)) {
            giveReward(username, rewardsList);
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
                    getLogger().error("Woah did that just happen? I couldn't save that offline player's vote!", e);
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
    public void gatherRandomRewards(){

    }
    //Chooses 1 random reward
    public String chooseReward(String username) {

        Integer reward = chanceMap.get(ThreadLocalRandom.current().nextInt(0, chanceMap.size()));
        getLogger().info("Chose Reward from Table" + reward.toString());
        List<Map<String,String>> commandList = lootMap.get(reward);
        Map<String, String> commandMap = commandList.get(ThreadLocalRandom.current().nextInt(0, commandList.size()));
        Map.Entry runCommand = Iterables.get(commandMap.entrySet(),0);
        //Get "Name of reward"
        currentRewards += runCommand.getKey().toString() + " & ";
        return parseVariables(runCommand.getValue().toString(), username);

    }
    public Text convertLink(String link){
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize(link);
        try {
            return textLink.toBuilder().onClick(TextActions.openUrl(new URL(textLink.toPlain()))).build();
        } catch (MalformedURLException e) {
            getLogger().error("Malformed URL");
            getLogger().error(e.toString());
        }
        return Text.of("Malformed URL - Inform Administrator");
    }
    private String parseVariables(String string, String username){
        return string.replace("{player}",username);
    }
    private String parseVariables(String string, String username, String currentRewards){
        if (currentRewards==null || currentRewards == ""){
            return parseVariables(string, username);
        }
        getLogger().info("Player " + username + " voted and recieved " + currentRewards);
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


}
