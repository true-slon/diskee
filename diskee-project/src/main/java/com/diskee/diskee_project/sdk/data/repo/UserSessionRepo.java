package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepo extends JpaRepository<UserSession, UUID>, JpaSpecificationExecutor<UserSession> {
    Optional<UserSession> findByRefreshTokenHash(String refreshTokenHash);

    List<UserSession> findAllByUser(DatUserEntity user);

    void deleteAllByUser(DatUserEntity user);
}
