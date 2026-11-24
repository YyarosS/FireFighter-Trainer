package ru.mirea.firefighter.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import ru.mirea.firefighter.domain.DomainException;
import ru.mirea.firefighter.domain.command.EndSessionCommand;
import ru.mirea.firefighter.domain.command.ShowHintCommand;
import ru.mirea.firefighter.domain.command.StartScenarioCommand;
import ru.mirea.firefighter.domain.command.TrainingCommand;
import ru.mirea.firefighter.domain.command.UpdateEnvironmentCommand;
import ru.mirea.firefighter.domain.model.vo.EnvironmentProfile;
import ru.mirea.firefighter.domain.model.vo.EventType;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.SessionStatus;
import ru.mirea.firefighter.domain.model.vo.TimeLimit;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;

/**
 * Тренировка — корень агрегата «Тренировка» (п. 2.4 отчёта).
 *
 * <p>Агрегат владеет журналом событий и журналом команд сервера и
 * защищает свои инварианты:</p>
 * <ul>
 *   <li>события принимаются только в состоянии {@code RUNNING};</li>
 *   <li>время событий не может уменьшаться (журнал монотонен);</li>
 *   <li>завершить сеанс можно ровно один раз;</li>
 *   <li>команда запуска выдаётся первой и ровно однажды.</li>
 * </ul>
 *
 * <p>В процедурной реализации эти правила проверялись в вызывающем коде
 * (или не проверялись вовсе): здесь они являются частью модели, поэтому
 * нарушить их через публичный API невозможно.</p>
 */
public final class TrainingSession {

    private final TrainingSessionId id;
    private final TraineeId traineeId;
    private final ScenarioId scenarioId;
    private final Instant startedAt;
    private final List<TrainingEvent> events = new ArrayList<>();
    private final List<TrainingCommand> commands = new ArrayList<>();

    private SessionStatus status = SessionStatus.RUNNING;
    private Instant finishedAt;
    private long nextEventSequence = 1;
    private long nextCommandSequence = 1;

    private TrainingSession(TrainingSessionId id, TraineeId traineeId,
                            ScenarioId scenarioId, Instant startedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.traineeId = Objects.requireNonNull(traineeId, "traineeId");
        this.scenarioId = Objects.requireNonNull(scenarioId, "scenarioId");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt");
    }

    /** Начать новую тренировку стажёра по выбранному сценарию. */
    public static TrainingSession start(TrainingSessionId id, TraineeId traineeId,
                                        ScenarioId scenarioId, Instant startedAt) {
        return new TrainingSession(id, traineeId, scenarioId, startedAt);
    }

    // ------------------------------------------------------------------ события

    /**
     * Зарегистрировать действие стажёра.
     *
     * @throws DomainException если тренировка уже завершена
     * @throws IllegalArgumentException если время события меньше предыдущего
     */
    public TrainingEvent recordEvent(EventType type, String targetObject,
                                     double atSeconds, double weight) {
        requireRunning("регистрировать события");
        if (atSeconds < lastEventTime()) {
            throw new IllegalArgumentException(
                    "Время события (%s с) не может быть меньше времени предыдущего (%s с)"
                            .formatted(atSeconds, lastEventTime()));
        }
        TrainingEvent event = new TrainingEvent(
                nextEventSequence++, type, targetObject, atSeconds, weight);
        events.add(event);
        return event;
    }

    /** Время последнего события; для пустого журнала — 0. */
    public double lastEventTime() {
        return events.isEmpty() ? 0.0 : events.get(events.size() - 1).atSeconds();
    }

    /** Фактическая длительность упражнения по журналу событий (Δt, алгоритм 1). */
    public double elapsedSeconds() {
        return lastEventTime();
    }

    /** Суммарный штраф S (алгоритм 2). */
    public double totalPenalty() {
        return events.stream().mapToDouble(TrainingEvent::penaltyWeight).sum();
    }

