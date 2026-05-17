package com.diskee.diskee_project.sdk.data.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.diskee.diskee_project.sdk.data.TrashBinEntity;

public interface TrashBinRepo extends JpaRepository<TrashBinEntity, Long>, JpaSpecificationExecutor<TrashBinEntity> {
    List<TrashBinEntity> findAllByUserId(Long userId);
    List<TrashBinEntity> findAllByAutoDeleteAtBefore(Instant date);
}