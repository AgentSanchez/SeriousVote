package net.adamsanchez.seriousvote.Data;

import net.adamsanchez.seriousvote.*;
import net.adamsanchez.seriousvote.loot.LootTable;
import net.adamsanchez.seriousvote.loot.LootTools;
import net.adamsanchez.seriousvote.utils.*;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by adam_ on 01/22/17.
 */
public class VoteSpreeSystem {

    String msgYear = "{player} Has voted for a year straight!!! He's earned a prize!";
    String msgMonth = "{player} Has voted for a month straight!!! He's earned a prize!";
    String msgWeek = "{player} Has voted for a week straight!!! He's earned a prize!";
    SeriousVote sv;


    //query database for person
    //Check for last day of voting
    //check number of sequential votes
    //If more than or equal to the milestone give reward
    Database db;

    public VoteSpreeSystem() {
        sv = SeriousVote.getInstance();
        db = new Database();
        db.createPlayerTable();
    }

    public boolean updateRecord(String playerIdentifier, int totalVotes, int voteSpree, Date lastVote) {
        PlayerRecord record = new PlayerRecord(playerIdentifier, totalVotes, voteSpree, lastVote);
        db.updatePlayer(record);
        return true;
    }

    public boolean updateRecord(PlayerRecord record) {
        db.updatePlayer(record);
        U.debug(CC.GREEN + record.toString());
        return true;
    }

    public PlayerRecord getRecord(String playerIdentifier) {

        PlayerRecord record = db.getPlayer(playerIdentifier);
        if (record == null) {
            return createRecord(playerIdentifier);
        }
        return record;
    }

    public void updateAllPlayerID() {
        ArrayList<PlayerRecord> recordList = db.getAllRecords();
        U.debug("Retrieved " + recordList.size() + " records from storage....");
        U.debug(CC.LINE);
        int numRecordsUpdated = 0, numAlreadyUpdated = 0;
        for (PlayerRecord record : recordList) {
            //If already name...
            if (!U.isUUID(record.getPlayerIdentifier())) {
                U.debug(CC.YELLOW + "Skipping record for player: " + record.getPlayerIdentifier() + ". Already Converted.");
                numAlreadyUpdated += 1;
                continue;
            }
            U.debug("Converting player with ID: " + record.getPlayerIdentifier());
            String newID = U.convertIDToName(record.getPlayerIdentifier());
            if (newID != null && newID != "") {
                PlayerRecord newRecord = new PlayerRecord(newID, record.getTotalVotes(), record.getVoteSpree(), record.getLastVote());
                db.updatePlayer(newRecord);
                U.debug("New player with new ID " + newID + " added...");
                U.debug(newRecord.toString());
                db.deletePlayer(record.getPlayerIdentifier());
                U.debug("Old player " + record.getPlayerIdentifier() + " deleted.");

                numRecordsUpdated += 1;
            }
        }
        U.debug(CC.LINE);
        U.debug(CC.CYAN + "Updated " + numRecordsUpdated + "/" + recordList.size() + " records. " + numAlreadyUpdated + " already updated.");
    }

    public void changePlayerID(PlayerRecord oldRecord, PlayerRecord newRecord) {
        updateRecord(newRecord);
        U.debug(CC.GREEN + "Record with ID " + newRecord.getPlayerIdentifier() + " added...");
        deleteRecord(oldRecord);
        U.debug(CC.RED + "Old record with ID " + oldRecord.getPlayerIdentifier() + " deleted.");
    }

    public void changePlayerID(String oldPlayerIdentifier, String newPlayerIdentifier) {
        PlayerRecord oldRecord, newRecord;
        oldRecord = db.getPlayer(oldPlayerIdentifier);
        if (oldRecord != null) {
            newRecord = new PlayerRecord(newPlayerIdentifier, oldRecord.getTotalVotes(), oldRecord.getVoteSpree(), oldRecord.getLastVote());
            changePlayerID(oldRecord, newRecord);
        } else {
            U.debug("Could not find player with id " + oldPlayerIdentifier);
        }

    }

    public void deleteRecord(PlayerRecord record) {
        db.deletePlayer(record.getPlayerIdentifier());
    }

    public PlayerRecord getRecordByRank(int rank) {
        PlayerRecord record = db.getRecordByRank(rank);
        U.debug("Request record for player in rank " + rank + ". Identifier: " + record.getPlayerIdentifier() + " Votes: " + record.getTotalVotes());
        return record == null ? null : record;
    }


