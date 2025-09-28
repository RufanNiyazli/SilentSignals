package com.project.silentsignals.entity;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrustedContactId implements Serializable {
    private User user;
    private Long contactUserId;
}