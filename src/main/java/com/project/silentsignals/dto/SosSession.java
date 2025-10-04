package com.project.silentsignals.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosSession implements Serializable {
    private Long sosAlertId;
    private Long triggeringId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;

}
