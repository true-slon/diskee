package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.PasswordReset;
import com.diskee.diskee_project.sdk.data.TrashBinEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TrashBinRepo extends JpaRepository<TrashBinEntity, UUID>, JpaSpecificationExecutor<TrashBinEntity> {
}
