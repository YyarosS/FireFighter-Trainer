package ru.mirea.firefighter.domain.command;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;

/**
 * Команда запуска сценария: клиент получает параметры среды и норматив
 * времени, с которыми сервер будет оценивать тренировку.
 */
public record StartScenarioCommand(
        long sequence,
        Instant issuedAt,
        ScenarioId scenarioId,
        EnvironmentProfile environment,
        TimeLimit timeLimit) implements TrainingCommand {

    public StartScenarioCommand {
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(scenarioId, "scenarioId");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(timeLimit, "timeLimit");
        if (sequence < 1) {
            throw new IllegalArgumentException("Номер команды начинается с 1");
        }
    }

    @Override
    public String type() {
        return "START_SCENARIO";
    }
}