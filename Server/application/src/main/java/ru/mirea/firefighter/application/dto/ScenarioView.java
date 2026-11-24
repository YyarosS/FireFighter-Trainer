package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.Scenario;

/** Представление сценария для REST API и каталога в VR-клиенте. */
public record ScenarioView(
        String id,
        String title,
        String description,
        EnvironmentView environment,
        double timeLimitSeconds) {

    public static ScenarioView from(Scenario scenario) {
        return new ScenarioView(
                scenario.id().value(),
                scenario.title(),
                scenario.description(),
                EnvironmentView.from(scenario.environment()),
                scenario.timeLimit().seconds());
    }
}