    public void checkForMilestones(PlayerRecord record, String playerName) {
        //Check based on amount of votes given.
        U.info("Player has " + record.getTotalVotes() + " votes currently.");
        List<String> commandList = new ArrayList<String>();
        if (CM.getEnabledMilestones().length < 1)
            U.error("You have no enabled custom milestones or your config is broken :(");

        if (IntStream.of(CM.getEnabledMilestones()).anyMatch(x -> x == record.getTotalVotes())) {
            String chosenRewardTable = TableManager.chooseTable(CM.getMilestoneRandomRewardByNumber(record.getTotalVotes()));
            //TODO Check to see if that specific number provides any random rewards before trying to give them out.

            //Choose The Random Rewards from the chosen table
            if (chosenRewardTable != "") {
                LootTable chosenTable = new LootTable(chosenRewardTable);
                for (String command : CM.getRandomRewardById(chosenTable.chooseReward())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
            }
            //Add The Set Commands
            for (String command : CM.getMilestoneSetRewardByNumber(record.getTotalVotes())) {
                commandList.add(OutputHelper.parseVariables(command, playerName));
            }
            //Send the Commands to Be Run
            LootTools.giveReward(commandList);
            //Now Send a Public Message
            U.bcast(CM.getMilestonesMessageByNumber(record.getTotalVotes()), playerName);

        }

    }

    public void checkForDailies(PlayerRecord record, String playerName) {
        List<String> commandList = new ArrayList<String>();
        //yearly
        if (U.isPlayerOnline(playerName)) {
            if (record.getVoteSpree() >= 365 && record.getVoteSpree() % 365 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(CM.getYearlyRandomCommands()));
                //Choose The Random Rewards from the chosen table
                for (String command : CM.getRandomRewardById(chosenTable.chooseReward())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.yearlySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                LootTools.giveReward(commandList);
                U.bcast(CM.getYearlyMessage(), playerName);


            } else if (record.getVoteSpree() >= 30 && record.getVoteSpree() % 30 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(CM.getMonthlyRandomCommands()));
                //Choose The Random Rewards from the chosen table
                for (String command : CM.getRandomRewardById(chosenTable.chooseReward())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.monthlySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                LootTools.giveReward(commandList);
                U.bcast(CM.getMonthlyMessage(), playerName);

            } else if (record.getVoteSpree() >= 7 && record.getVoteSpree() % 7 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(CM.getWeeklyRandomCommands()));
                U.info("Chosing from Table: " + chosenTable.getTableName());
                //Choose The Random Rewards from the chosen table
                for (String command : CM.getRandomRewardById(chosenTable.chooseReward())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.weeklySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                LootTools.giveReward(commandList);
                U.bcast(CM.getWeeklyMessage(), playerName);
            }

            int leastDays = getRemainingDays(record.getVoteSpree());
            Player player = sv.getPublicGame().getServer().getPlayer(playerName).get();
            player.sendMessage(Text.of("You have " + leastDays + " left until your next dailies reward!").toBuilder().color(TextColors.GOLD).build());
        }


    }

    public void addVote(String playerIdentifier) {

        PlayerRecord record = getRecord(playerIdentifier);
        if (record == null) {
            U.info("Creating a new record for " + playerIdentifier.toString() + ".");
            record = PlayerRecord.getBlankRecord(playerIdentifier);
            record.setLastVote(new Date(new java.util.Date().getTime()));
            record.setTotalVotes(1);
            record.setVoteSpree(1);
            updateRecord(record);

        } else {
            // If it's been a day since the last vote, increase the vote spree and change the lastvote
            if (new java.util.Date().getTime() - record.getLastVote().getTime() >= 86400000) {
                //if it's been longer than a day then reset the voteSpree
                if (new java.util.Date().getTime() - record.getLastVote().getTime() >= 172800000) {
                    record.setVoteSpree(1);
                    record.setLastVote(new Date(new java.util.Date().getTime()));
                    record.setTotalVotes(record.getTotalVotes() + 1);
                    updateRecord(record);

                    return;
                }
                record.setVoteSpree(record.getVoteSpree() + 1);
                record.setLastVote(new Date(new java.util.Date().getTime()));
                record.setTotalVotes(record.getTotalVotes() + 1);
                updateRecord(record);

                if (sv.isDailiesEnabled()) checkForDailies(record, U.getName(playerIdentifier));
                if (sv.isMilestonesEnabled()) checkForMilestones(record, U.getName(playerIdentifier));
                return;
            }
            record.setTotalVotes(record.getTotalVotes() + 1);
            if (sv.isMilestonesEnabled()) checkForMilestones(record, U.getName(playerIdentifier));

            updateRecord(record);

        }
    }

    public PlayerRecord createRecord(String playerIdentifier) {
        U.info("Creating a new record for " + playerIdentifier.toString() + ".");
        PlayerRecord record;
        record = PlayerRecord.getBlankRecord(playerIdentifier);
        record.setLastVote(new Date(new java.util.Date().getTime()));
        record.setTotalVotes(0);
        record.setVoteSpree(0);
        updateRecord(record);
        return record;
    }

    public static int getRemainingDays(int currentSpree) {
        //System.out.println("INPUT: " + CC.BLUE + currentSpree + CC.RESET);
        int a = 365 * (currentSpree / 365 + 1) - currentSpree;
        int b = 30 * (currentSpree / 30 + 1) - currentSpree;
        int c = 7 * (currentSpree / 7 + 1) - currentSpree;
        //System.out.println("A" +a + " B" + b + " C" + c);
        int leastDays = -1;
        if (a < b && a < c) {
            leastDays = a;
            System.out.println("1");
        } else if (b < c && b < a) {
            leastDays = b;
            System.out.println("2");
        } else if (c < b && c < a) {
            leastDays = c;
            //System.out.println("3");
        }
        //System.out.println("     -RESULT: " + CC.GREEN + leastDays + CC.RESET);
        return leastDays;
    }

    /**
     * Resets all player votes to 0;
     */
    public void resetPlayerVotes() {
        db.resetPlayers();
    }

    public ArrayList<PlayerRecord> getAllRecords() {
        return db.getAllRecords();
    }

    public int getNumberOfVoters() {
        return db.getCount();
    }

    public void reloadDB() {
        this.db = new Database();
    }

    public void shutdown() {
        this.db.shutdown();
    }
}
