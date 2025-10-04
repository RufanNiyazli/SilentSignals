package com.project.silentsignals.service.impl;

import com.project.silentsignals.config.QuartzConfig;
import com.project.silentsignals.dto.SosRequest;
import com.project.silentsignals.dto.SosSession;
import com.project.silentsignals.dto.SosWebSocketMessage;
import com.project.silentsignals.entity.Contact;
import com.project.silentsignals.entity.NotificationLog;
import com.project.silentsignals.entity.SosAlert;
import com.project.silentsignals.entity.User;
import com.project.silentsignals.enums.AlertStatus;
import com.project.silentsignals.enums.Channel;
import com.project.silentsignals.repository.NotificationLogRepository;
import com.project.silentsignals.repository.SosAlertRepository;
import com.project.silentsignals.repository.UserRepository;
import com.project.silentsignals.service.ISosService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SosServiceImpl implements ISosService {
    private static final int SOS_TTL_MINUTES = 3;

    private final UserRepository userRepository;
    private final SosAlertRepository sosAlertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationLogRepository notificationLogRepository;
    private final QuartzConfig quartzConfig;
    private final Scheduler scheduler;

    @Override
    @Transactional
    public void triggerSos(String userEmail, SosRequest sosRequest) {
        User user = userRepository.findUserByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found!" + userEmail));
        Set<Contact> contacts = user.getContacts();
        if (contacts.isEmpty()) {
            log.warn("User {} triggered SOS but has no contacts.", user.getId());
            return;
        }
        SosAlert sosAlert = saveInitialAlert(user, sosRequest, AlertStatus.PENDING);
        SosSession sosSession = SosSession.builder()
                .createdAt(sosAlert.getCreatedAt())
                .triggeringId(user.getId())
                .sosAlertId(sosAlert.getId())
                .latitude(sosRequest.latitude())
                .longitude(sosRequest.longitude())
                .build();
        String sessionKey = "sos:session" + sosAlert.getId();
        redisTemplate.opsForValue().set(sessionKey, sosSession, SOS_TTL_MINUTES, TimeUnit.MINUTES);

        log.info("Sos Session tored in redis.", sessionKey);
        broadcastToContacts(sosAlert, user, contacts);
        scheduleEscalationJob(sosAlert.getId());


    }

    @Override
    @Transactional
    public void resolveSos(Long alertId, String respondingUserEmail) {
        User respondingUser = userRepository.findUserByEmail(respondingUserEmail).orElseThrow(() -> new RuntimeException("Responding email not found" + respondingUserEmail));
        SosAlert sosAlert = sosAlertRepository.findById(alertId).orElseThrow(() -> new RuntimeException("Sos Alert not found id:" + alertId));
        if (sosAlert.getStatus() == AlertStatus.PENDING) {
            sosAlert.setStatus(AlertStatus.RESOLVED);
            sosAlertRepository.save(sosAlert);
            log.info("SosAlert status set Resolved{}", sosAlert.getId());
            String sessionKey = "sos:session:" + alertId;
            if (redisTemplate.delete(sessionKey)) {
                log.info("Sos session deleted from redis{}", sosAlert.getId());

            } else {
                log.warn("Attempted to resolve an alert that is not PENDING. Alert ID: {}, Status: {}", sosAlert.getId(), sosAlert.getStatus());
            }
        }

    }

    private SosAlert saveInitialAlert(User user, SosRequest request, AlertStatus status) {
        SosAlert sosAlert = SosAlert.builder()
                .createdAt(LocalDateTime.now())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .status(status)
                .user(user)
                .build();

        return sosAlertRepository.save(sosAlert);
    }

    private void broadcastToContacts(SosAlert sosAlert, User triggeringUser, Set<Contact> contacts) {
        SosWebSocketMessage webSocketMessage = new SosWebSocketMessage(sosAlert.getId(), triggeringUser.getUsername(), sosAlert.getLatitude(), sosAlert.getLongitude(), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        contacts.forEach(contact -> {
            User contactUser = contact.getContactUser();
            String destination = "/topic/sos/" + contactUser.getId();
            messagingTemplate.convertAndSend(destination, webSocketMessage);
            NotificationLog notificationLog = NotificationLog.builder()
                    .contact(contactUser).sosAlert(sosAlert).channel(Channel.WEBSOCKET).sentAt(LocalDateTime.now())
                    .build();
            notificationLogRepository.save(notificationLog);
            log.info("Sos Alert send and logged for contact via WebSocket{}", contactUser.getId());

        });
    }

    private void scheduleEscalationJob(Long alertId) {
        try {
            JobDetail jobDetail = QuartzConfig.buildJobDetail(alertId);
            Trigger trigger = QuartzConfig.buildJobTrigger(jobDetail, SOS_TTL_MINUTES);
            scheduler.scheduleJob(trigger);
            log.info("Scheduled SOS escalation job for alertId{}", alertId);
        } catch (SchedulerException e) {
            log.error("Error scheduling SOS escalation job for alertId:{}", alertId, e);

        }
    }

}
