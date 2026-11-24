package ru.mirea.firefighter.domain.port;

import java.util.List;
import java.util.Optional;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;

/** Порт хранения каталога сценариев. */
public interface ScenarioRepository {

    void save(Scenario scenario);

    Optional<Scenario> findById(ScenarioId id);

    List<Scenario> findAll();
}