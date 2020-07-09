package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.IOException;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class GiveVoteCommand implements CommandExecutor {

    @Override
    @NonNull
    @NonnullByDefault
    public CommandResult execute(CommandSource src, CommandContext args) {
        SeriousVote sv = SeriousVote.getInstance();
        String username = args.<String>getOne("player").get();
        if (U.isPlayerOnline(username)) {
            Player player = SeriousVote.getPublicGame().getServer().getPlayer(username).get();
            player.sendMessage(Text.of("An administrator has awarded you a vote!"));
            sv.giveVote(username);
            src.sendMessage(Text.of("You have successfully given " + username + " a vote"));
        } else {
            String playerIdentifier = U.getPlayerIdentifier(username);
            if (playerIdentifier != null) {
                //Write to File
                if (sv.getOfflineVotes().containsKey(playerIdentifier)) {
                    sv.getOfflineVotes().put(playerIdentifier, sv.getOfflineVotes().get(playerIdentifier) + 1);
                } else {
                    sv.getOfflineVotes().put(playerIdentifier, 1);
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
