package com.diskee.diskee_project.sdk.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_resets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DatUserEntity user;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_used", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean isUsed = false;
}