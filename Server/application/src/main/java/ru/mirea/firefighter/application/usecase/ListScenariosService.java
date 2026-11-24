package ru.mirea.firefighter.application.usecase;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import ru.mirea.firefighter.application.dto.ScenarioView;
import ru.mirea.firefighter.domain.port.ScenarioRepository;

/** Сценарий использования «Показать каталог сценариев». */
public final class ListScenariosService {

    private final ScenarioRepository scenarios;

    public ListScenariosService(ScenarioRepository scenarios) {
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
    }

    public List<ScenarioView> list() {
        return scenarios.findAll().stream()
                .sorted(Comparator.comparing(scenario -> scenario.title()))
                .map(ScenarioView::from)
                .toList();
    }
}