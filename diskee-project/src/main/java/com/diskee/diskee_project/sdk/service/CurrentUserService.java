// package com.diskee.diskee_project.sdk.service;

// import com.diskee.diskee_project.api.auth.CurrentUser;
// import com.diskee.diskee_project.sdk.data.DatUserEntity;
// import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import java.util.Optional;

// import org.springframework.security.core.context.SecurityContextHolder;

// @Service
// @RequiredArgsConstructor
// public class CurrentUserService {

//     private final DatUserRepo userRepository;
//     private final PasswordEncoder passwordEncoder;

//     /**
//      * Возвращает полную сущность текущего аутентифицированного пользователя
//      */
//     public DatUserEntity getUser() {
//         return getUserOpt()
//                 .orElseThrow(() -> new RuntimeException("Current user not found in database"));
//     }

//     public Optional<DatUserEntity> getUserOpt() {
//         Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//         if (principal instanceof CurrentUser currentUser) {
//             return userRepository.findById(currentUser.getUserId());
//         }
//         return Optional.empty();
//     }

//     /**
//      * Проверка пароля текущего пользователя
//      */
//     public boolean verifyPassword(String rawPassword) {
//         DatUserEntity user = getUser();
//         return passwordEncoder.matches(rawPassword, user.getPasswordHash());
//     }
// }

package com.diskee.diskee_project.sdk.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final DatUserRepo userRepository;

    public DatUserEntity getUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No users in database. Add user first."));
    }

    public Optional<DatUserEntity> getUserOpt() {
        return userRepository.findAll().stream().findFirst();
    }

    public boolean verifyPassword(String rawPassword) {
        return true;
    }
}