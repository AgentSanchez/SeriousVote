package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.CM;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.plugin.PluginContainer;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class VersionCommand implements CommandExecutor {

    //TODO Add metrics collection on/off to this information in red
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        CC.printSVInfo();
        return CommandResult.success();
    }
}
