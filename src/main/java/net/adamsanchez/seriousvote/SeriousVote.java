package net.adamsanchez.seriousvote;

import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import ninja.leaping.configurate.ConfigurationNode;

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
import org.spongepowered.api.text.action.TextActions;


import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by adam_ on 12/08/16.
 */
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

        getLogger().info(rootNode.getNode("config").getNode("server-ip").getString());
        getLogger().info(rootNode.getNode("config").getNode("commands").getChildrenList().get(1).toString());
    }



    @Listener
    public void onServerStart(GameInitializationEvent event)
    {
        getLogger().info("Serious Vote loading...");
        seriousVotePlugin = this;
        game = this.game;






        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("seriousvote.commands.vote")
                .executor(new SVote())
                .build();

        Sponge.getCommandManager().register(this, vote, "vote");
        //TODO:Add Reload Command
        //TODO:Add Give player Vote Command (For debug)
        getLogger().info("Serious Vote Has Loaded\n\n\n\n");

    }

    public class SVote implements CommandExecutor {

        public CommandResult execute(CommandSource src, CommandContext args) throws
                CommandException {
            getVoteSites(rootNode).forEach(site -> {
                try {
                    src.sendMessage(Text.of(site).toBuilder().onClick(TextActions.openUrl(new URL(site))).build());
                } catch (MalformedURLException e) {
                    getLogger().error(e.toString());
                }
            });
            return CommandResult.success();



        }
    }



    public List<String> getCommands(ConfigurationNode node) {
        return node.getNode("config","commands").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }

    public List<String> getVoteSites(ConfigurationNode node) {
        return node.getNode("config","vote-sites").getChildrenList().stream()
                .map(ConfigurationNode::getString).collect(Collectors.toList());
    }


    @Listener
    public void onPostInit(GamePostInitializationEvent event)
    {

    }

    @Listener
    public void onVote(VotifierEvent event)
    {
        Vote vote = event.getVote();
        rewardVote(vote.getUsername());
        getLogger().info("Vote Registered From " +vote.getServiceName() + " for "+ vote.getUsername());
    }

    public boolean rewardVote(String username){
        //Execute Commands
        for (String command : getCommands(rootNode)) {
            game.getCommandManager().process(game.getServer().getConsole(), command.replace("$player$",username));
        }

        //Execute Roulette

        //Log Vote Somehow

        return true;
    }

}
