package ru.mirea.firefighter.domain.policy;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Алгоритм 1 (п. 2.5 отчёта): рейтинг скорости.
 *
 * <pre>V = 100 · clamp(1 − Δt / T_max, 0, 1)</pre>
 *
 * <p>Δt берётся из журнала событий, T_max — из сценария. Пустой журнал
 * даёт V = 0 (упражнение не выполнено). Сложность O(1): агрегат хранит
 * время последнего события, повторный проход по журналу не нужен.</p>
 */
public final class SpeedRatingPolicy implements SessionRatingPolicy {

    @Override
    public Rating evaluate(TrainingSession session, Scenario scenario) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(scenario, "scenario");

        if (session.events().isEmpty()) {
            return Rating.ZERO;
        }
        double elapsed = session.elapsedSeconds();
        double limit = scenario.timeLimit().seconds();
        return Rating.clamp(100.0 * (1.0 - elapsed / limit));
    }
}