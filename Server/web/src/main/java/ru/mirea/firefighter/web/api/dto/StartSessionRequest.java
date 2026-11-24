package ru.mirea.firefighter.web.api.dto;

import jakarta.validation.constraints.NotBlank;

/** Запрос запуска сеанса тренировки. */
public record StartSessionRequest(
        @NotBlank(message = "Идентификатор стажёра обязателен") String traineeId,
        @NotBlank(message = "Идентификатор сценария обязателен") String scenarioId) {
}