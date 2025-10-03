
package com.project.silentsignals.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bu kontakt hansı istifadəçiyə aiddir
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private User owner;

    // Kontakt olaraq əlavə edilən istifadəçi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    private User contactUser;
}