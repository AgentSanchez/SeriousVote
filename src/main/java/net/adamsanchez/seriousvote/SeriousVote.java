package net.adamsanchez.seriousvote;

import com.google.inject.Inject;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;



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


    @Listener
    public void onServerStart(GameInitializationEvent event)
    {
        getLogger().info("Serious Vote loading...");
        seriousVotePlugin = this;
        game = Sponge.getGame();


        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("nope.nope.nope")
                .executor(new SVote())
                .build();

        Sponge.getCommandManager().register(this, vote, "vote");

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
        getLogger().info("getUsername() voted");
        getLogger().info(":D Hello From Mac OSx");
    }

}
