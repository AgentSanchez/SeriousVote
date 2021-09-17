package net.adamsanchez.seriousvote.events;

import net.adamsanchez.seriousvote.Data.PlayerRecord;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.utils.CM;
import net.adamsanchez.seriousvote.vote.VoteRequest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.impl.AbstractEvent;

import java.util.LinkedList;
import java.util.List;

public class VoteProcessedEvent extends AbstractEvent {

    private final Cause cause;
    private final VoteRequest vr;
    private final int voteSpree;
    private final int totalVotes;
    private final int nextMilestone;
    private final List<String> rewardList;
    private final String playerName;
    private final String voteSite;
    private final String timeStamp;

    public VoteProcessedEvent(VoteRequest vr, Cause cause) {
        CommentedConfigurationNode rootNode = SeriousVote.getInstance().getMainCfgNode();
        SeriousVote sv = SeriousVote.getInstance();
        this.cause = cause;
        this.vr = vr;
        this.rewardList = new LinkedList<String>();
        this.rewardList.addAll(vr.getRewardNames());
        this.playerName = vr.getUsername();
        this.voteSite = vr.getServiceName();
        this.timeStamp = vr.getTimeStamp();
        if (CM.getMilestonesEnabled(rootNode) || CM.getDailiesEnabled(rootNode)) {
            PlayerRecord pr = sv.getVoteSpreeSystem().getRecord(vr.getUsername());
            nextMilestone = sv.getVoteSpreeSystem().getRemainingMilestoneVotes(pr.getTotalVotes());
            totalVotes = pr.getTotalVotes();
            voteSpree = pr.getVoteSpree();
        } else {
            nextMilestone = -1;
            totalVotes = -1;
            voteSpree = -1;
        }

        if (CM.getDailiesEnabled(rootNode)) {

        }
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    public VoteRequest getVr() {
        return vr;
    }

    public int getVoteSpree() {
        return voteSpree;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public int getNextMilestone() {
        return nextMilestone;
    }

    public List<String> getRewardList() {
        return rewardList;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getVoteSite() {
        return voteSite;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
