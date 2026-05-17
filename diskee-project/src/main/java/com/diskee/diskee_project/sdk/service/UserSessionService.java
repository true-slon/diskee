package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.dto.CreateUserSessionDto;
import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.UserSession;
import com.diskee.diskee_project.sdk.data.repo.UserSessionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final UserSessionRepo sessionRepository;

    /**
     * Создать новую сессию с хешем refresh-токена.
     */
    @Transactional
    public UserSession createSession(CreateUserSessionDto dto) {
        String tokenHash = hashToken(dto.getRawRefreshToken());

        UserSession session = UserSession.builder()
                .user(dto.getUser())
                .refreshTokenHash(tokenHash)
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .expiresAt(dto.getExpiresAt())
                .build();

        return sessionRepository.save(session);
    }

    /**
     * Найти сессию по хешу refresh-токена.
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> findByRefreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        return sessionRepository.findByRefreshTokenHash(tokenHash);
    }

    /**
     * Удалить все сессии пользователя (например, при компрометации токена).
     */
    @Transactional
    public void deleteAllSessionsForUser(DatUserEntity user) {
        sessionRepository.deleteAllByUser(user);
    }

    /**
     * Хеширование refresh-токена (SHA-256 + Base64).
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    public void deleteSession(UUID sessionId) {
        sessionRepository.deleteById(sessionId);
    }
}