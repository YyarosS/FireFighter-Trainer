package ru.mirea.firefighter.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mirea.firefighter.application.port.Clock;
import ru.mirea.firefighter.application.port.IdGenerator;
import ru.mirea.firefighter.application.usecase.CreateScenarioService;
import ru.mirea.firefighter.application.usecase.FinishTrainingService;
import ru.mirea.firefighter.application.usecase.GetCommandsService;
import ru.mirea.firefighter.application.usecase.GetLeaderboardService;
import ru.mirea.firefighter.application.usecase.GetSessionStateService;
import ru.mirea.firefighter.application.usecase.GetTraineeHistoryService;
import ru.mirea.firefighter.application.usecase.ListScenariosService;
import ru.mirea.firefighter.application.usecase.RecordEventService;
import ru.mirea.firefighter.application.usecase.RegisterTraineeService;
import ru.mirea.firefighter.application.usecase.StartTrainingService;
import ru.mirea.firefighter.application.usecase.UpdateEnvironmentService;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;
import ru.mirea.firefighter.domain.policy.AccuracyRatingPolicy;
import ru.mirea.firefighter.domain.policy.CombinedRatingPolicy;
import ru.mirea.firefighter.domain.policy.EvaluationService;
import ru.mirea.firefighter.domain.policy.LeaderboardPolicy;
import ru.mirea.firefighter.domain.policy.SpeedRatingPolicy;
import ru.mirea.firefighter.domain.port.EvaluationRepository;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;
import ru.mirea.firefighter.infrastructure.id.UuidGenerator;
import ru.mirea.firefighter.infrastructure.persistence.InMemoryEvaluationRepository;
import ru.mirea.firefighter.infrastructure.persistence.InMemoryScenarioRepository;
import ru.mirea.firefighter.infrastructure.persistence.InMemoryTraineeRepository;
import ru.mirea.firefighter.infrastructure.persistence.InMemoryTrainingSessionRepository;
import ru.mirea.firefighter.infrastructure.time.SystemClock;

/**
 * Единственное место, где домен и приложение встречаются со Spring.
 *
 * <p>Домен ничего не знает об этом классе: зависимости передаются через
 * конструкторы (внедрение зависимостей «вручную»). Такая конфигурация
 * собирает тот же граф объектов, что и в тестах, поэтому тестируемость
 * не зависит от фреймворка.</p>
 *
 * <p>При переходе к этапу 5 достаточно заменить бины репозиториев на
 * реализации JPA — сценарии использования и домен не изменятся.</p>
 */
@Configuration
public class DomainConfig {

    // ------------------------------------------------------------ инфраструктурные порты

    @Bean
    public Clock clock() {
        return new SystemClock();
    }

    @Bean
    public IdGenerator idGenerator() {
        return new UuidGenerator();
    }

    @Bean
    public TraineeRepository traineeRepository() {
        return new InMemoryTraineeRepository();
    }

    @Bean
    public ScenarioRepository scenarioRepository() {
        return new InMemoryScenarioRepository();
    }

    @Bean
    public TrainingSessionRepository trainingSessionRepository() {
        return new InMemoryTrainingSessionRepository();
    }

    @Bean
    public EvaluationRepository evaluationRepository() {
        return new InMemoryEvaluationRepository();
    }

    // ------------------------------------------------------------ доменные правила

    /** Веса комбинированного рейтинга: 0.4 — скорость, 0.6 — точность (как на этапе 2). */
    @Bean
    public RatingWeights ratingWeights() {
        return new RatingWeights(0.4, 0.6);
    }

    @Bean
    public SpeedRatingPolicy speedRatingPolicy() {
        return new SpeedRatingPolicy();
    }

    @Bean
    public AccuracyRatingPolicy accuracyRatingPolicy() {
        return new AccuracyRatingPolicy(10.0);
    }

    @Bean
    public CombinedRatingPolicy combinedRatingPolicy(RatingWeights weights) {
        return new CombinedRatingPolicy(weights);
    }

    @Bean
    public EvaluationService evaluationService(SpeedRatingPolicy speedPolicy,
                                               AccuracyRatingPolicy accuracyPolicy,
                                               CombinedRatingPolicy combinedPolicy) {
        return new EvaluationService(speedPolicy, accuracyPolicy, combinedPolicy);
    }

    @Bean
    public LeaderboardPolicy leaderboardPolicy() {
        return new LeaderboardPolicy();
    }

    // ------------------------------------------------------------ сценарии использования

    @Bean
    public RegisterTraineeService registerTraineeService(TraineeRepository trainees, IdGenerator ids) {
        return new RegisterTraineeService(trainees, ids);
    }

    @Bean
    public CreateScenarioService createScenarioService(ScenarioRepository scenarios, IdGenerator ids) {
        return new CreateScenarioService(scenarios, ids);
    }

    @Bean
    public ListScenariosService listScenariosService(ScenarioRepository scenarios) {
        return new ListScenariosService(scenarios);
    }

    @Bean
    public StartTrainingService startTrainingService(TraineeRepository trainees,
                                                     ScenarioRepository scenarios,
                                                     TrainingSessionRepository sessions,
                                                     IdGenerator ids,
                                                     Clock clock) {
        return new StartTrainingService(trainees, scenarios, sessions, ids, clock);
    }

    @Bean
    public RecordEventService recordEventService(TrainingSessionRepository sessions) {
        return new RecordEventService(sessions);
    }

    @Bean
    public FinishTrainingService finishTrainingService(TrainingSessionRepository sessions,
                                                       ScenarioRepository scenarios,
                                                       EvaluationRepository evaluations,
                                                       EvaluationService evaluationService,
                                                       Clock clock) {
        return new FinishTrainingService(sessions, scenarios, evaluations, evaluationService, clock);
    }

    @Bean
    public GetSessionStateService getSessionStateService(TrainingSessionRepository sessions) {
        return new GetSessionStateService(sessions);
    }

    @Bean
    public GetCommandsService getCommandsService(TrainingSessionRepository sessions) {
        return new GetCommandsService(sessions);
    }

    @Bean
    public UpdateEnvironmentService updateEnvironmentService(TrainingSessionRepository sessions, Clock clock) {
        return new UpdateEnvironmentService(sessions, clock);
    }

    @Bean
    public GetLeaderboardService getLeaderboardService(TraineeRepository trainees,
                                                       ScenarioRepository scenarios,
                                                       TrainingSessionRepository sessions,
                                                       EvaluationRepository evaluations,
                                                       LeaderboardPolicy leaderboardPolicy) {
        return new GetLeaderboardService(trainees, scenarios, sessions, evaluations, leaderboardPolicy);
    }

    @Bean
    public GetTraineeHistoryService getTraineeHistoryService(TraineeRepository trainees,
                                                             ScenarioRepository scenarios,
                                                             TrainingSessionRepository sessions,
                                                             EvaluationRepository evaluations) {
        return new GetTraineeHistoryService(trainees, scenarios, sessions, evaluations);
    }
}