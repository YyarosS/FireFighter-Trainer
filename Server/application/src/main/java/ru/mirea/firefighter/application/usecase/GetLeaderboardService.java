package ru.mirea.firefighter.application.usecase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import ru.mirea.firefighter.application.dto.LeaderboardRow;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.LeaderboardEntry;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.policy.LeaderboardPolicy;
import ru.mirea.firefighter.domain.port.EvaluationRepository;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Сценарий использования «Построить таблицу лидеров по сценарию».
 *
 * <p>Сервис собирает строки из завершённых тренировок и передаёт их
 * доменной политике {@link LeaderboardPolicy} — порядок и отбор мест
 * остаются ответственностью домена, а не приложения.</p>
 */
public final class GetLeaderboardService {

    private static final int MAX_LIMIT = 100;

    private final TraineeRepository trainees;
    private final ScenarioRepository scenarios;
    private final TrainingSessionRepository sessions;
    private final EvaluationRepository evaluations;
    private final LeaderboardPolicy leaderboardPolicy;

    public GetLeaderboardService(TraineeRepository trainees,
                                 ScenarioRepository scenarios,
                                 TrainingSessionRepository sessions,
                                 EvaluationRepository evaluations,
                                 LeaderboardPolicy leaderboardPolicy) {
        this.trainees = Objects.requireNonNull(trainees, "trainees");
        this.scenarios = Objects.requireNonNull(scenarios, "scenarios");
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.evaluations = Objects.requireNonNull(evaluations, "evaluations");
        this.leaderboardPolicy = Objects.requireNonNull(leaderboardPolicy, "leaderboardPolicy");
    }

    public List<LeaderboardRow> leaderboard(String scenarioId, int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("Размер таблицы должен быть от 1 до " + MAX_LIMIT);
        }

        Scenario scenario = EntityLookup.scenario(scenarios, new ScenarioId(scenarioId));

        List<TrainingSession> finished = sessions.findByScenarioId(scenario.id()).stream()
                .filter(session -> !session.isRunning())
                .toList();

        Map<TrainingSessionId, Evaluation> evaluationsById =
                evaluations.findBySessionIds(finished.stream().map(TrainingSession::id).toList()).stream()
                        .collect(Collectors.toMap(Evaluation::sessionId, evaluation -> evaluation));

        List<LeaderboardEntry> entries = finished.stream()
                .filter(session -> evaluationsById.containsKey(session.id()))
                .map(session -> toEntry(session, evaluationsById.get(session.id()), scenario))
                .toList();

        List<LeaderboardEntry> ranked = leaderboardPolicy.rank(entries, limit);

        List<LeaderboardRow> rows = new ArrayList<>(ranked.size());
        for (int index = 0; index < ranked.size(); index++) {
            LeaderboardEntry entry = ranked.get(index);
            rows.add(new LeaderboardRow(
                    index + 1,
                    entry.traineeName(),
                    entry.scenarioTitle(),
                    entry.speed().value(),
                    entry.accuracy().value(),
                    entry.combined().value(),
                    entry.elapsedSeconds(),
                    entry.finishedAt().toString()));
        }
        return List.copyOf(rows);
    }

    private LeaderboardEntry toEntry(TrainingSession session, Evaluation evaluation, Scenario scenario) {
        Trainee trainee = EntityLookup.trainee(trainees, session.traineeId());
        return new LeaderboardEntry(
                trainee.fullName(),
                scenario.title(),
                evaluation.speed(),
                evaluation.accuracy(),
                evaluation.combined(),
                evaluation.elapsedSeconds(),
                evaluation.computedAt());
    }
}