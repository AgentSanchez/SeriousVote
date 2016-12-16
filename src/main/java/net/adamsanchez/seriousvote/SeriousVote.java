package net.adamsanchez.seriousvote;

import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import ninja.leaping.configurate.ConfigurationNode;

import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
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
        //TODO:Add Give player Vote Command (For debug)
        //////////////////////////COMMAND REGISTER////////////////////////////////////////////
        Sponge.getCommandManager().register(this, vote, "vote");
        Sponge.getCommandManager().register(this,vote,"seriousvotereload");
    }

    //////////////////////////////COMMAND EXECUTOR CLASSES/////////////////////////////////////
        public class SVoteReload implements CommandExecutor {
        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            reloadConfigs();
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
    private List<String> getCommands(ConfigurationNode node) {
        return node.getNode("config","commands").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    private List<String> getVoteSites(ConfigurationNode node) {
        return node.getNode("config","vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    private Text getPublicMessage(ConfigurationNode node){
        return TextSerializers.FORMATTING_CODE.deserialize(node.getNode("config", "broadcast-message").getString());
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
        Text textLink = TextSerializers.FORMATTING_CODE.deserialize("&4HelloWorld");
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
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////ACTION METHODS///////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean rewardVote(String username){
        //Execute Commands
        for (String command : getCommands(rootNode)) {
            game.getCommandManager().process(game.getServer().getConsole(), command.replace("{player}",username));
        }

        //Execute Roulette

        //Log Vote Somehow

        return true;
    }

    public boolean broadCastMessage(Text message){
        game.getServer().getBroadcastChannel().send(message);
        return true;
    }



}
