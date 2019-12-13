package net.adamsanchez.seriousvote.Data;

import net.adamsanchez.seriousvote.*;
import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.utils.OutputHelper;
import net.adamsanchez.seriousvote.utils.U;
import ninja.leaping.configurate.ConfigurationNode;


import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by adam_ on 01/22/17.
 */
public class VoteSpreeSystem {

    String msgYear = "{player} Has voted for a year straight!!! He's earned a prize!";
    String msgMonth = "{player} Has voted for a month straight!!! He's earned a prize!";
    String msgWeek = "{player} Has voted for a week straight!!! He's earned a prize!";
    SeriousVote sv;
    ConfigurationNode rootNode;


    //query database for person
    //Check for last day of voting
    //check number of sequential votes
    //If more than or equal to the milestone give reward
    Database db;

    public VoteSpreeSystem(ConfigurationNode node) {
        sv = SeriousVote.getInstance();
        db = new Database();
        db.createPlayerTable();
        rootNode = node;
    }

    public boolean updateRecord(UUID player, int totalVotes, int voteSpree, Date lastVote) {
        PlayerRecord record = new PlayerRecord(player, totalVotes, voteSpree, lastVote);
        db.updatePlayer(record);
        return true;
    }

    public boolean updateRecord(PlayerRecord record) {
        db.updatePlayer(record);
        return true;
    }

    public PlayerRecord getRecord(UUID player) {

        PlayerRecord record = db.getPlayer(player);
        if (record == null) {
            return createRecord(player);
        }
        return record;
    }

    public PlayerRecord getRecordByRank(int rank){
        PlayerRecord record = db.getRecordByRank(rank);
        U.debug("Request record for player in rank " + rank + ". UUID: " + record.getUuid().toString() + " Votes: " + record.getTotalVotes());
        return record == null ? null : record;
    }


    public void checkForMilestones(PlayerRecord record, String playerName) {
        //Check based on amount of votes given.
        U.info("Player has " + record.getTotalVotes() + " votes currently.");
        List<String> commandList = new ArrayList<String>();
        if (CM.getEnabledMilestones(rootNode).length < 1)
            U.error("You have no enabled custom milestones or your config is broken :(");

        if (IntStream.of(CM.getEnabledMilestones(rootNode)).anyMatch(x -> x == record.getTotalVotes())) {
            String chosenRewardTable = TableManager.chooseTable(rootNode.getNode("config", "milestones", "records", "" + record.getTotalVotes(), "random"));
            //TODO Check to see if that specific number provides any random rewards before trying to give them out.

            //Choose The Random Rewards from the chosen table
            if (chosenRewardTable != "") {
                LootTable chosenTable = new LootTable(chosenRewardTable, rootNode);
                for (String command : rootNode.getNode("config", "Rewards", chosenTable.chooseReward(), "rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
            }
            //Add The Set Commands
            for (String command : rootNode.getNode("config", "milestones", "records", "" + record.getTotalVotes(), "set").getChildrenList().stream()
                    .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                commandList.add(OutputHelper.parseVariables(command, playerName));
            }
            //Send the Commands to Be Run
            sv.giveReward(commandList);
            //Now Send a Public Message
            U.bcast(rootNode.getNode("config", "milestones", "records", "" + record.getTotalVotes(), "message").getString(), playerName);

        }

    }

    public void checkForDailies(PlayerRecord record, String playerName) {
        List<String> commandList = new ArrayList<String>();
        //yearly
        if (U.isOnline(playerName)) {
            if (record.getVoteSpree() >= 365 && record.getVoteSpree() % 365 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(rootNode.getNode("config", "dailies", "yearly", "random")), rootNode);
                //Choose The Random Rewards from the chosen table
                for (String command : rootNode.getNode("config", "Rewards", chosenTable.chooseReward(), "rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.yearlySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                sv.giveReward(commandList);
                U.bcast(rootNode.getNode("config", "dailies", "yearly", "message").getString(), playerName);


            } else if (record.getVoteSpree() >= 30 && record.getVoteSpree() % 30 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(rootNode.getNode("config", "dailies", "monthly", "random")), rootNode);
                //Choose The Random Rewards from the chosen table
                for (String command : rootNode.getNode("config", "Rewards", chosenTable.chooseReward(), "rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.monthlySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                sv.giveReward(commandList);
                U.bcast(rootNode.getNode("config", "dailies", "monthly", "message").getString(), playerName);

            } else if (record.getVoteSpree() >= 7 && record.getVoteSpree() % 7 == 0) {
                LootTable chosenTable = new LootTable(TableManager.chooseTable(rootNode.getNode("config", "dailies", "weekly", "random")), rootNode);
                U.info("Chosing from Table: " + chosenTable.getTableName());
                //Choose The Random Rewards from the chosen table
                for (String command : rootNode.getNode("config", "Rewards", chosenTable.chooseReward(), "rewards").getChildrenList().stream()
                        .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                for (String command : sv.weeklySet) {
                    commandList.add(OutputHelper.parseVariables(command, playerName));
                }
                sv.giveReward(commandList);
                U.bcast(rootNode.getNode("config", "dailies", "weekly", "message").getString(), playerName);
            }

            int leastDays = getRemainingDays(record.getVoteSpree());
            Player player = sv.getPublicGame().getServer().getPlayer(playerName).get();
            player.sendMessage(Text.of("You have " + leastDays + " left until your next dailies reward!").toBuilder().color(TextColors.GOLD).build());
        }


    }

    public void addVote(UUID player) {

        PlayerRecord record = getRecord(player);
        if (record == null) {
            U.info("Creating a new record for " + player.toString() + ".");
            record = PlayerRecord.getBlankRecord(player);
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

                if (sv.isDailiesEnabled()) checkForDailies(record, U.getName(player));
                if (sv.isMilestonesEnabled()) checkForMilestones(record, U.getName(player));
                return;
            }
            record.setTotalVotes(record.getTotalVotes() + 1);
            if (sv.isMilestonesEnabled()) checkForMilestones(record, U.getName(player));

            updateRecord(record);

        }
    }

    public PlayerRecord createRecord(UUID player) {
        U.info("Creating a new record for " + player.toString() + ".");
        PlayerRecord record;
        record = PlayerRecord.getBlankRecord(player);
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
    public void resetPlayerVotes(){
        db.resetPlayers();
    }

    public int getNumberOfVoters(){
        return db.getCount();
    }
    public void reloadDB() {
        this.db = new Database();
    }

    public void shutdown() {
        this.db.shutdown();
    }
}
