package ru.mirea.firefighter.application.port;

import java.time.Instant;

/**
 * Порт источника времени.
 *
 * <p>Сценарии использования не вызывают {@code Instant.now()} напрямую:
 * это делает время управляемым в тестах (фиксированные часы) и не
 * привязывает приложение к системным часам.</p>
 */
public interface Clock {

    Instant now();
}