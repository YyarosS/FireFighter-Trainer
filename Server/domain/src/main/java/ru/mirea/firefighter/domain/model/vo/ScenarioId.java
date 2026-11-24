package ru.mirea.firefighter.domain.model.vo;

import java.util.Objects;

/** Идентификатор сценария тренировки. */
public record ScenarioId(String value) {

    public ScenarioId {
        Objects.requireNonNull(value, "Идентификатор сценария не может быть null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор сценария не может быть пустым");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}