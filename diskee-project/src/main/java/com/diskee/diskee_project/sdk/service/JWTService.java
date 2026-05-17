package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.auth.CurrentUser;
import com.diskee.diskee_project.api.request.LoginRequest;
import com.diskee.diskee_project.api.response.AuthTokenResponse;
import com.diskee.diskee_project.dto.CreateUserSessionDto;
import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.UserSession;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTService {

    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;
    private final UserService userService;

    private static final TemporalAmount ACCESS_TOKEN_LIFETIME = Duration.ofHours(2);
    private static final TemporalAmount REMEMBER_ME_TOKEN_LIFETIME = Duration.ofDays(30);

    @Value("${jwt.secret}")
    private String SECRET;

    public AuthTokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication authentication = authenticationManager.authenticate(authToken);
        CurrentUser currentUser = (CurrentUser) authentication.getPrincipal();
        DatUserEntity user = currentUser.getUser();

        String accessToken = generateAccessToken(user.getId());

        String refreshToken = null;
        UUID sessionId = null;
        if (Boolean.TRUE.equals(request.getRememberMe())) {
            Pair<UUID, String> pair = generateRememberMeToken(user, ipAddress, userAgent);
            sessionId = pair.getFirst();
            refreshToken = pair.getSecond();
        }

        return AuthTokenResponse.builder()
                .withAccessToken(accessToken)
                .withRefreshToken(refreshToken)
                .withSessionId(sessionId)
                .build();
    }

    public AuthTokenResponse refreshAccessToken(String rawRefreshToken, String ipAddress, String userAgent) {
        JWTClaimsSet claims = parseTokenClaims(rawRefreshToken);
        Long userId = Long.valueOf(claims.getSubject());

        UserSession existingSession = userSessionService.findByRefreshToken(rawRefreshToken).orElse(null);
        DatUserEntity user = userService.findById(userId);

        if (existingSession == null) {
            userSessionService.deleteAllSessionsForUser(user);
            throw new RuntimeException("Refresh token reuse detected. All sessions have been invalidated.");
        }

        if (existingSession.getExpiresAt().isBefore(Instant.now())) {
            userSessionService.deleteSession(existingSession.getSessionId());
            throw new RuntimeException("Refresh token expired.");
        }

        userSessionService.deleteSession(existingSession.getSessionId());

        Pair<UUID, String> newPair = generateRememberMeToken(user, ipAddress, userAgent);
        String newAccessToken = generateAccessToken(user.getId());

        return AuthTokenResponse.builder()
                .withAccessToken(newAccessToken)
                .withRefreshToken(newPair.getSecond())
                .withSessionId(newPair.getFirst())
                .build();
    }

    public void logout(String rawRefreshToken) {
        userSessionService.findByRefreshToken(rawRefreshToken)
                .ifPresent(session -> userSessionService.deleteSession(session.getSessionId()));
    }

    public void logoutAll(Long userId) {
        DatUserEntity user = userService.findById(userId);
        userSessionService.deleteAllSessionsForUser(user);
    }

    @SneakyThrows
    private Pair<UUID, String> generateRememberMeToken(DatUserEntity user, String ipAddress, String userAgent) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(REMEMBER_ME_TOKEN_LIFETIME);
        String token = buildJwt(String.valueOf(user.getId()), now, expiresAt);

        CreateUserSessionDto dto = CreateUserSessionDto.builder()
                .user(user)
                .rawRefreshToken(token)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiresAt(expiresAt)
                .build();

        UserSession session = userSessionService.createSession(dto);
        return Pair.of(session.getSessionId(), token);
    }

    @SneakyThrows
    private String generateAccessToken(Long userId) {
        Instant now = Instant.now();
        return buildJwt(String.valueOf(userId), now, now.plus(ACCESS_TOKEN_LIFETIME));
    }

    @SneakyThrows
    private String buildJwt(String subject, Instant issuedAt, Instant expiresAt) {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(UUID.randomUUID().toString())
                .subject(subject)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(SECRET.getBytes()));
        return jwt.serialize();
    }

    @SneakyThrows
    private JWTClaimsSet parseTokenClaims(String token) {
        SignedJWT jwt = SignedJWT.parse(token);
        if (!jwt.verify(new MACVerifier(SECRET.getBytes()))) {
            throw new RuntimeException("Invalid JWT signature");
        }
        return jwt.getJWTClaimsSet();
    }
}