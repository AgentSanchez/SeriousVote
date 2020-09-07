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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class MigrateFromOnlineCommand implements CommandExecutor {

    public CommandResult execute(CommandSource src, CommandContext args) throws
            CommandException {
        if(SeriousVote.getInstance().usingVoteSpreeSystem()){
            SeriousVote sv = SeriousVote.getInstance();
            boolean debugPreCommand = sv.isDebug();
            CM.setDebugState(true);
            U.debug(CC.RED + "--------------------Begining Migration--------------------");
            sv.getVoteSpreeSystem().updateAllPlayerID();
            U.debug(CC.RED + "--------------------Ending Migration--------------------");
            CM.setDebugState(debugPreCommand);
            src.sendMessage(Text.of("MIGRATION COMPLETE!!!!").toBuilder().color(TextColors.GREEN).build());

        } else {
            src.sendMessage(Text.of("Not using Vote Spree System...").toBuilder().color(TextColors.RED).build());
        }
        return CommandResult.success();

    }
}
