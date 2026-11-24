package ru.mirea.firefighter.application.usecase;

import java.util.Objects;
import ru.mirea.firefighter.application.dto.EnvironmentView;
import ru.mirea.firefighter.application.dto.ScenarioView;
import ru.mirea.firefighter.application.port.IdGenerator;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;
import ru.mirea.firefighter.domain.port.ScenarioRepository;

/** Сценарий использования «Добавить сценарий в каталог». */
public final class CreateScenarioService {

    private final ScenarioRepository scenarios;
    private final IdGenerator ids;

    public CreateScenarioService(ScenarioRepository scenarios, IdGenerator ids) {
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
        this.ids = Objects.requireNonNull(ids, "ids");
    }

    public ScenarioView create(String title, String description,
                               EnvironmentView environment, double timeLimitSeconds) {
        Scenario scenario = Scenario.create(
                new ScenarioId(ids.nextId()),
                title,
                description,
                environment.toDomain(),
                new TimeLimit(timeLimitSeconds));
        scenarios.save(scenario);
        return ScenarioView.from(scenario);
    }
}