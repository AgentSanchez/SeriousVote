package net.adamsanchez.seriousvote.vote;

import com.vexsoftware.votifier.model.Vote;

import java.util.LinkedList;
import java.util.List;

public class VoteRequest extends Vote {
    private List<String> rewardStore = new LinkedList<String>();
    private Status voteStatus = Status.WAITING;

    public void addReward(String command){
        rewardStore.add(command);
    }

    public List<String> getRewards(){
        return rewardStore;
    }


}
