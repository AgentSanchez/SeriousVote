package net.adamsanchez.seriousvote.loot;

import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.LootTools;
import net.adamsanchez.seriousvote.utils.OutputHelper;
import net.adamsanchez.seriousvote.utils.U;
import net.adamsanchez.seriousvote.vote.Status;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.config.ConfigManager;

import java.util.stream.Collectors;

public class LootProcessor {

    public VoteRequest processVoteChanceTables(VoteRequest vr) {
        CommentedConfigurationNode mainCfgNode = SeriousVote.getInstance().getMainCfgNode();
        //Workflow Level 4
        VoteRequest workingRequest = vr;
        ConfigManager CM = SeriousVote.getInstance().
        U.debug("Adding SetCommands to the process queue");
        for (String setCommand : setCommands) {
            workingRequest.addReward(setCommand);
        }
        if (!lootTablesAvailable || randomDisabled) {
            workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
            return workingRequest;
        }
        //Setup Loot Table and gather rewards
        int maxNumberOfRewards = LootTools.genNumRandRewards(numRandRewards, minRandRewards, maxRandRewards);
        for (int i = 0; i < maxNumberOfRewards; i++) {
            LootTable mainLoot = new LootTable(LootTools.chooseTable(chanceMap, mainRewardTables), mainCfgNode);
            U.debug("Choosing a random reward.");
            String chosenReward = mainLoot.chooseReward();
            U.debug("Chose: " + chosenReward);
            workingRequest.addRewardName(mainCfgNode.getNode("config", "Rewards", chosenReward, "name").getString());
            for (String ix : mainCfgNode.getNode("config", "Rewards", chosenReward, "rewards").getChildrenList().stream()
                    .map(ConfigurationNode::getString).collect(Collectors.toList())) {
                workingRequest.addReward(OutputHelper.parseVariables(ix, workingRequest.getUsername()));
                U.debug(CC.YELLOW + "QUEUED: " + CC.WHITE + ix);
            }
        }
        workingRequest.setVoteStatus(Status.REWARDS_GATHERED);
        return workingRequest;
    }
}
