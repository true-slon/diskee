package com.diskee.diskee_project.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
public class UploadedFile {

    @EqualsAndHashCode.Include
    @Schema(description = "Идентификатор загруженного файла")
    private String key;

    @Schema(description = "Имя загруженного файла")
    private String filename;

    @Schema(description = "Оригинальное имя файла")
    private String originalFilename;

    @Schema(description = "Тип файла")
    private String extension;

    @Schema(description = "MIME тип файла")
    private String contentType;

    @Schema(description = "Размер файла (байт)")
    private Long size;

}