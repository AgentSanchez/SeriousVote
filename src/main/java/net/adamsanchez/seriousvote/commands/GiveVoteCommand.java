package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class GiveVoteCommand implements CommandExecutor {


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SeriousVote sv = SeriousVote.getInstance();
        String username = args.<String>getOne("player").get();
        if (U.isOnline(username)) {
            Player player = sv.getPublicGame().getServer().getPlayer(username).get();
            player.sendMessage(Text.of("An administrator has awarded you a vote!"));
            sv.giveVote(username);
            src.sendMessage(Text.of("You have successfully given " + username + " a vote"));
        } else {
            String playerIdentifier = U.getPlayerIdentifier(username);
            if (playerIdentifier != null) {
                //Write to File
                if (sv.getStoredVotes().containsKey(playerIdentifier)) {
                    sv.getStoredVotes().put(playerIdentifier, sv.getStoredVotes().get(playerIdentifier).intValue() + 1);
                } else {
                    sv.getStoredVotes().put(playerIdentifier, new Integer(1));
                }
                try {
                    OfflineHandler.saveOffline();
                    src.sendMessage(Text.of("You have successfully given " + username + " an offline vote"));
                } catch (IOException e) {
                    U.error("Woah did that just happen? I couldn't save that offline player's vote!", e);
                }
            }
        }

        sv.resetCurrentRewards();

        return CommandResult.success();
    }

}
