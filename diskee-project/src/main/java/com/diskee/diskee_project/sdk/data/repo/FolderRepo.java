package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.FolderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FolderRepo extends JpaRepository<FolderEntity, UUID>, JpaSpecificationExecutor<FolderEntity> {
}
