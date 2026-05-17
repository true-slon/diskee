package com.diskee.diskee_project.sdk.data.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.diskee.diskee_project.sdk.data.FolderEntity;

public interface FolderRepo extends JpaRepository<FolderEntity, Long>, JpaSpecificationExecutor<FolderEntity> {
    
    List<FolderEntity> findByUserIdAndParentFolderIsNullAndDeletedAtIsNull(Long userId);
    
    List<FolderEntity> findByParentFolderIdAndDeletedAtIsNull(Long parentFolderId);
    List<FolderEntity> findAllByParentFolderIdAndDeletedAtIsNull(Long parentFolderId);

    List<FolderEntity> findAllByParentFolderId(Long parentFolderId);

    List<FolderEntity> findAllByParentFolderIsNullAndDeletedAtIsNull();

    List<FolderEntity> findByParentFolderIdAndUserIdAndDeletedAtIsNull(Long parentFolderId, Long userId);

}