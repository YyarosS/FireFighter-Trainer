package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.Evaluation;

/** Итог тренировки: три рейтинга, длительность и качественная оценка. */
public record SessionResult(
        String sessionId,
        double speed,
        double accuracy,
        double combined,
        double elapsedSeconds,
        String grade,
        String gradeLabel,
        String finishedAt) {

    public static SessionResult from(Evaluation evaluation) {
        return new SessionResult(
                evaluation.sessionId().value(),
                evaluation.speed().value(),
                evaluation.accuracy().value(),
                evaluation.combined().value(),
                evaluation.elapsedSeconds(),
                evaluation.grade().name(),
                evaluation.grade().label(),
                evaluation.computedAt().toString());
    }
}