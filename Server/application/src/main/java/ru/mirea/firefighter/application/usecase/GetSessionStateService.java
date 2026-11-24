package ru.mirea.firefighter.application.usecase;

import java.util.Objects;
import ru.mirea.firefighter.application.dto.SessionStateView;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/** Сценарий использования «Получить текущее состояние сеанса». */
public final class GetSessionStateService {

    private final TrainingSessionRepository sessions;

    public GetSessionStateService(TrainingSessionRepository sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public SessionStateView state(String sessionId) {
        return SessionStateView.from(EntityLookup.session(sessions, sessionId));
    }
}