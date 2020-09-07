package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class ChangePlayerIDCommand implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) throws
            CommandException {
        String oldID = args.<String>getOne("OldPlayerID").get();
        String newID = args.<String>getOne("NewPlayerID").get();

        if(SeriousVote.getInstance().usingVoteSpreeSystem()){
            SeriousVote sv = SeriousVote.getInstance();
            boolean debugPreCommand = sv.isDebug();
            CM.setDebugState(true);
            U.debug(CC.YELLOW + "--------------------CHANGING USER ID--------------------");
            U.debug("Attempting to change from: " + oldID + " to " + newID + ".");
            sv.getVoteSpreeSystem().changePlayerID(oldID,newID);
            U.debug(CC.YELLOW + "--------------------Ending Migration--------------------");
            CM.setDebugState(debugPreCommand);
            src.sendMessage(Text.of("CHANGE COMPLETE!!!!").toBuilder().color(TextColors.GREEN).build());

        } else {
            src.sendMessage(Text.of("Not using Vote Spree System...").toBuilder().color(TextColors.RED).build());
        }
        return CommandResult.success();

    }
}
