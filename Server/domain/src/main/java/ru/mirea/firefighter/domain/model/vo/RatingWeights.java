package ru.mirea.firefighter.domain.model.vo;

/**
 * Веса комбинированного рейтинга (алгоритм 3, п. 2.5 отчёта).
 *
 * <p>Нормализация весов — обязанность самого объекта-значения, а не
 * вызывающего кода: снаружи нельзя получить сумму весов, отличную от 1.
 * Именно эта ответственность в процедурной версии была «размазана»
 * по функции {@code combinedRating}.</p>
 */
public record RatingWeights(double speedWeight, double accuracyWeight) {

    public RatingWeights {
        if (Double.isNaN(speedWeight) || Double.isNaN(accuracyWeight)
                || speedWeight < 0.0 || accuracyWeight < 0.0) {
            throw new IllegalArgumentException("Веса рейтингов не могут быть отрицательными");
        }
        if (speedWeight + accuracyWeight <= 0.0) {
            throw new IllegalArgumentException("Сумма весов должна быть положительной");
        }
    }

    /** Доля веса скорости после нормализации. */
    public double normalizedSpeedWeight() {
        return speedWeight / (speedWeight + accuracyWeight);
    }

    /** Доля веса точности после нормализации. */
    public double normalizedAccuracyWeight() {
        return accuracyWeight / (speedWeight + accuracyWeight);
    }
}