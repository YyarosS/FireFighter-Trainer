package ru.mirea.firefighter.web.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Запрос регистрации стажёра. */
public record RegisterTraineeRequest(
        @NotBlank(message = "ФИО обязательно") String fullName,
        @NotBlank(message = "Учебная группа обязательна") String studyGroup,
        @NotBlank(message = "Идентификатор VR-устройства обязателен") String vrDeviceId) {
}