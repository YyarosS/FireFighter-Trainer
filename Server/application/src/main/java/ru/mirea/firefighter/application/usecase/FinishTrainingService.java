package ru.mirea.firefighter.application.usecase;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.application.dto.SessionResult;
import ru.mirea.firefighter.application.port.Clock;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.policy.EvaluationService;
import ru.mirea.firefighter.domain.port.EvaluationRepository;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Завершить тренировку и рассчитать оценку».
 *
 * <p>Единственное место, где встречаются агрегат, доменный сервис оценки
 * и хранилища. Порядок шагов важен: сначала завершается агрегат (после
 * этого события запрещены), затем считается оценка, затем выдаётся
 * итоговая команда клиенту.</p>
 */
public final class FinishTrainingService {

    private final TrainingSessionRepository sessions;
    private final ScenarioRepository scenarios;
    private final EvaluationRepository evaluations;
    private final EvaluationService evaluationService;
    private final Clock clock;

    public FinishTrainingService(TrainingSessionRepository sessions,
                                 ScenarioRepository scenarios,
                                 EvaluationRepository evaluations,
                                 EvaluationService evaluationService,
                                 Clock clock) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
        this.evaluationService = Objects.requireNonNull(evaluationService, "evaluationService");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SessionResult finish(String sessionId) {
        TrainingSession session = EntityLookup.session(sessions, sessionId);
        Scenario scenario = EntityLookup.scenario(scenarios, session.scenarioId());

        Instant now = clock.now();
        session.finish(now);

        Evaluation evaluation = evaluationService.evaluate(session, scenario);
        session.issueEndCommand(now, evaluation);

        evaluations.save(evaluation);
        sessions.save(session);

        return SessionResult.from(evaluation);
    }
}