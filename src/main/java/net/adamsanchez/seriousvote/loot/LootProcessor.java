package net.adamsanchez.seriousvote.loot;

import net.adamsanchez.seriousvote.utils.*;
import net.adamsanchez.seriousvote.vote.Status;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.stream.Collectors;

public class LootProcessor {

    public VoteRequest processVoteChanceTables(VoteRequest vr) {
        //Workflow Level 4
        VoteRequest workingRequest = vr;
        U.debug("Adding SetCommands to the process queue");
        for (String setCommand : CM.getSetCommands()) {
            workingRequest.addReward(setCommand);
        }
        if (!CM.getAreLootTablesAvailable() || CM.getRandomDisabled()) {
            workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
            return workingRequest;
        }
        //Setup Loot Table and gather rewards
        int maxNumberOfRewards = LootTools.genNumRandRewards(CM.getRandomRewardsNumber(), CM.getRandomMin(), CM.getRandomMax());
        for (int i = 0; i < maxNumberOfRewards; i++) {
            LootTable mainLoot = new LootTable(LootTools.chooseTable(chanceMap, mainRewardTables));
            U.debug("Choosing a random reward.");
            String chosenReward = mainLoot.chooseReward();
            U.debug("Chose: " + chosenReward);
            workingRequest.addRewardName(CM.getRewardNameById(chosenReward));
            for (String ix : CM.getRewardsByID(chosenReward)) {
                workingRequest.addReward(OutputHelper.parseVariables(ix, workingRequest.getUsername()));
                U.debug(CC.YELLOW + "QUEUED: " + CC.WHITE + ix);
            }
        }
        workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
        return workingRequest;
    }
}
