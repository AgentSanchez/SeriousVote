package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.UUID;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class VoteCommand implements CommandExecutor {
    SeriousVote sv = SeriousVote.getInstance();

    public CommandResult execute(CommandSource src, CommandContext args) throws
            CommandException {
        src.sendMessage(
                TextSerializers.FORMATTING_CODE.deserialize(CM.getVoteSiteMessage(sv.getMainCfgNode()))
        );
        CM.getVoteSites(sv.getMainCfgNode()).forEach(site -> {
            src.sendMessage(U.convertStringToLink(site));
        });

        if (sv.usingMilestones() && (sv.isDailiesEnabled() || sv.isMilestonesEnabled())) {
            if (sv.getUserStorage().get().get(src.getName()).isPresent()) {
                UUID playerID = sv.getUserStorage().get().get(src.getName()).get().getUniqueId();
                PlayerRecord record = sv.getVoteSpreeSystem().getRecord(playerID);
                if (record != null) {
                    src.sendMessage(Text.of("You have a total of " + record.getTotalVotes()
                            + " votes. You have currently voted " + record.getVoteSpree()
                            + " days in a row.").toBuilder().color(TextColors.GOLD).build());
                    if (sv.isDailiesEnabled()) {
                        int vsa = record.getVoteSpree() + 1;
                        int a = 365 * (vsa / 365 + 1) - vsa;
                        int b = 30 * (vsa / 30 + 1) - vsa;
                        int c = 7 * (vsa / 7 + 1) - vsa;
                        int leastDays = 0;
                        if (a < b && a < c) {
                            leastDays = a;
                        } else if (b < c && b < a) {
                            leastDays = b;
                        } else if (c < b && c < a) {
                            leastDays = c;
                        }
                        leastDays += 1;
                        src.sendMessage(Text.of("You have to vote " + leastDays + " more days until your next dailies reward."));
                    }
                }
            }
        }
        return CommandResult.success();
    }
}
