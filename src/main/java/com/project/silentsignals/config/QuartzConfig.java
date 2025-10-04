package com.project.silentsignals.config;


import com.project.silentsignals.scheduler.SosEscalationJob;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
public class QuartzConfig {

    public static JobDetail buildJobDetail(Long alertId) {
        return JobBuilder.newJob(SosEscalationJob.class)
                .withIdentity("sosEscalationJob_" + alertId, "sos-jobs")
                .withDescription("SOS Escalation Job")
                .usingJobData("alertId", alertId)
                .storeDurably()
                .build();
    }

    public static Trigger buildJobTrigger(JobDetail jobDetail, int delayMinutes) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "sos-triggers")
                .withDescription("SOS Escalation Trigger")
                .startAt(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(delayMinutes)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}