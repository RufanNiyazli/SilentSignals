
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User contactUser;
}