package ru.mirea.firefighter.infrastructure.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.port.ScenarioRepository;

/** In-memory реализация порта каталога сценариев (этап 4). */
public final class InMemoryScenarioRepository implements ScenarioRepository {

    private final Map<ScenarioId, Scenario> storage = new ConcurrentHashMap<>();

    @Override
    public void save(Scenario scenario) {
        storage.put(scenario.id(), scenario);
    }

    @Override
    public Optional<Scenario> findById(ScenarioId id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Scenario> findAll() {
        return List.copyOf(storage.values());
    }
}