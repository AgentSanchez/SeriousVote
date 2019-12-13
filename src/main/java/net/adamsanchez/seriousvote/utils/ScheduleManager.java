package net.adamsanchez.seriousvote.utils;

import net.adamsanchez.seriousvote.Data.OfflineHandler;
import net.adamsanchez.seriousvote.SeriousVote;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class ScheduleManager {

    ArrayList<Task> taskList;

    public ScheduleManager run(){
        SeriousVote sv = SeriousVote.getInstance();
        Scheduler scheduler = Sponge.getScheduler();
        Task.Builder taskBuilder = scheduler.createTaskBuilder();
        taskList = new ArrayList<>();
        Task task = taskBuilder.execute(() -> taskCheckForUnprocessedVotes())
                .interval(700, TimeUnit.MILLISECONDS)
                .name("SeriousVote-CommandRewardExecutor")
                .submit(sv.getPlugin());
        taskList.add(task);

        if(sv.isMilestonesEnabled() && CM.getMonthlyResetEnabled(sv.getMainCfgNode())){
            U.info("Setting up monthly reset...");
            Task checkForResets = taskBuilder.execute(() -> taskCheckForMonthlyReset())
                    .interval(2, TimeUnit.HOURS)
                    .name("SeriousVote-MonthlyResetService")
                    .submit(sv.getPlugin());
            taskList.add(checkForResets);
        }

        return this;
    }

    public static void taskCheckForMonthlyReset(){
        SeriousVote sv = SeriousVote.getInstance();
        U.debug("Checking for reset.....");
        if(new java.util.Date().getTime() - OfflineHandler.retrieveLastReset().getTime() >= 86400001){
            Calendar c = Calendar.getInstance();

            if (c.get(Calendar.DAY_OF_MONTH) == CM.getMonthlyResetDay(sv.getMainCfgNode())){
                U.info("It's the #" + CM.getMonthlyResetDay(sv.getMainCfgNode()) + " day of the month. Resetting all vote totals to 0!");
                sv.getVoteSpreeSystem().resetPlayerVotes();
                OfflineHandler.storeLastReset(new java.util.Date());
            }
        } else
        {
            U.debug("Too soon since last reset....");
        }
    }

    public static void taskCheckForUnprocessedVotes(){
        if(SeriousVote.getInstance().hasUnprocessedVotes()){
            SeriousVote.getInstance().processVotes();
        }
    }
}
