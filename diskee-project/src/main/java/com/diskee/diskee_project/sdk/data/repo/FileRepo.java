package com.diskee.diskee_project.sdk.data.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.diskee.diskee_project.sdk.data.FileEntity;

public interface FileRepo extends JpaRepository<FileEntity, Long>, JpaSpecificationExecutor<FileEntity> {
    
    List<FileEntity> findByUserIdAndParentFolderIsNullAndIsDeletedFalse(Long userId);
    
    List<FileEntity> findByParentFolderIdAndIsDeletedFalse(Long parentFolderId);
    List<FileEntity> findAllByParentFolderIdAndIsDeletedFalse(Long parentFolderId);

    List<FileEntity> findAllByParentFolderId(Long parentFolderId);

    List<FileEntity> findAllByParentFolderIsNullAndIsDeletedFalse();
    List<FileEntity> findByParentFolderIdAndUserIdAndIsDeletedFalse(Long parentFolderId, Long userId);

    @Query(value = "SELECT f.* FROM diskee.files f JOIN diskee.file_search_vectors sv ON f.id = sv.file_id WHERE f.user_id = :userId AND f.is_deleted = false AND sv.search_vector @@ plainto_tsquery('russian', :query)", nativeQuery = true)
    List<FileEntity> searchByName(@Param("userId") Long userId, @Param("query") String query);

    @Query(value = "SELECT f.* FROM diskee.files f JOIN diskee.file_search_vectors sv ON f.id = sv.file_id WHERE f.user_id = :userId AND f.parent_folder_id = :folderId AND f.is_deleted = false AND sv.search_vector @@ plainto_tsquery('russian', :query)", nativeQuery = true)
    List<FileEntity> searchInFolder(@Param("userId") Long userId, @Param("folderId") Long folderId, @Param("query") String query);

    @Query(value = "SELECT f.* FROM diskee.files f JOIN diskee.file_search_vectors sv ON f.id = sv.file_id WHERE f.user_id = :userId AND f.is_deleted = false AND f.file_extension IN :extensions AND sv.search_vector @@ plainto_tsquery('russian', :query)", nativeQuery = true)
    List<FileEntity> searchByCategory(@Param("userId") Long userId, @Param("query") String query, @Param("extensions") List<String> extensions);

    @Query(value = "SELECT f.* FROM diskee.files f JOIN diskee.file_search_vectors sv ON f.id = sv.file_id WHERE f.user_id = :userId AND f.parent_folder_id = :folderId AND f.is_deleted = false AND f.file_extension IN :extensions AND sv.search_vector @@ plainto_tsquery('russian', :query)", nativeQuery = true)
    List<FileEntity> searchInFolderByCategory(@Param("userId") Long userId, @Param("folderId") Long folderId, @Param("query") String query, @Param("extensions") List<String> extensions);

    @Query("SELECT f FROM FileEntity f WHERE f.user.id = :userId AND f.isDeleted = false AND f.fileExtension IN :extensions")
    List<FileEntity> findAllByCategory(@Param("userId") Long userId, @Param("extensions") List<String> extensions);

    @Query("SELECT f FROM FileEntity f WHERE f.user.id = :userId AND f.parentFolder.id = :folderId AND f.isDeleted = false AND f.fileExtension IN :extensions")
    List<FileEntity> findAllByCategoryInFolder(@Param("userId") Long userId, @Param("folderId") Long folderId, @Param("extensions") List<String> extensions);
}