package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.Data.VoteSpreeSystem;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class DumpSQLCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

        if(SeriousVote.getInstance().usingVoteSpreeSystem()){
            OfflineHandler.dumpSQLData(SeriousVote.getInstance().getVoteSpreeSystem().getAllRecords());
        }
        else{
            src.sendMessage(Text.of(CC.RED + "Not using vote spree system!"));
        }

        for(int i = 0; i < 10; i++){
            PlayerRecord r = SeriousVote.getInstance().getVoteSpreeSystem().getRecordByRank(i);
            src.sendMessage(Text.of("TOP #" + (i+1) + " " + r.getPlayerIdentifier() + " - " + r.getTotalVotes()));
    }
        return CommandResult.success();
    }
}
