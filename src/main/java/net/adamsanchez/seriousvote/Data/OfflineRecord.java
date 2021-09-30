package net.adamsanchez.seriousvote.Data;

import java.util.HashMap;

public class OfflineRecord {
    private String username;
    private int offlineVotes;
    private HashMap<Integer, Integer> offlineMilestones;
    private HashMap<String, Integer> offlineDailies;

    public OfflineRecord(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getOfflineVotes() {
        return offlineVotes;
    }

    public void addOfflineVotes(int numVotes) {
        this.offlineVotes += numVotes;
    }

    public void removeOfflineVotes(int numVotes) {
        this.offlineVotes -= numVotes;
    }

    public HashMap<Integer, Integer> getOfflineMilestones() {
        return offlineMilestones;
    }

    public void addOfflineMilestone(int milestoneValue) {
        if (this.offlineMilestones.containsKey(milestoneValue)) {
            this.offlineMilestones.put(milestoneValue, offlineMilestones.get(milestoneValue) + 1);
        } else {
            this.offlineMilestones.put(milestoneValue, 1);
        }
    }

    public void removeOfflineMilestone(int milestoneValue) {
        if (this.offlineMilestones.containsKey(milestoneValue)) {
            this.offlineMilestones.put(milestoneValue, this.offlineMilestones.get(milestoneValue) - 1);
        }
        if (this.offlineMilestones.get(milestoneValue) < 1) offlineMilestones.remove(milestoneValue);
    }

    public HashMap<String, Integer> getOfflineDailies() {
        return offlineDailies;
    }

    public void addOfflineDaily(String dailyID) {
        if (this.offlineDailies.containsKey(dailyID)) {
            this.offlineDailies.put(dailyID, this.offlineDailies.get(dailyID) + 1);
        } else {
            this.offlineDailies.put(dailyID, 1);
        }
    }

    public void removeOfflineDaily(String dailyID) {
        if (this.offlineDailies.containsKey(dailyID)) {
            this.offlineDailies.put(dailyID, this.offlineDailies.get(dailyID) - 1);
        }
        if (this.offlineDailies.get(dailyID) < 1) this.offlineDailies.remove(dailyID);
    }

    public boolean isEmpty() {
        return offlineVotes < 1 && offlineDailies.size() < 1 && offlineMilestones.size() < 1;
    }
}
