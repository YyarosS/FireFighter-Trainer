package ru.mirea.firefighter.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.EvaluationRepository;

/** In-memory реализация порта оценок. Ключ — идентификатор тренировки (связь 1:1). */
public final class InMemoryEvaluationRepository implements EvaluationRepository {

    private final Map<TrainingSessionId, Evaluation> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Evaluation evaluation) {
        storage.put(evaluation.sessionId(), evaluation);
    }

    @Override
    public Optional<Evaluation> findBySessionId(TrainingSessionId sessionId) {
        return Optional.ofNullable(storage.get(sessionId));
    }

    @Override
    public List<Evaluation> findBySessionIds(java.util.Collection<TrainingSessionId> sessionIds) {
        return sessionIds.stream()
                .map(storage::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }
}