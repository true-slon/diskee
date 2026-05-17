package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.PasswordReset;
import com.diskee.diskee_project.sdk.data.SharedLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SharedLinkRepo extends JpaRepository<SharedLinkEntity, UUID>, JpaSpecificationExecutor<SharedLinkEntity> {
    Optional<SharedLinkEntity> findByToken(String token);
    List<SharedLinkEntity> findAllByFileUserOrFolderUser(DatUserEntity fileUser, DatUserEntity folderUser);
}
