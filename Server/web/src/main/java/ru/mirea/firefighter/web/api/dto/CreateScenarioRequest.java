package ru.mirea.firefighter.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import ru.mirea.firefighter.application.dto.EnvironmentView;

/**
 * Запрос создания сценария.
 *
 * <p>Границы температуры, видимости и концентрации проверяет домен
 * (объект-значение {@code EnvironmentProfile}) — веб-слой не дублирует
 * бизнес-правила, а лишь принимает данные.</p>
 */
public record CreateScenarioRequest(
        @NotBlank(message = "Название сценария обязательно") String title,
        String description,
        double temperatureCelsius,
        double visibilityMeters,
        double toxinConcentrationPpm,
        double timeLimitSeconds) {

    public EnvironmentView environment() {
        return new EnvironmentView(temperatureCelsius, visibilityMeters, toxinConcentrationPpm);
    }
}