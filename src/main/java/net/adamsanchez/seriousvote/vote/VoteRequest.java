package net.adamsanchez.seriousvote.vote;

import com.vexsoftware.votifier.model.Vote;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class VoteRequest {

    private String serviceName;
    private String username;
    private String timeStamp;

    private List<String> rewardStore = new LinkedList<String>();
    private Status voteStatus = Status.WAITING;
    private List<String> rewardNames = new LinkedList<String>();

    public VoteRequest(){}
    public VoteRequest(Vote v){
        username = v.getUsername();
        serviceName = v.getServiceName();
        timeStamp = v.getTimeStamp();
    }
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


    public List<String> getRewardNames() {
        return rewardNames;
    }

    public void addRewardName(String name) {
        this.rewardNames.add(name);
    }


    public void addReward(String command) {
        rewardStore.add(command);
    }

    public List<String> getRewards() {
        return rewardStore;
    }

    public boolean hasRewards() {
        return !rewardStore.isEmpty();
    }

    public Status getVoteStatus() {
        return voteStatus;
    }

    public void setVoteStatus(Status status) {
        this.voteStatus = status;
    }


}
