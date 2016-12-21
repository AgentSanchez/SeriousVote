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
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;

import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.text.Text;

import java.io.IOException;
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


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.lang.Math.incrementExact;
import static java.lang.Math.random;

/**
 * Created by adam_ on 12/08/16.
 */
@SuppressWarnings("unused")
@Plugin(id = "seriousvote", name = "Serious Vote", version = "1", description = "This plugin enables server admins to give players rewards for voting for their server.", dependencies = @Dependency(id = "nuvotifier", version = "1.0", optional = false) )
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

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path privateConfigDir;
    private CommentedConfigurationNode rootNode;


    ///////////////////////////////////////////////////////
    LinkedHashMap<Integer, List<Map<String, String>>> lootMap = new LinkedHashMap<Integer, List<Map<String,String>>>();
    List<Integer> chanceMap;
    String currentRewards;



    @Listener
    public void onInitilization(GamePreInitializationEvent event){
        getLogger().info("Serious Vote loading...");
        getLogger().info("Trying To setup Config Loader");

        Asset asset = plugin.getAsset("seriousvote.conf").orElse(null);

        if (Files.notExists(defaultConfig)) {
            if (asset != null) {
                try {
                    getLogger().info("Copying Default Config");
                    getLogger().info(asset.readString());
                    getLogger().info(defaultConfig.toString());
                    asset.copyToFile(defaultConfig);
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
                .permission("seriousvote.commands.adamin.reload")
                .executor(new SVoteReload())
                .build();
        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("seriousvote.commands.vote")
                .executor(new SVoteVote())
                .build();
        CommandSpec debug = CommandSpec.builder()
                .description(Text.of("DEBUG ONLY"))
                .permission("seriousvote.commands.admin.debug.debug")
                .executor(new SDebug())
                .build();
        //TODO:Add Give player Vote Command (For debug)
        //////////////////////////COMMAND REGISTER////////////////////////////////////////////
        Sponge.getCommandManager().register(this, vote, "vote");
        Sponge.getCommandManager().register(this, reload,"seriousvotereload");
        Sponge.getCommandManager().register(this, debug, "debug", "seriousdebug" );
    }

    //////////////////////////////COMMAND EXECUTOR CLASSES/////////////////////////////////////
        public class SVoteReload implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            reloadConfigs();
            return CommandResult.success();
        }
    }
    //TODO Fix this Method
    public class SDebug implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {

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

    private Text getPublicMessage(ConfigurationNode node, String username){
        return TextSerializers.FORMATTING_CODE.deserialize(parseVariables(node.getNode("config", "broadcast-message").getString(), username, currentRewards));

    }
    private int getRewardsNumber(ConfigurationNode node){
        int number = node.getNode("config", "random-rewards-number").getInt();
        if(number < 0 ){
            return ThreadLocalRandom.current().nextInt(node.getNode("config", "rewards-min").getInt(),node.getNode("config", "rewards-max").getInt());
        }
        return number;
    }

    public void reloadConfigs(){
        try {
            rootNode = loader.load();
        } catch (IOException e) {
            getLogger().error("There was an error while reloading your configs");
            getLogger().error(e.toString());
        }

        updateLoot(getRandomCommands(rootNode));

        for(String ix : getRandomCommands(rootNode)){
            getLogger().info(ix);
        }

    }

    public void updateLoot(List<String> lootTable){
        for(String ix : lootTable){
            getLogger().info(ix);
        }
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
            return;
        }
        buildChanceMap();
        getLogger().info("Rewards for seriousVote Have been loaded successfully");


    }

    void buildChanceMap() {

        if (lootMap.size() == 0) {
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

        getLogger().info("Vote Registered From " +vote.getServiceName() + " for "+ vote.getUsername());
        //Reset Name List
        currentRewards = "";
        getLogger().info(chanceMap.size() + "");
        //Get Random Rewards
        List<String> rewardsList = new LinkedList<String>();
        for(int i = 0; i < getRewardsNumber(rootNode); i++)
        {
            rewardsList.add(chooseReward(vote.getUsername()));
        }
        //Get Set Rewards
        for(String setCommand: getSetCommands(rootNode)){
            rewardsList.add(parseVariables(setCommand, vote.getUsername()));
        }



        broadCastMessage(getPublicMessage(rootNode,vote.getUsername()));
        rewardVote(vote.getUsername(), rewardsList);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean rewardVote(String username, List<String> rewardList){
        //Execute Commands
        for (String command : rewardList)
        {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }

        return true;
    }

    public boolean broadCastMessage(Text message){
        if (message.toPlain().isEmpty()) return false;
        game.getServer().getBroadcastChannel().send(message);
        return true;
    }



    public String chooseReward(String username)
    {
        Integer reward = chanceMap.get(ThreadLocalRandom.current().nextInt(0, chanceMap.size()));
        getLogger().info("Chose Reward from Table" + reward.toString());
        List<Map<String,String>> commandList = lootMap.get(reward);
        Map<String, String> commandMap = commandList.get(ThreadLocalRandom.current().nextInt(0, commandList.size()));
        Map.Entry runCommand = Iterables.get(commandMap.entrySet(),0);
        //Get "Name of reward"
        currentRewards += runCommand.getKey().toString() + " & ";
        return parseVariables(runCommand.getKey().toString(), username);

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
        return string.replace("{player}",username).replace("{rewards}", currentRewards);
    }



}
