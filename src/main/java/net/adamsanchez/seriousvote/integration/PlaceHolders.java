package net.adamsanchez.seriousvote.integration;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Token;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.api.SeriousVoteAPI;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

public class PlaceHolders {

    public static PlaceHolders aThis;
    public static boolean apiLoaded;
    private static PlaceholderService papi;
    public static void initialize(PluginContainer instance){
        if (!Sponge.getServiceManager().isRegistered(PlaceholderService.class)) {
            instance.getLogger().warn("PlaceholderAPI not found, support disabled!");
            return;
        }
        papi = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        aThis = new PlaceHolders();
        papi.loadAll(aThis, instance)
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
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }
        if (playerName != null){
            return String.valueOf(SeriousVoteAPI.getPlayerTotalVotes(playerName));
        }
        return String.valueOf(0);
    }

    @Placeholder(id = "sv-top-name")
    public String rankPlayerName(@Token String rankStr){
        int rank;
        try{
            U.debug("Received a: " + rankStr);
            rank = Integer.parseInt(rankStr);
        } catch (NumberFormatException e){

            if(rankStr.contains("-") && rankStr.length() > 1){
                try{
                    U.debug(CC.YELLOW + "Invalid input...Trying to Parse: " + rankStr.substring(rankStr.indexOf("_") + 1, rankStr.length()));
                    rank=Integer.parseInt(rankStr.substring(rankStr.indexOf("_") + 1, rankStr.length()));
                } catch(NumberFormatException e2){
                    return giveNumberFormatError(e2, rankStr);
                }
                return processTopNames(rank);
            }
            return giveNumberFormatError(e,rankStr);
        }
        return processTopNames(rank);
    }

    public String processTopNames(int rank){
        U.debug("SV-PlceHolder Retrieving #" + rank + " player's record... ");
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }
        if((Integer)rank == null){
            //return U.getName(SeriousVoteAPI.getRecordByRank(0).getUuid());
            U.error("Placeholder did not pass me an integer....");
            return "ERROR";
        }
        else if(rank > SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters()){
            U.debug("Requested number out of range!!!");
            return("UNKNOWN");
        }
        else
        {
            String playerIdentifier = SeriousVoteAPI.getRecordByRank(rank-1).getPlayerIdentifier();
            String playerName = U.getName(playerIdentifier);
            U.debug("Returning Player Name - " + playerName);

            return U.getName(SeriousVoteAPI.getRecordByRank(rank-1).getPlayerIdentifier());
        }
    }

    @Placeholder(id = "sv-top-votes")
    public String rankPlayerVotes(@Token String rankStr){
        int rank;
        try{
            U.debug("Received a: " + rankStr);
            rank = Integer.parseInt(rankStr);
        } catch (NumberFormatException e){

            if(rankStr.contains("-") && rankStr.length() > 1){
                    try{
                        U.debug(CC.YELLOW + "Invalid input...Trying to Parse: " + rankStr.substring(rankStr.indexOf("_") + 1, rankStr.length()));
                        rank=Integer.parseInt(rankStr.substring(rankStr.indexOf("_") + 1, rankStr.length()));
                    } catch(NumberFormatException e2){
                        return giveNumberFormatError(e2, rankStr);
                    }
                    return processTopVotes(rank);
            }
            return giveNumberFormatError(e,rankStr);
        }
        return processTopVotes(rank);
    }

    public String processTopVotes(int rank){
        U.debug("SV-PlceHolder Retrieving #" + rank + " player's record... ");
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return "MILESTONES NOT ENABLED";
        }

        if((Integer)rank == null){
            //return U.getName(SeriousVoteAPI.getRecordByRank(0).getUuid());
            U.error("Placeholder did not pass me an integer....");
            return "ERROR";
        }
        else if(rank > SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters()){
            U.debug("Requested number out of range!!!");
            return("UNKNOWN");
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
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) {
            U.debug("MILESTONES NOT ENABLED - CANNOT RETRIEVE DATA");
            return 0;
        }

        return SeriousVoteAPI.getTotalNumberOfVoters();
    }

    @Placeholder(id = "sv-version")
    public String rankPlayerVotes(){
        return SeriousVote.getInstance().getPlugin().getVersion().get();
    }

    public static PlaceholderService getPapi(){
        return papi;
    }

    public static Text papiParse(String s){
        return getPapi().replacePlaceholders(s, U.getConsole().getCommandSource().get(), U.getConsole().getCommandSource().get());
    }

    public static Text papiParse(String s, CommandSource src, CommandSource obsv){
        return getPapi().replacePlaceholders(s, src,obsv);
    }

    private String giveNumberFormatError(Exception e, String inputReceived){
        U.debug(CC.RED + "You have given an incorrect number format!!! Received: \"" + inputReceived + "\"" );
        U.debug(e.getStackTrace().toString());
        return "NUM_FORMAT_ERROR";
    }
}

