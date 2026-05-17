package com.diskee.diskee_project.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class TrashItemDTO {
    private Long id;
    private String itemType;
    private String name;
    private String originalPath;
    private Long size;
    private Instant deletedAt;
    private Instant autoDeleteAt;
}