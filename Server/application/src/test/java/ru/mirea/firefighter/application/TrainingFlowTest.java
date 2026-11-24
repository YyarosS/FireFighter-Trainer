package ru.mirea.firefighter.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mirea.firefighter.application.dto.EventInput;
import ru.mirea.firefighter.application.dto.EnvironmentView;
import ru.mirea.firefighter.application.dto.LeaderboardRow;
import ru.mirea.firefighter.application.dto.ScenarioView;
import ru.mirea.firefighter.application.dto.SessionResult;
import ru.mirea.firefighter.application.dto.SessionStateView;
import ru.mirea.firefighter.application.dto.SessionSummary;
import ru.mirea.firefighter.application.dto.TraineeView;
import ru.mirea.firefighter.application.exception.NotFoundException;
import ru.mirea.firefighter.application.port.Clock;
import ru.mirea.firefighter.application.port.IdGenerator;
import ru.mirea.firefighter.application.usecase.CreateScenarioService;
import ru.mirea.firefighter.application.usecase.FinishTrainingService;
import ru.mirea.firefighter.application.usecase.GetCommandsService;
import ru.mirea.firefighter.application.usecase.GetLeaderboardService;
import ru.mirea.firefighter.application.usecase.GetSessionStateService;
import ru.mirea.firefighter.application.usecase.GetTraineeHistoryService;
import ru.mirea.firefighter.application.usecase.RecordEventService;
import ru.mirea.firefighter.application.usecase.RegisterTraineeService;
import ru.mirea.firefighter.application.usecase.StartTrainingService;
import ru.mirea.firefighter.domain.DomainException;
import ru.mirea.firefighter.domain.model.vo.EventType;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;
import ru.mirea.firefighter.domain.policy.AccuracyRatingPolicy;
import ru.mirea.firefighter.domain.policy.CombinedRatingPolicy;
import ru.mirea.firefighter.domain.policy.EvaluationService;
import ru.mirea.firefighter.domain.policy.LeaderboardPolicy;
import ru.mirea.firefighter.domain.policy.SpeedRatingPolicy;

/**
 * Сквозной сценарий работы приложения без Spring и без базы данных:
 * регистрация стажёра → создание сценария → старт → события → завершение
 * → таблица лидеров → история.
 *
 * <p>Числа совпадают с демонстрационным прогоном процедурной реализации
 * ({@code Main.java}): V = 27.22, A = 100, R = 70.89 для 131 с при
 * нормативе 180 с.</p>
 */
@DisplayName("Прикладной сценарий тренировки")
class TrainingFlowTest {

    private RegisterTraineeService registerTrainee;
    private CreateScenarioService createScenario;
    private StartTrainingService startTraining;
    private RecordEventService recordEvent;
    private FinishTrainingService finishTraining;
    private GetSessionStateService sessionState;
    private GetCommandsService commands;
    private GetLeaderboardService leaderboard;
    private GetTraineeHistoryService history;

    @BeforeEach
    void setUp() {
        FakeRepositories.Trainees trainees = new FakeRepositories.Trainees();
        FakeRepositories.Scenarios scenarios = new FakeRepositories.Scenarios();
        FakeRepositories.Sessions sessions = new FakeRepositories.Sessions();
        FakeRepositories.Evaluations evaluations = new FakeRepositories.Evaluations();

        Clock clock = new FixedClock();
        IdGenerator ids = new SequentialIdGenerator();

        EvaluationService evaluationService = new EvaluationService(
                new SpeedRatingPolicy(),
                new AccuracyRatingPolicy(10.0),
                new CombinedRatingPolicy(new RatingWeights(0.4, 0.6)));

        registerTrainee = new RegisterTraineeService(trainees, ids);
        createScenario = new CreateScenarioService(scenarios, ids);
        startTraining = new StartTrainingService(trainees, scenarios, sessions, ids, clock);
        recordEvent = new RecordEventService(sessions);
        finishTraining = new FinishTrainingService(sessions, scenarios, evaluations, evaluationService, clock);
        sessionState = new GetSessionStateService(sessions);
        commands = new GetCommandsService(sessions);
        leaderboard = new GetLeaderboardService(trainees, scenarios, sessions, evaluations, new LeaderboardPolicy());
        history = new GetTraineeHistoryService(trainees, scenarios, sessions, evaluations);
    }

