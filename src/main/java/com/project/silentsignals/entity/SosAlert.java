package com.project.silentsignals.entity;

import com.project.silentsignals.enums.AlertStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sos_alerts")
public class SosAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "sosAlert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NotificationLog> notificationLogs;
}