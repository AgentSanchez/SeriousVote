package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;

/**
 * Created by Adam Sanchez on 4/13/2018.
 * This command is used to set the votes of a player to a specific number.
 */
public class SetVotesCommand implements CommandExecutor {
    //TODO Test - TestCase add votes to player, use command for offline multiple times
    //  if they all work we're gravy.

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SeriousVote sv = SeriousVote.getInstance();
        String username = args.<String>getOne("playerID").get();
        boolean offline = args.<Boolean>getOne("offlineVotes").get();
        int newNumVotes = args.<Integer>getOne("numVotes").get();

        PlayerRecord pr = sv.getVoteSpreeSystem().getRecord(U.getPlayerIdentifier(username));
        if(offline){
            if(sv.getOfflineVotes().containsKey(username)){
                sv.getOfflineVotes().put(username, newNumVotes);
                sv.triggerSave();
            }
            return CommandResult.success();
        }

        if(pr!=null){
            pr.setTotalVotes(newNumVotes);
            U.debug("Retrieving from Database for " + pr.getPlayerIdentifier());
            PlayerRecord newR = new PlayerRecord(pr.getPlayerIdentifier(), newNumVotes, pr.getVoteSpree(), pr.getLastVote());
            if(!sv.getVoteSpreeSystem().updateRecord(newR)){
                src.sendMessage(Text.of("COULD NOT UPDATE RECORD").toBuilder().color(TextColors.RED).build());
                return CommandResult.success();
            }
        }
        src.sendMessage(Text.of("RECORD UPDATED").toBuilder().color(TextColors.GREEN).build());
        return CommandResult.success();
    }

}
