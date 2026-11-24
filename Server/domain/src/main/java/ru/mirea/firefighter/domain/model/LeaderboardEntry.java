package ru.mirea.firefighter.domain.model;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Строка таблицы лидеров: снимок результата тренировки, подготовленный
 * для алгоритма 4 (п. 2.5 отчёта).
 */
public record LeaderboardEntry(
        String traineeName,
        String scenarioTitle,
        Rating speed,
        Rating accuracy,
        Rating combined,
        double elapsedSeconds,
        Instant finishedAt) {

    public LeaderboardEntry {
        Objects.requireNonNull(traineeName, "traineeName");
        Objects.requireNonNull(scenarioTitle, "scenarioTitle");
        Objects.requireNonNull(speed, "speed");
        Objects.requireNonNull(accuracy, "accuracy");
        Objects.requireNonNull(combined, "combined");
        Objects.requireNonNull(finishedAt, "finishedAt");
        if (elapsedSeconds < 0.0) {
            throw new IllegalArgumentException("Длительность не может быть отрицательной");
        }
    }
}