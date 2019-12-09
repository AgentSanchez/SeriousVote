package net.adamsanchez.seriousvote.integration;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.api.SeriousVoteAPI;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;

import javax.annotation.Nullable;

public class PlaceHolders {

    public static PlaceHolders aThis;
    public static boolean apiLoaded;

    public static void initialize(PluginContainer instance){
        if (!Sponge.getServiceManager().isRegistered(PlaceholderService.class)) {
            instance.getLogger().warn("PlaceholderAPI not found, support disabled!");
            return;
        }
        PlaceholderService apiService = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        aThis = new PlaceHolders();
        apiService.loadAll(aThis, instance)
                .stream()
                .map(builder -> builder.author("seriousvote")
                        .plugin(instance)
                        .version(SeriousVote.getInstance().getPlugin().getVersion().get())
                )
                .forEach(builder -> {
                    try {
                        builder.buildAndRegister();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        apiLoaded = true;
    }

    @Placeholder(id = "sv-player-votes")
    public String playerTotalVotes(@Token String playerName) {
        if(!SeriousVote.getInstance().usingMilestones()) return "MILESTONES NOT ENABLED";
        if (playerName != null){
            return String.valueOf(SeriousVoteAPI.getPlayerTotalVotes(playerName));
        }
        return String.valueOf(0);
    }

    @Placeholder(id = "sv-top-name")
    public String rankPlayerName(@Token @Nullable Integer rank){
        if(!SeriousVote.getInstance().usingMilestones()) return "MILESTONES NOT ENABLED";

        if(rank == null){
            return U.getName(SeriousVoteAPI.getRecordByRank(1).getUuid());
        }
        else
        {
            return U.getName(SeriousVoteAPI.getRecordByRank(rank).getUuid());
        }
    }

    @Placeholder(id = "sv-top-votes")
    public String rankPlayerVotes(@Token @Nullable Integer rank){
        if(!SeriousVote.getInstance().usingMilestones()) return "MILESTONES NOT ENABLED";

        if(rank == null){
            return SeriousVoteAPI.getRecordByRank(1).getTotalVotes() + "";
        }
        else
        {
            return SeriousVoteAPI.getRecordByRank(rank).getUuid() + "";
        }
    }

    @Placeholder(id = "sv-version")
    public String rankPlayerVotes(){
        return SeriousVote.getInstance().getPlugin().getVersion().get();
    }

}

