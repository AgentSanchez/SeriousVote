package net.adamsanchez.seriousvote;

import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;


import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;

import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adam_ on 12/08/16.
 */
@Plugin(id = "seriousvote", name = "Serious Vote", version = "1", description = "This plugin enables server admins to give players rewards for voting for their server.", dependencies = @Dependency(id = "nuvotifier", version = "1.0", optional = false) )
public class SeriousVote
{
    protected SeriousVote()
    {
        ;
    }

    public static Game game;
    public static EconomyService economyService;

    private static SeriousVote seriousVotePlugin;

    @Inject
    private Logger logger;

    public Logger getLogger()
    {
        return logger;
    }


    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path privateConfigDir;


    @Listener
    public void onServerStart(GameInitializationEvent event)
    {
        getLogger().info("Serious Vote loading...");
        seriousVotePlugin = this;
        game = Sponge.getGame();

        //Load Configurations
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        ConfigurationNode rootNode = loader.createEmptyNode(ConfigurationOptions.defaults());

        try {
            rootNode = loader.load();
        } catch(IOException e) {
            getLogger().warn("THERE WAS AN ERROR WITH THE SERIOUS VOTE CONFIGURATION FILE");
        }

        ConfigurationNode defaultNode;
        defaultNode = loader.createEmptyNode();
        if (rootNode.getChildrenList().size() == 0){
            getLogger().info("No configuration found... Attempting to load Default Configurations...");
            URL jarConfigFile = this.getClass().getResource("defaultConfig.conf");
            ConfigurationLoader<CommentedConfigurationNode> defaultLoader =
                    HoconConfigurationLoader.builder().setURL(jarConfigFile).build();


            try {
                defaultNode = defaultLoader.load();

                if(defaultNode.getChildrenList().size() == 0) {
                    try {
                        loader.save(defaultNode);
                        getLogger().info("Default Configurations Loaded");
                        rootNode = defaultNode;
                    } catch (IOException e) {
                        getLogger().warn("THERE WAS AN ERROR TRYING TO SAVE THE DEFAULT CONFIGURATIONS");
                    }
                }
                else
                {
                    getLogger().warn("No configurations could be loaded for Serious Vote, we will now shut down.");
                }

            } catch(IOException e) {
                getLogger().warn("THERE WAS AN ERROR WITH THE SERIOUS VOTE DEFAULT CONFIGURATION FILE");
            }


        }




        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("nope.nope.nope")
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
            src.sendMessage(Text.of("Hello World"));
            return CommandResult.success();

        }
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
        getLogger().info("SERIOUSVOTE Vote Registered - " + vote.getUsername() +" voted");
    }

    public boolean rewardVote(String username){
        List<String> commandList = new ArrayList<String>();


        for (String command : commandList) {
            game.getCommandManager().process(game.getServer().getConsole(), command);
        }



        return true;
    }

}
