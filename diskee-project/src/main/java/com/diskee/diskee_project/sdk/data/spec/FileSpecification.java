package com.diskee.diskee_project.sdk.data.spec;

import com.diskee.diskee_project.sdk.data.FileEntity;
import com.diskee.diskee_project.utils.SpecificationUtils;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class FileSpecification {
    public static Specification<FileEntity> idIn(Collection<Long> ids) {
        return SpecificationUtils.idLongIn(ids);
    }

    public static Specification<FileEntity> deleted(boolean deleted) {
        return SpecificationUtils.deleted(deleted);
    }
}