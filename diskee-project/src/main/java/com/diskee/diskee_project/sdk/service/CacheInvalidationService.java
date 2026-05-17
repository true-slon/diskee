package com.diskee.diskee_project.sdk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheInvalidationService {

    private final CacheManager cacheManager;

    // Имя кеша, используемое в аннотациях
    private static final String FOLDER_CONTENTS_CACHE = "folder_contents";
    private static final String FILE_METADATA_CACHE = "file_metadata";
    private static final String ROOT_KEY_SUFFIX = "root";

    /** Сбросить кеш содержимого конкретной родительской папки */
    public void evictFolderContents(Long parentFolderId) {
        String key = buildFolderContentsKey(parentFolderId);
        evictAfterCommit(FOLDER_CONTENTS_CACHE, key);
    }

    /** Сбросить кеш корневого списка папок/файлов (parentId == null) */
    public void evictFolderContentsRoot() {
        evictFolderContents(null);
    }

    /** Сбросить кеш метаданных файла по идентификатору */
    public void evictFileMetadata(Long fileId) {
        evictAfterCommit(FILE_METADATA_CACHE, fileId.toString());
    }

    /** Сбросить метаданные нескольких файлов */
    public void evictFileMetadata(Collection<Long> fileIds) {
        fileIds.forEach(this::evictFileMetadata);
    }

    /** Пакетный сброс кеша содержимого для нескольких родительских папок */
    public void evictFolderContentsMultiple(Collection<Long> parentFolderIds) {
        parentFolderIds.forEach(this::evictFolderContents);
    }

    private String buildFolderContentsKey(Long parentFolderId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String parentPart = (parentFolderId != null) ? parentFolderId.toString() : ROOT_KEY_SUFFIX;
        return username + "-" + parentPart;
    }


    private void evictAfterCommit(String cacheName, String key) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    doEvict(cacheName, key);
                }
            });
        } else {
            // Если транзакции нет – удаляем сразу
            doEvict(cacheName, key);
        }
    }

    private void doEvict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            log.warn("EVICTING cache={} key={}", cacheName, key);   // добавьте
            cache.evict(key);
        } else {
            log.error("Cache {} not found!", cacheName);            // добавьте
        }
    }
}