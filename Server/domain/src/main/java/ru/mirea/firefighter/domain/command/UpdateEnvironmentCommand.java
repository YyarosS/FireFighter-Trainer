package ru.mirea.firefighter.domain.command;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;

/**
 * Команда изменения параметров среды: сервер (инструктор или модель
 * динамики пожара) задаёт новое состояние, клиент только отображает его.
 */
public record UpdateEnvironmentCommand(
        long sequence,
        Instant issuedAt,
        EnvironmentProfile environment) implements TrainingCommand {

    public UpdateEnvironmentCommand {
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(environment, "environment");
        if (sequence < 1) {
            throw new IllegalArgumentException("Номер команды начинается с 1");
        }
    }

    @Override
    public String type() {
        return "UPDATE_ENVIRONMENT";
    }
}