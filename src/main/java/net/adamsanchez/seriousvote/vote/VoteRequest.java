package net.adamsanchez.seriousvote.vote;

import com.vexsoftware.votifier.model.Vote;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class VoteRequest extends Vote {
    private List<String> rewardStore = new LinkedList<String>();
    private Status voteStatus = Status.WAITING;
    private Set<String> rewardNames = new HashSet<String>();

    public void addReward(String command){
        rewardStore.add(command);
    }

    public List<String> getRewards(){
        return rewardStore;
    }

    public boolean hasRewards(){
        return !rewardStore.isEmpty();
    }

    public Status getVoteStatus(){
        return voteStatus;
    }
    public void setVoteStatus(Status status){
        this.voteStatus = status;
    }


}
