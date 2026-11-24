package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.command.EndSessionCommand;
import ru.mirea.firefighter.domain.command.ShowHintCommand;
import ru.mirea.firefighter.domain.command.StartScenarioCommand;
import ru.mirea.firefighter.domain.command.TrainingCommand;
import ru.mirea.firefighter.domain.command.UpdateEnvironmentCommand;

/**
 * Команда сервера в формате JSON.
 *
 * <p>Поля, не относящиеся к типу команды, равны {@code null} и не
 * сериализуются. Разбор на клиенте выполняется по полю {@code type}.</p>
 */
public record CommandView(
        long sequence,
        String type,
        String issuedAt,
        String scenarioId,
        Double timeLimitSeconds,
        EnvironmentView environment,
        String hint,
        Double speed,
        Double accuracy,
        Double combined) {

    public static CommandView from(TrainingCommand command) {
        return switch (command) {
            case StartScenarioCommand start -> new CommandView(
                    start.sequence(), start.type(), start.issuedAt().toString(),
                    start.scenarioId().value(), start.timeLimit().seconds(),
                    EnvironmentView.from(start.environment()), null, null, null, null);
            case UpdateEnvironmentCommand update -> new CommandView(
                    update.sequence(), update.type(), update.issuedAt().toString(),
                    null, null, EnvironmentView.from(update.environment()), null, null, null, null);
            case ShowHintCommand hint -> new CommandView(
                    hint.sequence(), hint.type(), hint.issuedAt().toString(),
                    null, null, null, hint.text(), null, null, null);
            case EndSessionCommand end -> new CommandView(
                    end.sequence(), end.type(), end.issuedAt().toString(),
                    null, null, null, null,
                    end.speed().value(), end.accuracy().value(), end.combined().value());
        };
    }
}