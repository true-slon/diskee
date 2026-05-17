package com.diskee.diskee_project.utils;

import lombok.SneakyThrows;
import org.hibernate.mapping.SoftDeletable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EntityUtils {
    @SneakyThrows
    public static <E extends BaseEntity & FlagDeletable> void checkInconsistency(
            @NonNull Long id,
            @Nullable E entity,
            @NonNull Function<Long, Exception> exceptionFactory,
            @NonNull boolean deleted
    ) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(exceptionFactory);

        if ((entity != null)
                && (Objects.equals(entity.getId(), id))) {
            entity.isDeleted();
            if (deleted) {
                return;
            }
        }

        throw exceptionFactory.apply(id);
    }

    @SneakyThrows
    public static <E extends  BaseEntity & FlagDeletable> void checkInconsistency(
            @NonNull Collection<Long> ids,
            @NonNull Collection<E> entities,
            @NonNull Function<Long, Exception> exceptionFactory,
            @NonNull boolean deleted
    ) {
        Objects.requireNonNull(ids);
        Objects.requireNonNull(entities);
        Objects.requireNonNull(exceptionFactory);

        Set<Long> idsSet = ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> entityIdsSet = entities.stream()
                .filter(Objects::nonNull)
                .filter(ent -> ent.isDeleted() || deleted)
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        idsSet.removeAll(entityIdsSet);

        if (idsSet.isEmpty()) {
            return;
        }
        throw exceptionFactory.apply(idsSet.iterator().next());
    }
}