    @Test
    @DisplayName("Полный цикл: старт, телеметрия, оценка, лидеры, история")
    void fullTrainingCycle() {
        TraineeView trainee = registerTrainee.register("Иванов И.И.", "ИКБО-30-24", "Quest-3-01");
        ScenarioView scenario = createScenario.create(
                "Возгорание в серверной",
                "Сценарий с задымлением",
                new EnvironmentView(60.0, 5.0, 120.0),
                180.0);

        SessionStateView started = startTraining.start(trainee.id(), scenario.id());
        assertThat(started.status()).isEqualTo("RUNNING");
        assertThat(started.environment().temperatureCelsius()).isEqualTo(60.0);

        assertThat(commands.commands(started.sessionId(), 0))
                .singleElement()
                .satisfies(command -> assertThat(command.type()).isEqualTo("START_SCENARIO"));

        recordEvent.record(started.sessionId(), List.of(
                new EventInput(EventType.ACTION, "extinguisher", 21.5, null),
                new EventInput(EventType.ACTION, "door", 48.0, null),
                new EventInput(EventType.ACTION, "hose", 96.4, null),
                new EventInput(EventType.ACTION, "victim", 131.0, null)));

        SessionResult result = finishTraining.finish(started.sessionId());

        assertThat(result.speed()).isCloseTo(27.2222, offset(1e-4));
        assertThat(result.accuracy()).isCloseTo(100.0, offset(1e-9));
        assertThat(result.combined()).isCloseTo(70.8889, offset(1e-4));
        assertThat(result.grade()).isEqualTo("SATISFACTORY");

        assertThat(commands.commands(started.sessionId(), 1))
                .singleElement()
                .satisfies(command -> assertThat(command.type()).isEqualTo("END_SESSION"));

        List<LeaderboardRow> rows = leaderboard.leaderboard(scenario.id(), 10);
        assertThat(rows).singleElement().satisfies(row -> {
            assertThat(row.place()).isEqualTo(1);
            assertThat(row.traineeName()).isEqualTo("Иванов И.И.");
            assertThat(row.combined()).isCloseTo(70.8889, offset(1e-4));
        });

        List<SessionSummary> summaries = history.history(trainee.id());
        assertThat(summaries).singleElement().satisfies(summary -> {
            assertThat(summary.status()).isEqualTo("FINISHED");
            assertThat(summary.combined()).isCloseTo(70.8889, offset(1e-4));
        });
    }

    @Test
    @DisplayName("Повторное завершение и события после финиша отклоняются")
    void finishedSessionRejectsFurtherActions() {
        TraineeView trainee = registerTrainee.register("Петров П.П.", "ИКБО-30-24", "Quest-3-02");
        ScenarioView scenario = createScenario.create(
                "Задымление в актовом зале", "", new EnvironmentView(40.0, 10.0, 80.0), 240.0);
        SessionStateView started = startTraining.start(trainee.id(), scenario.id());
        recordEvent.record(started.sessionId(),
                List.of(new EventInput(EventType.ACTION, "door", 30.0, null)));
        finishTraining.finish(started.sessionId());

        assertThatThrownBy(() -> finishTraining.finish(started.sessionId()))
                .isInstanceOf(DomainException.class);
        assertThatThrownBy(() -> recordEvent.record(started.sessionId(),
                List.of(new EventInput(EventType.ACTION, "door", 40.0, null))))
                .isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("Неизвестный идентификатор даёт NotFoundException")
    void unknownSessionIsNotFound() {
        assertThatThrownBy(() -> sessionState.state("missing"))
                .isInstanceOf(NotFoundException.class);
    }

    /** Часы, возвращающие фиксированный момент: тесты детерминированы. */
    private static final class FixedClock implements Clock {

        @Override
        public Instant now() {
            return Instant.parse("2026-10-02T10:00:00Z");
        }
    }

    /** Генератор предсказуемых идентификаторов. */
    private static final class SequentialIdGenerator implements IdGenerator {

        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public String nextId() {
            return "id-" + counter.incrementAndGet();
        }
    }
}