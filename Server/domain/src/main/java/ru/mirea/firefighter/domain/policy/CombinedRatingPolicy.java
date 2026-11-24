package ru.mirea.firefighter.domain.policy;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.Rating;
import ru.mirea.firefighter.domain.model.vo.RatingWeights;

/**
 * Алгоритм 3 (п. 2.5 отчёта): комбинированный рейтинг.
 *
 * <pre>R = w_V · V + w_A · A</pre>
 *
 * <p>Нормализация весов инкапсулирована в {@link RatingWeights}, поэтому
 * политика занимается только вычислением. Сложность O(1).</p>
 */
public final class CombinedRatingPolicy {

    private final RatingWeights weights;

    public CombinedRatingPolicy(RatingWeights weights) {
        this.weights = Objects.requireNonNull(weights, "weights");
    }

    /** Скомбинировать два рейтинга с учётом весов. */
    public Rating combine(Rating speed, Rating accuracy) {
        Objects.requireNonNull(speed, "speed");
        Objects.requireNonNull(accuracy, "accuracy");

        double combined = weights.normalizedSpeedWeight() * speed.value()
                + weights.normalizedAccuracyWeight() * accuracy.value();
        return Rating.clamp(combined);
    }

    public RatingWeights weights() {
        return weights;
    }
}