package com.project.silentsignals.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SosHistoryResponse(Long alertId, LocalDateTime timestamp, String status, Double latitude,
                                 Double longitude, List<NotificationLogResponse> notifications) {
}
