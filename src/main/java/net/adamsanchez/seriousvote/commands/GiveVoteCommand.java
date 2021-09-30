package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.Data.OfflineRecord;
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

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class GiveVoteCommand implements CommandExecutor {


    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        SeriousVote sv = SeriousVote.getInstance();
        String username = args.<String>getOne("player").get();
        if (U.isPlayerOnline(username)) {
            Player player = sv.getPublicGame().getServer().getPlayer(username).get();
            player.sendMessage(Text.of("An administrator has awarded you a vote!"));
            sv.forceGiveVote(username);
            src.sendMessage(Text.of("You have successfully given " + username + " a vote"));
        } else {
            String playerIdentifier = U.getPlayerIdentifier(username);
            if (playerIdentifier != null) {
                //Write to File
                if (sv.getOfflineVotes().containsKey(playerIdentifier)) {
                    sv.getOfflineVotes().get(playerIdentifier).addOfflineVotes(1);
                } else {
                    OfflineRecord record = new OfflineRecord(username);
                    record.addOfflineVotes(1);
                    sv.getOfflineVotes().put(username, record);
                }
                try {
                    OfflineHandler.saveOffline();
                    src.sendMessage(Text.of("You have successfully given " + username + " an offline vote"));
                } catch (IOException e) {
                    U.error("Woah did that just happen? I couldn't save that offline player's vote!", e);
                }
            }
        }
        return CommandResult.success();
    }

}
