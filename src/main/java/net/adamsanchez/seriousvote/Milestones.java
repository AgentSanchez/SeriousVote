package net.adamsanchez.seriousvote;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by adam_ on 01/22/17.
 */
public class Milestones {

    String msgYear = "{player} Has voted for a year straight!!! He's earned a prize!";
    String msgMonth = "{player} Has voted for a month straight!!! He's earned a prize!";
    String msgWeek = "{player} Has voted for a week straight!!! He's earned a prize!";
    SeriousVote sv;

    //query database for person
    //Check for last day of voting
    //check number of sequential votes
    //If more than or equal to the milestone give reward
    Database db;
    public Milestones(){
        sv = SeriousVote.getInstance();
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
        List<String> commandList = new ArrayList<String>();
        //yearly
        if(record.getVoteSpree() >= 365 && record.getVoteSpree()%365 == 0){

            for(String command:sv.yearlySet) {
                commandList.add(sv.parseVariables(command, playerName));
            }
            sv.giveReward(commandList);

        }
        else if(record.getVoteSpree() >= 30 && record.getVoteSpree()%30 == 0){
            for(String command:sv.monthlySet) {
                commandList.add(sv.parseVariables(command, playerName));
            }
            sv.giveReward(commandList);
        }
        else if(record.getVoteSpree() >= 7 && record.getVoteSpree()%7 == 0){
            for(String command:sv.weeklySet) {
                commandList.add(sv.parseVariables(command, playerName));
            }
            sv.giveReward(commandList);
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
            updateRecord(record);
        } else {
            // If it's been a day since the last vote, increase the vote spree and change the lastvote
            if(new java.util.Date().getTime() - record.getLastVote().getTime() >= 86400000 ) {
                //if it's been longer than a day then reset the voteSpree
                if(new java.util.Date().getTime() - record.getLastVote().getTime() >= 172800000 ){
                    record.setVoteSpree(1);
                    record.setLastVote(new Date(new java.util.Date().getTime()));
                    updateRecord(record);

                    return;
                }
                record.setVoteSpree(record.getVoteSpree() + 1);
                record.setLastVote(new Date(new java.util.Date().getTime()));
                updateRecord(record);
                checkForMilestones(record, U.getName(player));
                return;
            }
            record.setTotalVotes(record.getTotalVotes() + 1);
            //TODO make it tell the player how many days left till his next reward!
            updateRecord(record);

        }
    }

    public void reloadDB(){
        this.db = new Database();
    }




}
