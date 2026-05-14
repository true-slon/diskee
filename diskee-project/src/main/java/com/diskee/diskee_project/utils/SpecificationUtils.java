package com.diskee.diskee_project.utils;

import jakarta.persistence.criteria.CollectionJoin;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

public final class SpecificationUtils {

    public static <X> Specification<X> distinct(Class<X> entityType) {

        return (root, query, builder) -> {

            query.distinct(true);

            return builder.conjunction();
        };
    }

    public static <X, Y> Specification<X> orderByAsk(SingularAttribute<X, Y> attribute) {

        return (root, query, builder) -> {

            query.orderBy(builder.asc(root.get(attribute)));

            return builder.conjunction();
        };
    }

    public static <X, Y> Specification<X> orderByDesc(SingularAttribute<X, Y> attribute) {

        return (root, query, builder) -> {

            query.orderBy(builder.desc(root.get(attribute)));

            return builder.conjunction();
        };
    }

    public static <X, Y> Specification<X> equals(SingularAttribute<X, Y> attribute, Y value) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return builder.equal(
                    root.get(attribute),
                    value
            );
        };
    }

    @NonNull
    public static <X> Specification<X> idEquals(
            @NonNull SingularAttribute<X, ?> attribute,
            @Nullable UUID id
    ) {
        return (root, query, builder) -> id == null
                ? builder.conjunction()
                : builder.equal(root.get(attribute).get("id"), id);
    }

    public static <X, Y> Specification<X> notEquals(SingularAttribute<X, Y> attribute, Y value) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return builder.notEqual(
                    root.get(attribute),
                    value
            );
        };
    }

    public static <X> Specification<X> equalsIgnoreCase(SingularAttribute<X, String> attribute, String value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }

            return builder.equal(
                    builder.lower(root.get(attribute)),
                    value.toLowerCase()
            );
        };
    }

    public static <X> Specification<X> like(
            SingularAttribute<X, String> attribute,
            String value
    ) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return builder.like(
                    builder.lower(
                            root.get(attribute).as(String.class)
                    ),
                    "%" + value.toLowerCase() + "%"
            );
        };
    }

    public static <X, Y> Specification<X> isNull(SingularAttribute<X, Y> attribute) {

        return (root, query, builder) -> builder.isNull(
                root.get(attribute)
        );
    }

    public static <X, Y> Specification<X> equalsOrNull(SingularAttribute<X, Y> attribute, Y value) {

        return (value != null)
                ? equals(attribute, value)
                : isNull(attribute);
    }

    public static <X, Y> Specification<X> in(SingularAttribute<X, Y> attribute, Collection<Y> value) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return root
                    .get(attribute)
                    .in(value);
        };
    }

    public static <X, Y> Specification<X> in(ListAttribute<X, Y> attribute, Collection<Y> value) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return root
                    .join(attribute, JoinType.LEFT)
                    .in(value);
        };
    }

    public static <X> Specification<X> idIn(Collection<UUID> value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return root.get("id").in(value);
        };
    }

    public static <X> Specification<X> idLongIn(Collection<Long> value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            return root.get("id").in(value);
        };
    }

    public static <X> Specification<X> deleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (deleted) {
                return criteriaBuilder.conjunction();
            }
            return root.get("deletedAt").isNull();
        };
    }

    public static <X> Specification<X> afterOrEquals(
            SingularAttribute<X, ZonedDateTime> attribute,
            ZonedDateTime value
    ) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return builder.greaterThanOrEqualTo(
                    root.get(attribute),
                    value
            );
        };
    }

    public static <X> Specification<X> beforeOrEquals(
            SingularAttribute<X, ZonedDateTime> attribute,
            ZonedDateTime value
    ) {

        return (root, query, builder) -> {

            if (value == null) {
                return builder.conjunction();
            }

            return builder.lessThanOrEqualTo(
                    root.get(attribute),
                    value
            );
        };
    }

    public static <T, E> Specification<E> getSpecOrConj(
            JsonNullable<T> field,
            Function<T, Specification<E>> fieldSpec
    ) {
        return field.isPresent()
                ? fieldSpec.apply(field.get())
                : (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public static <X> Specification<X> likeAny(CollectionAttribute<X, String> attribute, String value) {
        return (root, query, builder) -> {
            if (value == null) {
                return builder.conjunction();
            }
            CollectionJoin<X, String> join = root.join(attribute);
            return builder.like(
                    builder.lower(join),
                    "%" + value.toLowerCase() + "%"
            );
        };
    }
}
