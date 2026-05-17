package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.request.RegisterRequest;
import com.diskee.diskee_project.api.response.RegisterResponse;
import com.diskee.diskee_project.api.exception.UserAlreadyExistsProblem;
import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final DatUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsProblem(request.getEmail());
        }

        DatUserEntity user = DatUserEntity.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .build();

        DatUserEntity saved = userRepo.save(user);

        return RegisterResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .displayName(saved.getDisplayName())
                .storageLimitBytes(saved.getStorageLimitBytes())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}