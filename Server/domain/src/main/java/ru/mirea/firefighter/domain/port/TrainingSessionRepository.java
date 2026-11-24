package ru.mirea.firefighter.domain.port;

import java.util.List;
import java.util.Optional;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.ScenarioId;
import ru.mirea.firefighter.domain.model.vo.TraineeId;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;

/** Порт хранения тренировок (агрегатов вместе с журналами событий и команд). */
public interface TrainingSessionRepository {

    void save(TrainingSession session);

    Optional<TrainingSession> findById(TrainingSessionId id);

    List<TrainingSession> findByTraineeId(TraineeId traineeId);

    List<TrainingSession> findByScenarioId(ScenarioId scenarioId);
}