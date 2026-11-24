package ru.mirea.firefighter.domain.model.vo;

import java.util.Objects;

/** Идентификатор стажёра. Отдельный тип исключает путаницу с другими идентификаторами. */
public record TraineeId(String value) {

    public TraineeId {
        Objects.requireNonNull(value, "Идентификатор стажёра не может быть null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор стажёра не может быть пустым");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}