package com.project.silentsignals.scheduler;

import com.project.silentsignals.dto.SosSession;
import com.project.silentsignals.entity.NotificationLog;
import com.project.silentsignals.entity.SosAlert;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.enums.AlertStatus;
import com.project.silentsignals.enums.Channel;
import com.project.silentsignals.repository.NotificationLogRepository;
import com.project.silentsignals.repository.SosAlertRepository;
import com.project.silentsignals.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SosEscalationJob extends QuartzJobBean {
    private final RedisTemplate<String, Object> redisTemplate;
    private final SosAlertRepository sosAlertRepository;
    private final INotificationService notificationService;
    private final NotificationLogRepository notificationLogRepository;


    @Override
    @Transactional
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        long alertId = context.getJobDetail().getJobDataMap().getLong("alertId");
        String sessionKey = "sos:session:" + alertId;

        Object sessionObj = redisTemplate.opsForValue().get(sessionKey);

        if (sessionObj == null) {
            log.info("SOS escalation for alertId {} cancelled. Session not found in Redis (resolved).", alertId);
            return;
        }

        SosSession session = convertToSosSession(sessionObj);

        log.warn("SOS alertId {} escalating...", alertId);

        SosAlert sosAlert = sosAlertRepository.findByIdWithUserAndContacts(alertId)
                .orElseThrow(() -> new RuntimeException("Cannot escalate: SosAlert not found " + alertId));

        sosAlert.setStatus(AlertStatus.ESCALATED);
        sosAlertRepository.save(sosAlert);

        User triggeringUser = sosAlert.getUser();

        log.info("Triggering user: {}, contacts count: {}", triggeringUser.getId(), triggeringUser.getContacts().size());

        if (triggeringUser.getContacts().isEmpty()) {
            log.warn("No contacts found for user {}, skipping email escalation", triggeringUser.getId());
            return;
        }

        triggeringUser.getContacts().forEach(contact -> {
            User contactUser = contact.getContactUser();
            log.info("Processing contact: {}, contactUser: {}", contact.getId(), contactUser != null ? contactUser.getId() : "null");

            if (contactUser != null) {
                log.info("Attempting to send email to: {}", contactUser.getEmail());
                if (notificationService.sendEmailAlert(contactUser, triggeringUser,
                        sosAlert.getLatitude(), sosAlert.getLongitude())) {
                    logNotification(sosAlert, contactUser, Channel.EMAIL);
                }
            }
        });
    }
    private SosSession convertToSosSession(Object obj) {
        if (obj instanceof LinkedHashMap) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) obj;
            return SosSession.builder()
                    .createdAt(LocalDateTime.parse((String) map.get("createdAt")))
                    .triggeringId(((Number) map.get("triggeringId")).longValue())
                    .sosAlertId(((Number) map.get("sosAlertId")).longValue())
                    .latitude((Double) map.get("latitude"))
                    .longitude((Double) map.get("longitude"))
                    .build();
        }
        return (SosSession) obj;
    }

    private void logNotification(SosAlert alert, User contact, Channel channel) {
        NotificationLog notificationLog = NotificationLog.builder().sosAlert(alert).contact(contact).channel(channel).sentAt(LocalDateTime.now()).build();
        notificationLogRepository.save(notificationLog);
        log.info("Logged {} notification for alertId {} to contact {}", channel, alert.getId(), contact.getId());
    }
}
