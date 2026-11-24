package ru.mirea.firefighter.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.port.TraineeRepository;

/**
 * In-memory реализация порта стажёров (этап 4).
 *
 * <p>На этапе 5 заменяется реализацией на PostgreSQL/JPA: домен и
 * приложение об этом не знают, меняется только бин в конфигурации.</p>
 */
public final class InMemoryTraineeRepository implements TraineeRepository {

    private final Map<TraineeId, Trainee> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Trainee trainee) {
        storage.put(trainee.id(), trainee);
    }

    @Override
    public Optional<Trainee> findById(TraineeId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public boolean existsById(TraineeId id) {
        return storage.containsKey(id);
    }
}