
package com.project.silentsignals.controller.impl;

import com.project.silentsignals.dto.*;
import com.project.silentsignals.entity.SosAlert;
import com.project.silentsignals.repository.SosAlertRepository;

import com.project.silentsignals.service.ISosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SosController {

    private final ISosService sosService;
    private final SosAlertRepository sosAlertRepository;

    @PostMapping("/send")
    public ResponseEntity<Void> sendSos(@RequestBody SosRequest sosRequest, @AuthenticationPrincipal UserDetails userDetails) {
        sosService.triggerSos(userDetails.getUsername(), sosRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/respond/{alertId}")
    public ResponseEntity<Void> respondToSos(@PathVariable Long alertId, @AuthenticationPrincipal UserDetails userDetails) {
        sosService.resolveSos(alertId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<SosHistoryResponse>> getSosHistory(@AuthenticationPrincipal UserDetails userDetails) {
        List<SosAlert> alerts = sosAlertRepository.findByUser_EmailOrderByCreatedAtDesc(userDetails.getUsername());
        List<SosHistoryResponse> history = alerts.stream().map(this::mapAlertToHistoryResponse).collect(Collectors.toList());
        return ResponseEntity.ok(history);
    }

    private SosHistoryResponse mapAlertToHistoryResponse(SosAlert alert) {
        List<NotificationLogResponse> notificationLogs = alert.getNotificationLogs().stream()
                .map(log -> new NotificationLogResponse(log.getContact().getEmail(), log.getChannel().name(), log.getSentAt()))
                .collect(Collectors.toList());
        return new SosHistoryResponse(alert.getId(), alert.getCreatedAt(), alert.getStatus().name(), alert.getLatitude(), alert.getLongitude(), notificationLogs);
    }
}