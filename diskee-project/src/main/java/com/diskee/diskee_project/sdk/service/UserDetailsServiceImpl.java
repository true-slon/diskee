package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.api.auth.CurrentUser;
import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DatUserRepo userRepo;

    @NotNull
    @Override
    public UserDetails loadUserByUsername(@NotNull String email) throws UsernameNotFoundException {
        DatUserEntity user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        return new CurrentUser(user);
    }
}