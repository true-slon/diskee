package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.auth.CurrentUser;
import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final DatUserRepo userRepository;

    public DatUserEntity getUser() {
        return getUserOpt()
                .orElseThrow(() -> new RuntimeException("Current user not found in database"));
    }

    public Optional<DatUserEntity> getUserOpt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return Optional.empty();

        if (auth.getPrincipal() instanceof Jwt jwt) {
            String userId = jwt.getSubject();
            return userRepository.findById(Long.parseLong(userId));
        }

        if (auth.getPrincipal() instanceof CurrentUser currentUser) {
            return userRepository.findById(currentUser.getUserId());
        }

        return Optional.empty();
    }

    public boolean verifyPassword(String rawPassword) {
        return true;
    }
}