package ru.mirea.firefighter.domain.model;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;

/**
 * Сценарий тренировки — каталог упражнений с параметрами среды
 * (сущность «Сценарий», п. 2.4 отчёта).
 *
 * <p>Параметры среды и норматив времени неизменяемы в рамках сеанса:
 * если инструктору нужно изменить условия, это делается командами
 * {@code UpdateEnvironmentCommand}, а не правкой каталога.</p>
 */
public final class Scenario {

    private final ScenarioId id;
    private final String title;
    private final String description;
    private final EnvironmentProfile environment;
    private final TimeLimit timeLimit;

    private Scenario(ScenarioId id, String title, String description,
                     EnvironmentProfile environment, TimeLimit timeLimit) {
        this.id = Objects.requireNonNull(id, "id");
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название сценария не может быть пустым");
        }
        this.title = title.strip();
        this.description = description == null ? "" : description.strip();
        this.environment = Objects.requireNonNull(environment, "environment");
        this.timeLimit = Objects.requireNonNull(timeLimit, "timeLimit");
    }

    public static Scenario create(ScenarioId id, String title, String description,
                                  EnvironmentProfile environment, TimeLimit timeLimit) {
        return new Scenario(id, title, description, environment, timeLimit);
    }

    public ScenarioId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public EnvironmentProfile environment() {
        return environment;
    }

    public TimeLimit timeLimit() {
        return timeLimit;
    }
}