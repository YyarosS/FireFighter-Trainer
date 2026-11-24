package ru.mirea.firefighter.domain.model.vo;

import java.util.Objects;

/** Идентификатор сеанса тренировки (агрегат «Тренировка»). */
public record TrainingSessionId(String value) {

    public TrainingSessionId {
        Objects.requireNonNull(value, "Идентификатор тренировки не может быть null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор тренировки не может быть пустым");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}