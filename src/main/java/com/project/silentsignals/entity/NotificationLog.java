package com.project.silentsignals.entity;

import com.project.silentsignals.enums.Channel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_alert_id")
    private SosAlert sosAlert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private User contact;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private LocalDateTime sentAt;
}