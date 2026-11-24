package ru.mirea.firefighter.domain.model.vo;

/**
 * Параметры моделируемой среды сценария (сущность «Сценарий», п. 2.4 отчёта).
 *
 * <p>Объект-значение с проверкой физически осмысленных границ: температура
 * от −50 до 150 °C, видимость от 0 до 1000 м, концентрация токсинов
 * от 0 до 10 000 ppm. Благодаря этому сценарий нельзя создать
 * с отрицательной видимостью.</p>
 */
public record EnvironmentProfile(
        double temperatureCelsius,
        double visibilityMeters,
        double toxinConcentrationPpm) {

    public EnvironmentProfile {
        requireRange(temperatureCelsius, -50.0, 150.0, "Температура, °C");
        requireRange(visibilityMeters, 0.0, 1000.0, "Видимость, м");
        requireRange(toxinConcentrationPpm, 0.0, 10_000.0, "Концентрация токсинов, ppm");
    }

    private static void requireRange(double value, double min, double max, String field) {
        if (Double.isNaN(value) || value < min || value > max) {
            throw new IllegalArgumentException(
                    "%s должна быть в диапазоне [%s; %s], получено: %s".formatted(field, min, max, value));
        }
    }
}