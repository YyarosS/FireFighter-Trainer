package ru.mirea.firefighter.application.usecase;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.mirea.firefighter.application.dto.SessionSummary;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.EvaluationRepository;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/** Сценарий использования «Показать историю тренировок стажёра». */
public final class GetTraineeHistoryService {

    private final TraineeRepository trainees;
    private final ScenarioRepository scenarios;
    private final TrainingSessionRepository sessions;
    private final EvaluationRepository evaluations;

    public GetTraineeHistoryService(TraineeRepository trainees,
                                    ScenarioRepository scenarios,
                                    TrainingSessionRepository sessions,
                                    EvaluationRepository evaluations) {
        this.trainees = Objects.requireNonNull(trainees, "trainees");
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
    }

    public List<SessionSummary> history(String traineeId) {
        Trainee trainee = EntityLookup.trainee(trainees, new TraineeId(traineeId));

        List<TrainingSession> traineeSessions = sessions.findByTraineeId(trainee.id()).stream()
                .sorted(Comparator.comparing(TrainingSession::startedAt).reversed())
                .toList();

        Map<TrainingSessionId, Evaluation> evaluationsById =
                evaluations.findBySessionIds(traineeSessions.stream().map(TrainingSession::id).toList()).stream()
                        .collect(Collectors.toMap(Evaluation::sessionId, evaluation -> evaluation));

        Map<ScenarioId, String> scenarioTitles = new HashMap<>();
        return traineeSessions.stream()
                .map(session -> new SessionSummary(
                        session.id().value(),
                        scenarioTitle(scenarioTitles, session.scenarioId()),
                        session.status().name(),
                        session.startedAt().toString(),
                        session.finishedAt().map(Object::toString).orElse(null),
                        Optional.ofNullable(evaluationsById.get(session.id()))
                                .map(evaluation -> evaluation.combined().value())
                                .orElse(null)))
                .toList();
    }

    private String scenarioTitle(Map<ScenarioId, String> cache, ScenarioId scenarioId) {
        return cache.computeIfAbsent(scenarioId, id -> scenarios.findById(id)
                .map(scenario -> scenario.title())
                .orElse("Сценарий удалён"));
    }
}