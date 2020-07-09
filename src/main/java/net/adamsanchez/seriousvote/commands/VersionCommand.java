package net.adamsanchez.seriousvote.commands;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Created by Adam Sanchez on 4/15/2018.
 */
public class VersionCommand implements CommandExecutor {

    @Override
    @NonNull
    @NonnullByDefault
    public CommandResult execute(CommandSource src, CommandContext args) {
        SeriousVote sv = SeriousVote.getInstance();
        sv.getLogger().info(CC.RED + "ONLINE MODE? " + Sponge.getGame().getServer().getOnlineMode() + " "
                + CC.YELLOW_BOLD + "Serious Vote Version: "
                + CC.PURPLE_BOLD + SeriousVote.getInstance().getPlugin().getVersion().get()
                + CC.YELLOW_BOLD + " MC-Version: "
                + CC.PURPLE_BOLD + Sponge.getPlatform().getMinecraftVersion().getName()
                + CC.YELLOW_BOLD + " Sponge-Version: "
                + CC.PURPLE_BOLD + Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getName() + "-"
                + Sponge.getPlatform().getContainer(Platform.Component.IMPLEMENTATION).getVersion().orElse("unknown")
                + "\n" + CC.LINE + "\n"
                + CC.YELLOW_BOLD + " Votifier-Version: " + CC.CYAN + Sponge.getPluginManager().getPlugin("nuvotifier").get().getName()
                + " " + CC.PURPLE + Sponge.getPluginManager().getPlugin("nuvotifier").flatMap(PluginContainer::getSource).get().toString());
        return CommandResult.success();
    }
}
