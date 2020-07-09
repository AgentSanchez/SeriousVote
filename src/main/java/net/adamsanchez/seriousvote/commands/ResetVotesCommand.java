package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class ResetVotesCommand implements CommandExecutor {

    @NonnullByDefault
    @NonNull
    public CommandResult execute(CommandSource src, CommandContext args) {
        if (SeriousVote.getInstance().usingVoteSpreeSystem()) {
            SeriousVote.getInstance().getVoteSpreeSystem().resetPlayerVotes();
            src.sendMessage(Text.of("All votes reset to 0"));
            SeriousVote.getInstance().getOfflineVotes().clear();
            SeriousVote.getInstance().triggerSave();
        } else {
            src.sendMessage(Text.of("You are not using a database...."));

        }
        return CommandResult.success();

    }
}
