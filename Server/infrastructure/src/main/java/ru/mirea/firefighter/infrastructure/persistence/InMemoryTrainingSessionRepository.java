package ru.mirea.firefighter.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * In-memory реализация порта тренировок (этап 4).
 *
 * <p>Хранит ссылки на агрегаты, поэтому вызывающий код обязан сохранять
 * агрегат после изменения ({@code save}). В реализации на JPA это
 * заменится на управление состоянием сущностей в транзакции.</p>
 */
public final class InMemoryTrainingSessionRepository implements TrainingSessionRepository {

    private final Map<TrainingSessionId, TrainingSession> storage = new ConcurrentHashMap<>();

    @Override
    public void save(TrainingSession session) {
        storage.put(session.id(), session);
    }

    @Override
    public Optional<TrainingSession> findById(TrainingSessionId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<TrainingSession> findByTraineeId(TraineeId traineeId) {
        return storage.values().stream()
                .filter(session -> session.traineeId().equals(traineeId))
                .toList();
    }

    @Override
    public List<TrainingSession> findByScenarioId(ScenarioId scenarioId) {
        return storage.values().stream()
                .filter(session -> session.scenarioId().equals(scenarioId))
                .toList();
    }
}