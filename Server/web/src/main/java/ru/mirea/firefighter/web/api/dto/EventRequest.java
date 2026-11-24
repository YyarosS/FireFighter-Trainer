package ru.mirea.firefighter.web.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.mirea.firefighter.application.dto.EventInput;
import ru.mirea.firefighter.domain.model.vo.EventType;

/**
 * Одно событие телеметрии.
 *
 * @param weight необязательный вес нарушения; если не передан, сервер
 *               использует вес по умолчанию для типа события
 */
public record EventRequest(
        @NotNull(message = "Тип события обязателен") EventType type,
        @NotBlank(message = "Объект события обязателен") String targetObject,
        double atSeconds,
        Double weight) {

    public EventInput toInput() {
        return new EventInput(type, targetObject, atSeconds, weight);
    }
}