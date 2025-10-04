package com.project.silentsignals.dto;

public record SosWebSocketMessage(Long alertId, String fromUser, Double latitude, Double longitude, String timeStamp) {
}
