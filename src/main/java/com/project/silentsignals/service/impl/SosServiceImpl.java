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
import com.project.silentsignals.repository.ContactRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
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
    private final ContactRepository contactRepository;

    @Override
    @Transactional
    public void triggerSos(String userEmail, SosRequest sosRequest) {
        var user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        List<Contact> contactsList = contactRepository.findByOwnerIdWithContactUser(user.getId());
        Set<Contact> contacts = new LinkedHashSet<>(contactsList);

        log.info("User {} triggered SOS. Contacts found: {}", user.getId(), contacts.size());

        // SOS alert hemise yarat
        var sosAlert = saveSosAlert(user, sosRequest, AlertStatus.PENDING);
        log.info("Created SOS alert {} for user {}", sosAlert.getId(), user.getId());

        // Redis  SOS session saxla
        storeSosSessionInRedis(sosAlert, user, sosRequest);

        if (contacts.isEmpty()) {
            log.warn("User {} triggered SOS but has no contacts. Alert created but no notifications sent.", user.getId());
        } else {
            // Contacts a SOS u broadcast et
            broadcastToContacts(sosAlert, user, contacts);
        }

        if (user.getSosAlerts() != null && !user.getSosAlerts().isEmpty()) {
            for (SosAlert alert : user.getSosAlerts()) {
                if (alert.getStatus() == AlertStatus.PENDING && !alert.getId().equals(sosAlert.getId())) {
                    alert.setStatus(AlertStatus.TRIGGERED);
                    sosAlertRepository.save(alert);
                }
            }
        }

        // Escalation job u planlasdır
        scheduleEscalationJob(sosAlert.getId());
    }
    @Override
    @Transactional
    public void resolveSos(Long alertId, String respondingUserEmail) {
        var respondingUser = userRepository.findUserByEmail(respondingUserEmail)
                .orElseThrow(() -> new RuntimeException("Responding user not found: " + respondingUserEmail));

        var sosAlert = sosAlertRepository.findById(alertId)
                .orElseThrow(() -> new RuntimeException("Sos Alert not found: " + alertId));

        if (sosAlert.getStatus() == AlertStatus.PENDING) {
            sosAlert.setStatus(AlertStatus.RESOLVED);
            sosAlertRepository.save(sosAlert);
            log.info("SOS alert {} resolved by user {}", sosAlert.getId(), respondingUser.getId());

            var sessionKey = getRedisSessionKey(alertId);
            if (Boolean.TRUE.equals(redisTemplate.delete(sessionKey))) {
                log.info("SOS session {} removed from Redis", sessionKey);
            } else {
                log.warn("Failed to remove SOS session {} from Redis", sessionKey);
            }
        } else {
            log.warn("Attempted to resolve SOS alert {} which is not PENDING. Current status: {}",
                    alertId, sosAlert.getStatus());
        }
    }

    private SosAlert saveSosAlert(User user, SosRequest request, AlertStatus status) {
        var sosAlert = SosAlert.builder()
                .createdAt(LocalDateTime.now())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .status(status)
                .user(user)
                .build();
        return sosAlertRepository.save(sosAlert);
    }

    private void storeSosSessionInRedis(SosAlert sosAlert, User triggeringUser, SosRequest request) {
        var sosSession = SosSession.builder()
                .createdAt(sosAlert.getCreatedAt())
                .triggeringId(triggeringUser.getId())
                .sosAlertId(sosAlert.getId())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        var sessionKey = getRedisSessionKey(sosAlert.getId());
        redisTemplate.opsForValue().set(sessionKey, sosSession, SOS_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Stored SOS session in Redis: {}", sessionKey);
    }

    private void broadcastToContacts(SosAlert sosAlert, User triggeringUser, Set<Contact> contacts) {
        var message = new SosWebSocketMessage(
                sosAlert.getId(),
                triggeringUser.getUsername(),
                sosAlert.getLatitude(),
                sosAlert.getLongitude(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );

        for (var contact : contacts) {
            var contactUser = contact.getContactUser();
            if (contactUser != null) {
                var destination = "/topic/sos/" + contactUser.getId();

                messagingTemplate.convertAndSend(destination, message);
                saveNotificationLog(contactUser, sosAlert, Channel.WEBSOCKET);

                log.info("SOS alert {} sent via WebSocket to contact {}", sosAlert.getId(), contactUser.getId());
            } else {
                log.warn("Contact {} has null contactUser, skipping notification", contact.getId());
            }
        }
    }

    private void saveNotificationLog(User contactUser, SosAlert sosAlert, Channel channel) {
        var logEntry = NotificationLog.builder()
                .contact(contactUser)
                .sosAlert(sosAlert)
                .channel(channel)
                .sentAt(LocalDateTime.now())
                .build();
        notificationLogRepository.save(logEntry);
    }

    private void scheduleEscalationJob(Long alertId) {
        try {
            JobDetail jobDetail = QuartzConfig.buildJobDetail(alertId);
            Trigger trigger = QuartzConfig.buildJobTrigger(jobDetail, SOS_TTL_MINUTES);
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("Scheduled SOS escalation job for alertId {}", alertId);
        } catch (SchedulerException e) {
            log.error("Failed to schedule SOS escalation job for alertId {}", alertId, e);
        }
    }

    private String getRedisSessionKey(Long alertId) {
        return "sos:session:" + alertId;
    }
}