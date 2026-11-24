package ru.mirea.firefighter.application.port;

/**
 * Порт генерации идентификаторов.
 *
 * <p>В рабочей конфигурации — UUID, в тестах — предсказуемая
 * последовательность, что делает проверки детерминированными.</p>
 */
public interface IdGenerator {

    String nextId();
}