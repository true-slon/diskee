package com.diskee.diskee_project.api.response;

import com.diskee.diskee_project.dto.FileDTOs;
import com.diskee.diskee_project.dto.FolderDTOs;
import lombok.Data;

@Data
public class SharedFolderContentsResponse {
    private Long folderId;
    private String folderName;
    private String fullPath;
    private String permission;
    private java.util.List<FileDTOs.FileResponse> files;
    private java.util.List<FolderDTOs.FolderResponse> subFolders;
}
