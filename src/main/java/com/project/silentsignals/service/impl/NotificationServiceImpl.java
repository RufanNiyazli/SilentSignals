package com.project.silentsignals.service.impl;

import com.project.silentsignals.entity.User;
import com.project.silentsignals.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {
    private final JavaMailSender javaMailSender;

    @Override
    public boolean sendEmailAlert(User contactUser, User triggeringUser, Double latitude, Double longitude) {
        try {

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("developerx73@gmail.com");
            message.setTo(contactUser.getEmail());
            message.setSubject("URGENT Sos Alert Escalation from: " + triggeringUser.getEmail());
            String googleMapsLink = String.format("https://www.google.com/maps?q=%f,%f", latitude, longitude);
            String text = String.format("Dear %s,\n\nThis is an urgent escalation notice.\n\n%s has triggered an SOS alert, and it has not been resolved.\n\nLast known location:\nLatitude: %f\nLongitude: %f\n\nView on map: %s\n\nPlease take immediate action.\n\nSincerely,\nThe SilentSignals Team", contactUser.getUsername(), triggeringUser.getUsername(), latitude, longitude, googleMapsLink);
            message.setText(text);
            javaMailSender.send(message);
            log.info("Escalation email sent successfully to {}", contactUser.getEmail());

            return true;
        } catch (MailException e) {
            log.error("Failed to send escalation email to {}: {}", contactUser.getEmail(), e.getMessage());
            return false;
        }
    }
}
