package net.adamsanchez.seriousvote.integration;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Token;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.api.SeriousVoteAPI;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.Sponge;
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
        U.debug("Attempting to retrieve player " + playerName + "'s record...");
        if(!SeriousVote.getInstance().usingMilestones()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }
        if (playerName != null){
            return String.valueOf(SeriousVoteAPI.getPlayerTotalVotes(playerName));
        }
        return String.valueOf(0);
    }

    @Placeholder(id = "sv-top-name")
    public String rankPlayerName(@Token @Nullable Integer rank){
        U.debug("Attempting to retrieve #" + rank + " player's record... ");
        if(!SeriousVote.getInstance().usingMilestones()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }

        if(rank == null || rank > SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters()){
            U.debug("Requested number out of range!!!");
            //return U.getName(SeriousVoteAPI.getRecordByRank(0).getUuid());
            return "_EMPTY_";
        }
        else
        {
            String playerName = U.getName(SeriousVoteAPI.getRecordByRank(rank-1).getUuid());
            U.debug("Returning Player Name - " + playerName);
            return U.getName(SeriousVoteAPI.getRecordByRank(rank-1).getUuid());
        }
    }

    @Placeholder(id = "sv-top-votes")
    public String rankPlayerVotes(@Token @Nullable Integer rank){
        U.debug("Attempting to retrieve #" + rank + " player's record... ");
        if(!SeriousVote.getInstance().usingMilestones()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }

        if(rank == null || rank > SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters()){
            //return SeriousVoteAPI.getRecordByRank(0).getTotalVotes() + "";
            U.debug("Requested number out of range!!!");
            return "_EMPTY_";
        }
        else
        {
            Integer totalVotes = SeriousVoteAPI.getRecordByRank(rank-1).getTotalVotes();
            U.debug("Returning totalVotes ---: " + totalVotes);
            return totalVotes + "";
        }
    }

    @Placeholder(id = "sv-total-voters")
    public int numberOfVoters(){
        if(!SeriousVote.getInstance().usingMilestones()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return 0;
        }

        return SeriousVoteAPI.getTotalNumberOfVoters();
    }

    @Placeholder(id = "sv-version")
    public String rankPlayerVotes(){
        return SeriousVote.getInstance().getPlugin().getVersion().get();
    }

}

