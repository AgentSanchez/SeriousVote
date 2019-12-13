package net.adamsanchez.seriousvote.api;

import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;

import java.util.UUID;

public class SeriousVoteAPI {

    //Return the player's total vote amout
    public static int getPlayerTotalVotes(String playerName){
        UUID userID = U.getIdFromName(playerName);
        return SeriousVote.getInstance().getVoteSpreeSystem().getRecord(userID).getTotalVotes();
    }

    public static PlayerRecord getRecordByRank(int rank){
        if(!SeriousVote.getInstance().usingMilestones()) return null;
        return SeriousVote.getInstance().getVoteSpreeSystem().getRecordByRank(rank);
    }

    public static int getTotalNumberOfVoters(){
        if(!SeriousVote.getInstance().usingMilestones()) return 0;
        return SeriousVote.getInstance().getVoteSpreeSystem().getNumberOfVoters();
    }

}
