package ru.mirea.firefighter.application.dto;

/**
 * Краткая запись о тренировке для истории стажёра.
 *
 * @param combined итоговый рейтинг; {@code null}, если тренировка ещё не завершена
 */
public record SessionSummary(
        String sessionId,
        String scenarioTitle,
        String status,
        String startedAt,
        String finishedAt,
        Double combined) {
}