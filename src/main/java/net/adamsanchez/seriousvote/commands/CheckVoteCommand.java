package net.adamsanchez.seriousvote.commands;

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
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class CheckVoteCommand implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SeriousVote sv = SeriousVote.getInstance();
        String playerIdentifier = U.getPlayerIdentifier(args.getOne("player").isPresent() ?
                (String) args.getOne("player").get() :
                src.getIdentifier());
        if (sv.usingVoteSpreeSystem() && (sv.isDailiesEnabled() || sv.isMilestonesEnabled())) {
            PlayerRecord record = sv.getVoteSpreeSystem().getRecord(playerIdentifier);
            if (record != null) {
                src.sendMessage(Text.of(U.getName(playerIdentifier) +
                        " vote record: ").toBuilder().color(TextColors.AQUA).build());
                src.sendMessage(Text.of("Total Votes: "
                        + record.getTotalVotes()).toBuilder().color(TextColors.GOLD).build());

                if (sv.isMilestonesEnabled()) {
                    src.sendMessage(Text.of("Next Milestone: " +
                            SeriousVote.getInstance().getVoteSpreeSystem().getRemainingMilestoneVotes(record.getTotalVotes())));
                }
                if (sv.isDailiesEnabled()) {
                    src.sendMessage(Text.of("Current Streak: " +
                            record.getVoteSpree() +
                            " Days").toBuilder().color(TextColors.GOLD).build());
                    src.sendMessage(Text.of("Next Daily: " +
                            VoteSpreeSystem.getRemainingDays(record.getVoteSpree()) +
                            " Days.").toBuilder().color(TextColors.GOLD).build());
                }
            }

        } else {
            src.sendMessage(Text.of("It seems that currently all the database modules are currently disabled."));
        }

        return CommandResult.success();
    }
}
