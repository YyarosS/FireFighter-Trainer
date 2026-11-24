package ru.mirea.firefighter.domain.port;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.vo.TrainingSessionId;

/** Порт хранения оценок тренировок. */
public interface EvaluationRepository {

    void save(Evaluation evaluation);

    Optional<Evaluation> findBySessionId(TrainingSessionId sessionId);

    /** Пакетная выборка оценок — используется таблицей лидеров и историей стажёра. */
    List<Evaluation> findBySessionIds(Collection<TrainingSessionId> sessionIds);
}