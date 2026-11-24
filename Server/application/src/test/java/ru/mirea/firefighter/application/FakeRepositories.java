package ru.mirea.firefighter.application;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.Trainee;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;
import ru.mirea.firefighter.domain.port.EvaluationRepository;
import ru.mirea.firefighter.domain.port.ScenarioRepository;
import ru.mirea.firefighter.domain.port.TraineeRepository;
import ru.mirea.firefighter.domain.port.TrainingSessionRepository;

/**
 * Ручные тестовые дублёры хранилищ.
 *
 * <p>Модуль {@code application} не зависит от {@code infrastructure},
 * поэтому в его тестах используются собственные простые реализации портов,
 * а не боевые классы. Это заодно доказывает, что сценарии использования
 * действительно не привязаны к конкретному хранилищу.</p>
 */
final class FakeRepositories {

    private FakeRepositories() {
    }

    static final class Trainees implements TraineeRepository {

        private final Map<TraineeId, Trainee> storage = new LinkedHashMap<>();

        @Override
        public void save(Trainee trainee) {
            storage.put(trainee.id(), trainee);
        }

        @Override
        public Optional<Trainee> findById(TraineeId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Trainee> findAll() {
            return List.copyOf(storage.values());
        }

        @Override
        public boolean existsById(TraineeId id) {
            return storage.containsKey(id);
        }
    }

    static final class Scenarios implements ScenarioRepository {

        private final Map<ScenarioId, Scenario> storage = new LinkedHashMap<>();

        @Override
        public void save(Scenario scenario) {
            storage.put(scenario.id(), scenario);
        }

        @Override
        public Optional<Scenario> findById(ScenarioId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Scenario> findAll() {
            return List.copyOf(storage.values());
        }
    }

    static final class Sessions implements TrainingSessionRepository {

        private final Map<TrainingSessionId, TrainingSession> storage = new ConcurrentHashMap<>();

        @Override
        public void save(TrainingSession session) {
            storage.put(session.id(), session);
        }

        @Override
        public Optional<TrainingSession> findById(TrainingSessionId id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<TrainingSession> findByTraineeId(TraineeId traineeId) {
            return storage.values().stream()
                    .filter(session -> session.traineeId().equals(traineeId))
                    .toList();
        }

        @Override
        public List<TrainingSession> findByScenarioId(ScenarioId scenarioId) {
            return storage.values().stream()
                    .filter(session -> session.scenarioId().equals(scenarioId))
                    .toList();
        }
    }

    static final class Evaluations implements EvaluationRepository {

        private final Map<TrainingSessionId, Evaluation> storage = new ConcurrentHashMap<>();

        @Override
        public void save(Evaluation evaluation) {
            storage.put(evaluation.sessionId(), evaluation);
        }

        @Override
        public Optional<Evaluation> findBySessionId(TrainingSessionId sessionId) {
            return Optional.ofNullable(storage.get(sessionId));
        }

        @Override
        public List<Evaluation> findBySessionIds(Collection<TrainingSessionId> sessionIds) {
            return sessionIds.stream()
                    .map(storage::get)
                    .filter(evaluation -> evaluation != null)
                    .toList();
        }
    }
}