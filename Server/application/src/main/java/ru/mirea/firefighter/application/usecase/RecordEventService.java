package ru.mirea.firefighter.application.usecase;

import java.util.List;
import java.util.Objects;
import ru.mirea.firefighter.application.dto.EventInput;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Принять события тренировки от VR-клиента».
 *
 * <p>Принимается пакет событий: клиент буферизует телеметрию и отправляет
 * её пачками, что снижает накладные расходы сети. Вес нарушения, если
 * клиент его не передал, берётся из типа события на сервере.</p>
 */
public final class RecordEventService {

    private final TrainingSessionRepository sessions;

    public RecordEventService(TrainingSessionRepository sessions) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
    }

    public void record(String sessionId, List<EventInput> events) {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("Пакет событий не может быть пустым");
        }
        TrainingSession session = EntityLookup.session(sessions, sessionId);
        for (EventInput event : events) {
            session.recordEvent(
                    event.type(),
                    event.targetObject(),
                    event.atSeconds(),
                    event.effectiveWeight());
        }
        sessions.save(session);
    }
}