package com.diskee.diskee_project.sdk.service;

import com.diskee.diskee_project.sdk.data.DatUserEntity;
import com.diskee.diskee_project.sdk.data.repo.DatUserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final DatUserRepo userRepository;

    @Transactional
    public DatUserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    /**
     * Опционально: поиск по email (может пригодиться для проверок).
     */
    @Transactional
    public Optional<DatUserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Опционально: сохранение пользователя.
     */
    @Transactional
    public DatUserEntity save(DatUserEntity user) {
        return userRepository.save(user);
    }
}
