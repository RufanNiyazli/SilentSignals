package com.project.silentsignals.config;


import com.project.silentsignals.scheduler.SosEscalationJob;
import org.quartz.*;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class QuartzConfig {
    public static JobDetail buildJobDetail(Long alertId, String sosSessionId) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("alertId", alertId);
        jobDataMap.put("sosSessionId", sosSessionId);
        return JobBuilder.newJob(SosEscalationJob.class)
                .withIdentity(UUID.randomUUID().toString(), "sos-jobs")
                .withDescription("SOS Escalation Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }
    public static Trigger buildJobTrigger(JobDetail jobDetail,int delayInSeconds){
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(),"sos-triggers")
                .withDescription("SOS Escalation Trigger")
                .startAt(DateBuilder.futureDate(delayInSeconds,DateBuilder.IntervalUnit.SECOND))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
