package ru.mirea.firefighter.domain.command;

import java.time.Instant;
import java.util.Objects;

/** Команда показа подсказки обучаемому (решение принимает сервер, а не клиент). */
public record ShowHintCommand(
        long sequence,
        Instant issuedAt,
        String text) implements TrainingCommand {

    public ShowHintCommand {
        Objects.requireNonNull(issuedAt, "issuedAt");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Текст подсказки не может быть пустым");
        }
        if (sequence < 1) {
            throw new IllegalArgumentException("Номер команды начинается с 1");
        }
    }

    @Override
    public String type() {
        return "SHOW_HINT";
    }
}