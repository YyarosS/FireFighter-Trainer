package ru.mirea.firefighter.application.usecase;

import java.util.Objects;
import ru.mirea.firefighter.application.dto.EnvironmentView;
import ru.mirea.firefighter.application.dto.SessionStateView;
import ru.mirea.firefighter.application.port.Clock;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Изменить параметры среды в ходе тренировки».
 *
 * <p>Точка приложения «сервера-авторитета»: инструктор или модель
 * динамики пожара меняет условия, сервер фиксирует изменение командой,
 * клиент только отображает новое состояние.</p>
 */
public final class UpdateEnvironmentService {

    private final TrainingSessionRepository sessions;
    private final Clock clock;

    public UpdateEnvironmentService(TrainingSessionRepository sessions, Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SessionStateView update(String sessionId, EnvironmentView environment) {
        TrainingSession session = EntityLookup.session(sessions, sessionId);
        session.issueEnvironmentCommand(clock.now(), environment.toDomain());
        sessions.save(session);
        return SessionStateView.from(session);
    }
}