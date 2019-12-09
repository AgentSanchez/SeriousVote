package net.adamsanchez.seriousvote.api;

import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.U;

import java.util.UUID;

public class SeriousVoteAPI {

    //Return the player's total vote amout
    public static int getPlayerTotalVotes(String playerName){
        UUID userID = U.getIdFromName(playerName);
        return SeriousVote.getInstance().getMilestones().getRecord(userID).getTotalVotes();
    }

    public static PlayerRecord getRecordByRank(int rank){
        return SeriousVote.getInstance().getMilestones().getRecordByRank(rank);
    }

}
