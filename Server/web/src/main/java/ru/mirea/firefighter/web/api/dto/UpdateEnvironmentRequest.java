package ru.mirea.firefighter.web.api.dto;

import ru.mirea.firefighter.application.dto.EnvironmentView;

/** Запрос изменения параметров среды в ходе тренировки (решение инструктора). */
public record UpdateEnvironmentRequest(
        double temperatureCelsius,
        double visibilityMeters,
        double toxinConcentrationPpm) {

    public EnvironmentView toView() {
        return new EnvironmentView(temperatureCelsius, visibilityMeters, toxinConcentrationPpm);
    }
}