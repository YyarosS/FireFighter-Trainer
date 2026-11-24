package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.Trainee;

/** Представление стажёра для REST API. */
public record TraineeView(
        String id,
        String fullName,
        String studyGroup,
        String vrDeviceId) {

    public static TraineeView from(Trainee trainee) {
        return new TraineeView(
                trainee.id().value(),
                trainee.fullName(),
                trainee.studyGroup(),
                trainee.vrDeviceId());
    }
}