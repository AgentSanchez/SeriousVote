package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
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
public class ResetVotesCommand implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) throws
            CommandException {
        if (SeriousVote.getInstance().reloadConfigs()) {
            src.sendMessage(Text.of("All vote records reset to 0"));
        } else {
            src.sendMessage(Text.of("Could not execute the reset command.").toBuilder().color(TextColors.RED).build());
        }

        return CommandResult.success();

    }
}
