package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DatUserRepo extends JpaRepository<DatUserEntity, Long>, JpaSpecificationExecutor<DatUserEntity> {
    Optional<DatUserEntity> findByEmail(String email);
}
