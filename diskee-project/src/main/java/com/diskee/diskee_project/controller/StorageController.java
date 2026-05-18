package com.diskee.diskee_project.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.service.CurrentUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class StorageController {

    private final CurrentUserService currentUserService;

    @GetMapping("/storage")
    public ResponseEntity<Map<String, Object>> getStorageInfo() {
        DatUserEntity user = currentUserService.getUser();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("storageUsedBytes", user.getStorageUsedBytes());
        result.put("storageLimitBytes", user.getStorageLimitBytes());
        result.put("usagePercentage", user.getStorageLimitBytes() > 0
                ? (user.getStorageUsedBytes() * 100.0 / user.getStorageLimitBytes())
                : 0);
        return ResponseEntity.ok(result);
    }
}