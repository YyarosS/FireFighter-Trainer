package ru.mirea.firefighter.domain.policy;

import java.time.Instant;
import java.util.Objects;
import ru.mirea.firefighter.domain.DomainException;
import ru.mirea.firefighter.domain.model.Evaluation;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Доменный сервис оценки тренировки: координирует политики и собирает
 * итоговую сущность {@link Evaluation}.
 *
 * <p>Сервис не содержит формул — только композицию. Каждая формула живёт
 * в своей политике, поэтому их можно проверять и заменять по отдельности.</p>
 */
public final class EvaluationService {

    private final SessionRatingPolicy speedPolicy;
    private final SessionRatingPolicy accuracyPolicy;
    private final CombinedRatingPolicy combinedPolicy;

    public EvaluationService(SessionRatingPolicy speedPolicy,
                             SessionRatingPolicy accuracyPolicy,
                             CombinedRatingPolicy combinedPolicy) {
        this.speedPolicy = Objects.requireNonNull(speedPolicy, "speedPolicy");
        this.accuracyPolicy = Objects.requireNonNull(accuracyPolicy, "accuracyPolicy");
        this.combinedPolicy = Objects.requireNonNull(combinedPolicy, "combinedPolicy");
    }

    /**
     * Рассчитать оценку завершённой тренировки.
     *
     * @throws DomainException если тренировка ещё не завершена
     */
    public Evaluation evaluate(TrainingSession session, Scenario scenario) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(scenario, "scenario");

        Instant computedAt = session.finishedAt()
                .orElseThrow(() -> new DomainException("Нельзя оценить незавершённую тренировку"));

        Rating speed = speedPolicy.evaluate(session, scenario);
        Rating accuracy = accuracyPolicy.evaluate(session, scenario);
        Rating combined = combinedPolicy.combine(speed, accuracy);

        return new Evaluation(
                session.id(),
                speed,
                accuracy,
                combined,
                combinedPolicy.weights(),
                session.elapsedSeconds(),
                computedAt);
    }
}