package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.utils.OutputHelper;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class DebugCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        U.debug(CC.YELLOW + "Debug Mode has been toggled and is now: "
                + (SeriousVote.getInstance().toggleDebug()
                ? CC.GREEN + "ON" : CC.RED + "OFF"));

        src.sendMessage(OutputHelper.strToText("&e" + "Debug is now: "
                + (CM.getDebugMode()
                ? "&2" + "ON" : "&4" + "OFF")));
        return CommandResult.success();
    }
}
