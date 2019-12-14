package net.adamsanchez.seriousvote.Data;

import java.sql.Date;
import java.util.UUID;

/**
 * Created by adam_ on 01/23/17.
 */
public class PlayerRecord {



    private String playerIdentifier;
    private int totalVotes, voteSpree;
    private Date lastVote;

    public PlayerRecord(String playerIdentifier, int totalVotes, int voteSpree, Date lastVote){
        this.playerIdentifier = playerIdentifier;
        this.totalVotes = totalVotes;
        this.voteSpree = voteSpree;
        this.lastVote = lastVote;
    }

    public static PlayerRecord getBlankRecord(String playerIdentifier){
        return new PlayerRecord(playerIdentifier, 1,1, new Date(new java.util.Date().getTime()));
    }

    public String getPlayerIdentifier() {
        return playerIdentifier;
    }
    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public int getVoteSpree() {
        return voteSpree;
    }

    public void setVoteSpree(int voteSpree) {
        this.voteSpree = voteSpree;
    }

    public Date getLastVote() {
        return lastVote;
    }

    public void setLastVote(Date lastVote) {
        this.lastVote = lastVote;
    }

}
