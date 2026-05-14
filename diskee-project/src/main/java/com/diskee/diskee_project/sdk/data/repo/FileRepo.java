package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FileRepo extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {
}