    /** Количество ошибочных действий. */
    public long errorCount() {
        return events.stream().filter(TrainingEvent::isError).count();
    }

    public List<TrainingEvent> events() {
        return List.copyOf(events);
    }

    // ------------------------------------------------------------------ команды

    /** Первая команда сеанса: параметры среды и норматив времени. */
    public StartScenarioCommand issueStartCommand(Instant now, EnvironmentProfile environment, TimeLimit timeLimit) {
        requireRunning("выдать команду запуска");
        if (!commands.isEmpty()) {
            throw new DomainException("Команда запуска уже выдана");
        }
        StartScenarioCommand command =
                new StartScenarioCommand(nextCommandSequence++, now, scenarioId, environment, timeLimit);
        commands.add(command);
        return command;
    }

    /** Изменение условий среды по решению сервера (например, инструктора). */
    public UpdateEnvironmentCommand issueEnvironmentCommand(Instant now, EnvironmentProfile environment) {
        requireRunning("изменять параметры среды");
        UpdateEnvironmentCommand command =
                new UpdateEnvironmentCommand(nextCommandSequence++, now, environment);
        commands.add(command);
        return command;
    }

    /** Подсказка обучаемому. */
    public ShowHintCommand issueHintCommand(Instant now, String text) {
        requireRunning("выдавать подсказку");
        ShowHintCommand command = new ShowHintCommand(nextCommandSequence++, now, text);
        commands.add(command);
        return command;
    }

    /** Финальная команда: итоговые рейтинги. Выдаётся только после завершения сеанса. */
    public EndSessionCommand issueEndCommand(Instant now, Evaluation evaluation) {
        if (status != SessionStatus.FINISHED) {
            throw new DomainException("Итоговая команда выдаётся только после завершения тренировки");
        }
        if (!evaluation.sessionId().equals(id)) {
            throw new DomainException("Оценка относится к другой тренировке");
        }
        EndSessionCommand command = new EndSessionCommand(
                nextCommandSequence++,
                now,
                evaluation.speed(),
                evaluation.accuracy(),
                evaluation.combined());
        commands.add(command);
        return command;
    }

    /** Текущие условия среды: последняя команда изменения или команда запуска. */
    public EnvironmentProfile currentEnvironment() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            TrainingCommand command = commands.get(i);
            if (command instanceof UpdateEnvironmentCommand update) {
                return update.environment();
            }
            if (command instanceof StartScenarioCommand start) {
                return start.environment();
            }
        }
        throw new DomainException("Сеанс не содержит команды запуска сценария");
    }

    /** Команды с номером больше {@code afterSequence} — для догоняющей синхронизации клиента. */
    public List<TrainingCommand> commandsAfter(long afterSequence) {
        return commands.stream()
                .filter(command -> command.sequence() > afterSequence)
                .toList();
    }

    public long lastCommandSequence() {
        return nextCommandSequence - 1;
    }

    // ------------------------------------------------------------------ завершение

    /** Завершить тренировку. Повторное завершение запрещено. */
    public void finish(Instant at) {
        requireRunning("завершить тренировку");
        if (at.isBefore(startedAt)) {
            throw new DomainException("Время завершения не может быть раньше времени начала");
        }
        this.status = SessionStatus.FINISHED;
        this.finishedAt = at;
    }

    // ------------------------------------------------------------------ доступ

    public TrainingSessionId id() {
        return id;
    }

    public TraineeId traineeId() {
        return traineeId;
    }

    public ScenarioId scenarioId() {
        return scenarioId;
    }

    public Instant startedAt() {
        return startedAt;
    }

    public Optional<Instant> finishedAt() {
        return Optional.ofNullable(finishedAt);
    }

    public SessionStatus status() {
        return status;
    }

    public boolean isRunning() {
        return status == SessionStatus.RUNNING;
    }

    private void requireRunning(String action) {
        if (status != SessionStatus.RUNNING) {
            throw new DomainException("Нельзя " + action + ": тренировка уже завершена");
        }
    }
}