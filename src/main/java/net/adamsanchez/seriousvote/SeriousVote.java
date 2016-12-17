package net.adamsanchez.seriousvote;

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


import java.util.List;
import java.util.stream.Collectors;

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
    //Load Configurations

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

        try {
            rootNode = loader.load();
            getLogger().info("Yay Serious Vote configs correctly loaded");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

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
            rootNode = reloadConfigs();
            return CommandResult.success();
        }
    }
    //TODO Fix this Method
    public class SDebug implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            //This retrieves the word Reward, is it combining all the sub nodes into 1? Hoe many entries does the sub node have
            //If it returns a node can I extract a map from that node for it's key/values
            //Can I store the name as the key for the Name ... Player won blank

            rootNode.getNode("config","Rewards").getChildrenMap()
                    .forEach( (k,v)-> getLogger().info(k.toString()));
            //This should log more than one, now..If so The names can be used in another array to get all the nodes.
            /*

                for all items in array
                    get from root node > Add to a double value map in form of Name > Percentage, Command, Sort the map by percentage.
                    The array is loaded into memory and can be used in the future without the expense of loading from config again
                           reload command must reload this into the seperate map, the node based structure is not practical for this application

                Run method getRandomReward()
                   method runs through map and gets all the first values of keys
                   runs some sort of random using java.math.random() to gather a number from 0 -1 ie: 0.25 0.87
                   It then picks a value in the map that is closest to that value.
                   It does this operations the number of times defined in the config file
                   ie: RandomRewards();



             */


            rootNode.getNode("config","Rewards").getChildrenList().stream()
                    .map(ConfigurationNode::getString).collect(Collectors.toList());



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

3
    ///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////CONFIGURATION METHODS//////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    private List<String> getCommands(ConfigurationNode node) {
        return node.getNode("config","commands").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    //load in a map
    private List<String> getRandomCommands(ConfigurationNode node) {
           getLogger().info(node.getNode("config","Rewards").getChildrenList().get(1).getKey().toString());

        return null;
    }
    private List<String> getVoteSites(ConfigurationNode node) {
        return node.getNode("config","vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    private Text getPublicMessage(ConfigurationNode node, String username){
        return TextSerializers.FORMATTING_CODE.deserialize(parseVariables(node.getNode("config", "broadcast-message").getString(), username));

    }
    private int getRewardsNumber(ConfigurationNode node){
        int number = node.getNode("config", "random-rewards-number").getInt();
        if(number < 0 ){
            return (int)random();
        }
    }

    public CommentedConfigurationNode reloadConfigs(){
        try {
            return loader.load();
        } catch (IOException e) {
            getLogger().error("There was an error while reloading your configs");
            getLogger().error(e.toString());
        }
        return HoconConfigurationLoader.builder().build().createEmptyNode();
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




    ///////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////LISTENERS///////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////
    @Listener
    public void onVote(VotifierEvent event)
    {
        Vote vote = event.getVote();
        rewardVote(vote.getUsername());
        getLogger().info("Vote Registered From " +vote.getServiceName() + " for "+ vote.getUsername());
        broadCastMessage(getPublicMessage(rootNode,vote.getUsername()));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean rewardVote(String username){
        //Execute Commands
        for (String command : getCommands(rootNode)) {
            game.getCommandManager().process(game.getServer().getConsole(), parseVariables(command,username));
        }

        //Execute Roulette

        //Log Vote Somehow

        return true;
    }

    public boolean broadCastMessage(Text message){
        if (message.toPlain().isEmpty()) return false;
        game.getServer().getBroadcastChannel().send(message);
        return true;
    }



}
