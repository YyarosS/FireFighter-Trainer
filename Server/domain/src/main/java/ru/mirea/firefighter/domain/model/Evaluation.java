package ru.mirea.firefighter.domain.model;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.Rating;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;

/**
 * Оценка тренировки (сущность «Оценка», п. 2.4 отчёта): три рейтинга,
 * использованные веса и момент расчёта.
 *
 * <p>Связь с тренировкой — 1:1, поэтому идентификатором оценки служит
 * идентификатор тренировки. Оценка неизменяема: пересчёт создаёт новую
 * запись, что сохраняет историю решений.</p>
 */
public final class Evaluation {

    private final TrainingSessionId sessionId;
    private final Rating speed;
    private final Rating accuracy;
    private final Rating combined;
    private final RatingWeights weights;
    private final double elapsedSeconds;
    private final Instant computedAt;

    public Evaluation(TrainingSessionId sessionId,
                      Rating speed,
                      Rating accuracy,
                      Rating combined,
                      RatingWeights weights,
                      double elapsedSeconds,
                      Instant computedAt) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
        this.speed = Objects.requireNonNull(speed, "speed");
        this.accuracy = Objects.requireNonNull(accuracy, "accuracy");
        this.combined = Objects.requireNonNull(combined, "combined");
        this.weights = Objects.requireNonNull(weights, "weights");
        this.computedAt = Objects.requireNonNull(computedAt, "computedAt");
        if (Double.isNaN(elapsedSeconds) || elapsedSeconds < 0.0) {
            throw new IllegalArgumentException("Длительность не может быть отрицательной");
        }
        this.elapsedSeconds = elapsedSeconds;
    }

    /** Качественная оценка по комбинированному рейтингу. */
    public Grade grade() {
        return Grade.forRating(combined);
    }

    public boolean isExcellent() {
        return grade() == Grade.EXCELLENT;
    }

    public TrainingSessionId sessionId() {
        return sessionId;
    }

    public Rating speed() {
        return speed;
    }

    public Rating accuracy() {
        return accuracy;
    }

    public Rating combined() {
        return combined;
    }

    public RatingWeights weights() {
        return weights;
    }

    public double elapsedSeconds() {
        return elapsedSeconds;
    }

    public Instant computedAt() {
        return computedAt;
    }

    /** Качественная шкала итогового рейтинга. */
    public enum Grade {

        EXCELLENT("отлично", 90.0),
        GOOD("хорошо", 75.0),
        SATISFACTORY("удовлетворительно", 60.0),
        UNSATISFACTORY("неудовлетворительно", 0.0);

        private final String label;
        private final double minCombined;

        Grade(String label, double minCombined) {
            this.label = label;
            this.minCombined = minCombined;
        }

        public static Grade forRating(Rating rating) {
            for (Grade grade : values()) {
                if (rating.isAtLeast(grade.minCombined)) {
                    return grade;
                }
            }
            return UNSATISFACTORY;
        }

        public String label() {
            return label;
        }
    }
}