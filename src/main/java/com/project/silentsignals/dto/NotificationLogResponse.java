package com.project.silentsignals.dto;

import java.time.LocalDateTime;

public record NotificationLogResponse(String contactEmail, String channel, LocalDateTime sentAt) {
}
