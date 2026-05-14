package com.diskee.diskee_project.sdk.data.repo;

import com.diskee.diskee_project.sdk.data.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PasswordResetRepo extends JpaRepository<PasswordReset, UUID>, JpaSpecificationExecutor<PasswordReset> {
}
