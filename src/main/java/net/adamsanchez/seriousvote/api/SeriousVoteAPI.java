package net.adamsanchez.seriousvote.api;

import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;

public class SeriousVoteAPI {

    //Return the player's total vote amout
    public static int getPlayerTotalVotes(String playerName){
        String playerIdentifier = U.getPlayerIdentifier(playerName);
        return SeriousVote.getInstance().getVoteSpreeSystem().getRecord(playerIdentifier).getTotalVotes();
    }

    public static PlayerRecord getRecordByRank(int rank){
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) return null;
        return SeriousVote.getInstance().getVoteSpreeSystem().getRecordByRank(rank);
    }

    public static int getTotalNumberOfVoters(){
        if(!SeriousVote.getInstance().usingVoteSpreeSystem()) return 0;
        return SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters();
    }

}
