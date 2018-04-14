package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

/**
 * Created by Adam Sanchez on 4/13/2018.
 */
public class CommandHandler {

    public static void registerCommands() {

        //////////////////////COMMAND BUILDERS///////////////////////////////////////////////
        CommandSpec reload = CommandSpec.builder()
                .description(Text.of("Reload your configs for seriousvote"))
                .permission("seriousvote.commands.admin.reload")
                .executor(new ReloadCommand())
                .build();
        CommandSpec vote = CommandSpec.builder()
                .description(Text.of("Checks to see if it's running"))
                .permission("seriousvote.commands.vote")
                .executor(new VoteCommand())
                .build();

        CommandSpec giveVote = CommandSpec.builder()
                .description(Text.of("For admins to give a player a vote"))
                .permission("seriousvote.commands.admin.give")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new GiveVoteCommand())
                .build();
        CommandSpec checkVote = CommandSpec.builder()
                .description(Text.of("Check another player's vote record"))
                .permission("seriousvote.commands.admin.check")
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("player"))))
                .executor(new CheckVoteCommand())
                .build();

        //////////////////////////COMMAND REGISTER////////////////////////////////////////////
        Sponge.getCommandManager().register(SeriousVote.getInstance(), vote, "vote");
        Sponge.getCommandManager().register(SeriousVote.getInstance(), reload, "svreload", "seriousvotereload");
        Sponge.getCommandManager().register(SeriousVote.getInstance(), giveVote, "givevote");
        Sponge.getCommandManager().register(SeriousVote.getInstance(), checkVote, "checkvote");
    }
}
