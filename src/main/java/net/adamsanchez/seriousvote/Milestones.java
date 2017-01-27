package net.adamsanchez.seriousvote;


import java.sql.Date;
import java.util.UUID;

/**
 * Created by adam_ on 01/22/17.
 */
public class Milestones {

    String msgYear = "{player} Has voted for a year straight!!! He's earned a prize!";
    String msgMonth = "{player} Has voted for a month straight!!! He's earned a prize!";
    String msgWeek = "{player} Has voted for a week straight!!! He's earned a prize!";

    //query database for person
    //Check for last day of voting
    //check number of sequential votes
    //If more than or equal to the milestone give reward
    Database db;
    public Milestones(){
        db = new Database();
        db.createPlayerTable();
    }

    //TODO Create Milestones Table
    //Each milestone table will have a random  selection
    //Each milestone table will have a set selection

    public boolean updateRecord(UUID player, int totalVotes, int voteSpree, Date lastVote){
        PlayerRecord record = new PlayerRecord(player, totalVotes, voteSpree, lastVote);
        db.updatePlayer(record);
        return true;
    }
    public boolean updateRecord(PlayerRecord record){
        db.updatePlayer(record);
        return true;
    }

    public PlayerRecord getRecord(UUID player){
        return db.getPlayer(player);
    }

    public void checkForMilestones(PlayerRecord record, String playerName){

        //yearly
        if(record.getVoteSpree()%365 == 0){
            U.bcast(msgYear,playerName);

        }
        //monthly
        else if(record.getVoteSpree()%30 == 0){
            U.bcast(msgMonth,playerName);
        }
        //weekly
        if(record.getVoteSpree()%7 == 0){
            U.bcast(msgWeek,playerName);

        }


    }

    public void addVote(UUID player){
        PlayerRecord record = getRecord(player);
        if(record == null){
            U.info("Creating a new record for " + player.toString() + ".");
            record = PlayerRecord.getBlankRecord(player);
            record.setLastVote(new Date(new java.util.Date().getTime()));
            record.setTotalVotes(1);
            record.setVoteSpree(1);
        } else {
            // If it's been a day since the last vote, increase the vote spree and change the lastvote
            if(new java.util.Date().getTime() - record.getLastVote().getTime() >= 86400000 ) {
                U.info(new java.util.Date().getTime() + " - " + record.getLastVote().getTime() + " is > or < than " + 86400000 );
                record.setVoteSpree(record.getVoteSpree() + 1);
                record.setLastVote(new Date(new java.util.Date().getTime()));
            }
            record.setTotalVotes(record.getTotalVotes() + 1);
            //TODO make it tell the player how many days left till his next reward!
            updateRecord(record);
            checkForMilestones(record, U.getName(player));

        }
    }

    public void reloadDB(){
        this.db = new Database();
    }




}
