package ru.mirea.firefighter.domain.policy;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Алгоритм 2 (п. 2.5 отчёта): рейтинг точности.
 *
 * <pre>A = 100 · max(0, 1 − S / S_max)</pre>
 *
 * <p>Сумма штрафов S вычисляется агрегатом ({@code totalPenalty()}),
 * причём вклад каждого события определяет сам тип события. Политика
 * не знает перечня «ошибочных» типов — она лишь нормирует сумму,
 * поэтому новый тип ошибки не требует изменения этого класса.</p>
 */
public final class AccuracyRatingPolicy implements SessionRatingPolicy {

    private final double maxPenalty;

    /**
     * @param maxPenalty порог S_max, при котором точность обнуляется
     */
    public AccuracyRatingPolicy(double maxPenalty) {
        if (Double.isNaN(maxPenalty) || maxPenalty <= 0.0) {
            throw new IllegalArgumentException("S_max должен быть положительным");
        }
        this.maxPenalty = maxPenalty;
    }

    @Override
    public Rating evaluate(TrainingSession session, Scenario scenario) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(scenario, "scenario");

        double penalty = session.totalPenalty();
        return Rating.clamp(100.0 * Math.max(0.0, 1.0 - penalty / maxPenalty));
    }

    public double maxPenalty() {
        return maxPenalty;
    }
}