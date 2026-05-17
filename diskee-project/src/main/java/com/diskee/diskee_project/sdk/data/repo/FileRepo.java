package com.diskee.diskee_project.sdk.data.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.diskee.diskee_project.sdk.data.FileEntity;

public interface FileRepo extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {
    
    List<FileEntity> findByUserIdAndParentFolderIsNullAndIsDeletedFalse(Long userId);
    
    List<FileEntity> findByParentFolderIdAndIsDeletedFalse(Long parentFolderId);
    List<FileEntity> findAllByParentFolderIdAndIsDeletedFalse(Long parentFolderId);

    List<FileEntity> findAllByParentFolderId(Long parentFolderId);

    List<FileEntity> findAllByParentFolderIsNullAndIsDeletedFalse();

}