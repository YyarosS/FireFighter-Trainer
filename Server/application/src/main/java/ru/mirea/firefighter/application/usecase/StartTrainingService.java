package ru.mirea.firefighter.application.usecase;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.application.dto.SessionStateView;
import ru.mirea.firefighter.application.port.Clock;
import ru.mirea.firefighter.application.port.IdGenerator;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Начать тренировку».
 *
 * <p>Сервер проверяет существование стажёра и сценария, создаёт агрегат
 * и сразу выдаёт первую команду — параметры среды и норматив времени.
 * Клиент не выбирает условия сам, он их получает.</p>
 */
public final class StartTrainingService {

    private final TraineeRepository trainees;
    private final ScenarioRepository scenarios;
    private final TrainingSessionRepository sessions;
    private final IdGenerator ids;
    private final Clock clock;

    public StartTrainingService(TraineeRepository trainees,
                                ScenarioRepository scenarios,
                                TrainingSessionRepository sessions,
                                IdGenerator ids,
                                Clock clock) {
        this.trainees = Objects.requireNonNull(trainees, "trainees");
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.ids = Objects.requireNonNull(ids, "ids");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SessionStateView start(String traineeId, String scenarioId) {
        Trainee trainee = EntityLookup.trainee(trainees, new TraineeId(traineeId));
        Scenario scenario = EntityLookup.scenario(scenarios, new ScenarioId(scenarioId));

        Instant now = clock.now();
        TrainingSession session = TrainingSession.start(
                new TrainingSessionId(ids.nextId()),
                trainee.id(),
                scenario.id(),
                now);
        session.issueStartCommand(now, scenario.environment(), scenario.timeLimit());
        sessions.save(session);

        return SessionStateView.from(session);
    }
}