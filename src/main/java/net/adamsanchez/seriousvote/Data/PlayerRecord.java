package net.adamsanchez.seriousvote.Data;

import javax.xml.crypto.Data;
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



}
