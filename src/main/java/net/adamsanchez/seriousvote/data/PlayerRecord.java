package net.adamsanchez.seriousvote.data;

import java.sql.Date;
import java.util.UUID;

/**
 * Created by adam_ on 01/23/17.
 */
public class PlayerRecord {



    UUID uuid;



    int totalVotes, voteSpree;
    Date lastVote;

    public PlayerRecord(UUID uuid, int totalVotes, int voteSpree, Date lastVote){
        this.uuid = uuid;
        this.totalVotes = totalVotes;
        this.voteSpree = voteSpree;
        this.lastVote = lastVote;
    }

    public static PlayerRecord getBlankRecord(UUID player){
        return new PlayerRecord(player, 0,0, new Date(new java.util.Date().getTime()));
    }

    public UUID getUuid() {
        return uuid;
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
