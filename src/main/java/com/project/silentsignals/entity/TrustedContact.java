package com.project.silentsignals.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(TrustedContactId.class)
@Table(name = "trusted_contacts")
public class TrustedContact {

    @Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    private Long contactUserId; // elave olunan istifadchi
}
