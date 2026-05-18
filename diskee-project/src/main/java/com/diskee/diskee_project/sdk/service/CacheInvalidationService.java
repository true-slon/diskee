package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.sdk.data.FolderEntity;
import com.diskee.diskee_project.sdk.data.repo.FileRepo;
import com.diskee.diskee_project.sdk.data.repo.FolderRepo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final CacheManager cacheManager;

    private final FolderRepo folderRepo;
    private final FileRepo fileRepo;

    private static final String FOLDER_CONTENTS_CACHE = "folder_contents";
    private static final String FILE_METADATA_CACHE = "file_metadata";

    private static final String ROOT_KEY_SUFFIX = "root";

    public void evictFolderContents(Long parentFolderId) {
        String key = buildFolderContentsKey(parentFolderId);
        evictAfterCommit(FOLDER_CONTENTS_CACHE, key);
    }

    public void evictFolderContentsRoot() {
        evictFolderContents(null);
    }

    public void evictFileMetadata(Long fileId) {
        evictAfterCommit(FILE_METADATA_CACHE, fileId.toString());
    }

    public void evictFileMetadata(Collection<Long> fileIds) {
        fileIds.stream()
                .filter(id -> id != null)
                .forEach(this::evictFileMetadata);
    }

    public void evictFolderContentsMultiple(Collection<Long> folderIds) {
        folderIds.forEach(this::evictFolderContents);
    }

    /**
     * Полная инвалидация дерева папки:
     *
     * - самой папки
     * - родительской папки
     * - всех подпапок
     * - файлов внутри
     */
    public void invalidateFolderTree(FolderEntity folder) {

        CacheInvalidationContext ctx =
                new CacheInvalidationContext();

        collectFolderTree(folder, ctx);

        log.warn(
                "INVALIDATING TREE folders={} parents={} files={}",
                ctx.getFolderIds(),
                ctx.getParentFolderIds(),
                ctx.getFileIds()
        );

        evictFolderContentsMultiple(ctx.getFolderIds());

        evictFolderContentsMultiple(ctx.getParentFolderIds());

        evictFileMetadata(ctx.getFileIds());
    }

    /**
     * Рекурсивный сбор id для инвалидации
     */
    private void collectFolderTree(
            FolderEntity folder,
            CacheInvalidationContext ctx
    ) {

        if (folder == null) {
            return;
        }

        // сама папка
        ctx.getFolderIds().add(folder.getId());

        // родитель
        Long parentId =
                folder.getParentFolder() != null
                        ? folder.getParentFolder().getId()
                        : null;

        ctx.getParentFolderIds().add(parentId);

        // файлы текущей папки
        List<FileEntity> files =
                fileRepo.findAllByParentFolderId(folder.getId());

        for (FileEntity file : files) {
            ctx.getFileIds().add(file.getId());
        }

        // подпапки
        List<FolderEntity> subFolders =
                folderRepo.findAllByParentFolderId(folder.getId());

        for (FolderEntity subFolder : subFolders) {
            collectFolderTree(subFolder, ctx);
        }
    }

    private String buildFolderContentsKey(Long parentFolderId) {

        String username =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        String parentPart =
                parentFolderId != null
                        ? parentFolderId.toString()
                        : ROOT_KEY_SUFFIX;

        return username + "-" + parentPart;
    }

    private void evictAfterCommit(
            String cacheName,
            String key
    ) {

        if (TransactionSynchronizationManager.isActualTransactionActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            doEvict(cacheName, key);
                        }
                    }
            );

        } else {

            doEvict(cacheName, key);
        }
    }

    private void doEvict(
            String cacheName,
            String key
    ) {

        Cache cache = cacheManager.getCache(cacheName);

        if (cache != null) {

            log.warn(
                    "EVICTING cache={} key={}",
                    cacheName,
                    key
            );

            cache.evict(key);

        } else {

            log.error(
                    "Cache {} not found!",
                    cacheName
            );
        }
    }

    @Getter
    public static class CacheInvalidationContext {

        private final Set<Long> folderIds =
                new HashSet<>();

        private final Set<Long> parentFolderIds =
                new HashSet<>();

        private final Set<Long> fileIds =
                new HashSet<>();
    }
}