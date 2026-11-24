package ru.mirea.firefighter.application.dto;

/** Строка таблицы лидеров с занятым местом. */
public record LeaderboardRow(
        int place,
        String traineeName,
        String scenarioTitle,
        double speed,
        double accuracy,
        double combined,
        double elapsedSeconds,
        String finishedAt) {
}