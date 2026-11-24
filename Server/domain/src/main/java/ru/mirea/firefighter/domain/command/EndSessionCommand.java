package ru.mirea.firefighter.domain.command;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Команда завершения сеанса: сервер сообщает клиенту итоговые рейтинги.
 *
 * <p>Клиент не вычисляет рейтинги сам — он лишь отображает полученные
 * значения, поэтому подмена результата на стороне клиента невозможна.</p>
 */
public record EndSessionCommand(
        long sequence,
        Instant issuedAt,
        Rating speed,
        Rating accuracy,
        Rating combined) implements TrainingCommand {

    public EndSessionCommand {
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(speed, "speed");
        Objects.requireNonNull(accuracy, "accuracy");
        Objects.requireNonNull(combined, "combined");
        if (sequence < 1) {
            throw new IllegalArgumentException("Номер команды начинается с 1");
        }
    }

    @Override
    public String type() {
        return "END_SESSION";
    }
}