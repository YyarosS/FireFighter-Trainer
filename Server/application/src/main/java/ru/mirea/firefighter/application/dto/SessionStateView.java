package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.TrainingSession;

/**
 * Текущее состояние сеанса — то, что VR-клиент периодически запрашивает
 * у сервера (архитектура «сервер-авторитет»).
 */
public record SessionStateView(
        String sessionId,
        String traineeId,
        String scenarioId,
        String status,
        double elapsedSeconds,
        long lastCommandSequence,
        EnvironmentView environment,
        String startedAt,
        String finishedAt) {

    public static SessionStateView from(TrainingSession session) {
        return new SessionStateView(
                session.id().value(),
                session.traineeId().value(),
                session.scenarioId().value(),
                session.status().name(),
                session.elapsedSeconds(),
                session.lastCommandSequence(),
                EnvironmentView.from(session.currentEnvironment()),
                session.startedAt().toString(),
                session.finishedAt().map(Object::toString).orElse(null));
    }
}