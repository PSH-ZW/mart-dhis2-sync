package com.thoughtworks.martdhis2sync;

import com.thoughtworks.martdhis2sync.dao.EventDAO;
import com.thoughtworks.martdhis2sync.model.AnalyticsCronJob;
import com.thoughtworks.martdhis2sync.service.LoggerService;
import com.thoughtworks.martdhis2sync.service.SyncService;
import org.apache.logging.log4j.core.util.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
public class JobScheduler implements SchedulingConfigurer {

    @Autowired
    protected LoggerService loggerService;

    @Autowired
    protected EventDAO eventDAO;

    @Autowired
    protected ThreadPoolTaskScheduler threadPoolTaskScheduler;
    
    @Autowired
    protected SyncService syncService;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        final AnalyticsCronJob cronJob = eventDAO.getSyncCronJob();
        try {
            taskRegistrar.setScheduler(threadPoolTaskScheduler);
            taskRegistrar.addTriggerTask(getTask(cronJob), getTrigger(cronJob));
        } catch (ParseException e) {
            loggerService.collateLogMessage("Could not parse the cron expression: " + cronJob.getExpression() + " for: " +
                    cronJob.getName());
            e.printStackTrace();
        }
    }

    private Trigger getTrigger(AnalyticsCronJob quartzCronScheduler) throws ParseException {
        PeriodicTrigger periodicTrigger;
        Date now = new Date();
        long nextExecutionTimeByStatement = new CronExpression(quartzCronScheduler.getExpression()).
                getNextValidTimeAfter(now).getTime();
        periodicTrigger = new PeriodicTrigger((int) (nextExecutionTimeByStatement - now.getTime()), TimeUnit.MILLISECONDS);
        periodicTrigger.setInitialDelay(quartzCronScheduler.getStartDelay());
        return periodicTrigger;
    }

    private Runnable getTask(final AnalyticsCronJob quartzCronScheduler) {
        return new Runnable() {
            @Override
            public void run() {
                syncService.syncToDhis();
                try {
                    loggerService.collateLogMessage("Triggering job: " + quartzCronScheduler.getName());

                } catch (Exception e) {
                    loggerService.collateLogMessage("Thread Failed for the job: " + quartzCronScheduler.getName());
                }
            }
        };
    }

}
