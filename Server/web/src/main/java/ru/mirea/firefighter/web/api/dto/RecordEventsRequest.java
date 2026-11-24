package ru.mirea.firefighter.web.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import ru.mirea.firefighter.application.dto.EventInput;

/** Пакет событий телеметрии, отправленный VR-клиентом. */
public record RecordEventsRequest(
        @NotEmpty(message = "Пакет событий не может быть пустым")
        @Valid List<EventRequest> events) {

    public List<EventInput> toInputs() {
        return events.stream().map(EventRequest::toInput).toList();
    }
}