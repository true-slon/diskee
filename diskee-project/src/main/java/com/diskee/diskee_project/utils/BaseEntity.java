package com.diskee.diskee_project.utils;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Persistable;

import java.util.Objects;

@MappedSuperclass
@Getter
@Setter
@ToString
public abstract class BaseEntity implements Persistable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Возвращает true, если entity ещё не записан в БД (not persisted yet).
     */
    @Override
    public boolean isNew() {
        return id == null;
    }

    /**
     * Сравнение entity по значению ID.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false;
        BaseEntity that = (BaseEntity) other;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * Hash code постоянный, основан на классе (т.к. ID может быть null для новых объектов).
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}