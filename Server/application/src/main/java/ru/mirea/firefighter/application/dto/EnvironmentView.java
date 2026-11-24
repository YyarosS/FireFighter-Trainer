package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;

/** Параметры среды в формате, отдаваемом клиенту и REST API. */
public record EnvironmentView(
        double temperatureCelsius,
        double visibilityMeters,
        double toxinConcentrationPpm) {

    public static EnvironmentView from(EnvironmentProfile profile) {
        return new EnvironmentView(
                profile.temperatureCelsius(),
                profile.visibilityMeters(),
                profile.toxinConcentrationPpm());
    }

    public EnvironmentProfile toDomain() {
        return new EnvironmentProfile(temperatureCelsius, visibilityMeters, toxinConcentrationPpm);
    }
}