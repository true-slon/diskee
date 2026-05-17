package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.LinkFilesFoldersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LinkFilesFoldersRepo extends JpaRepository<LinkFilesFoldersEntity, Long>, JpaSpecificationExecutor<LinkFilesFoldersEntity> {
}
