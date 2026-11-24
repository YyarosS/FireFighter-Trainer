package ru.mirea.firefighter.domain.model.vo;

/**
 * Рейтинг тренировки — значение на отрезке [0; 100].
 *
 * <p>Объект-значение: неизменяем, проверяет собственный диапазон
 * в конструкторе, поэтому ни один алгоритм не может вернуть
 * «рейтинг −5» или «рейтинг 120». Соответствует алгоритмам 1–3
 * (п. 2.5 отчёта), где результат ограничивается функцией clamp.</p>
 *
 * @param value числовое значение рейтинга
 */
public record Rating(double value) {

    public static final Rating ZERO = new Rating(0.0);
    public static final Rating MAX = new Rating(100.0);

    public Rating {
        if (Double.isNaN(value) || value < 0.0 || value > 100.0) {
            throw new IllegalArgumentException(
                    "Рейтинг должен принадлежать отрезку [0; 100], получено: " + value);
        }
    }

    /** Ограничение произвольного числа отрезком [0; 100] — аналог clamp из процедурной версии. O(1). */
    public static Rating clamp(double raw) {
        if (Double.isNaN(raw)) {
            return ZERO;
        }
        if (raw < 0.0) {
            return ZERO;
        }
        if (raw > 100.0) {
            return MAX;
        }
        return new Rating(raw);
    }

    /** Доля от максимума: 42.0 → 0.42. Нужна для взвешенной суммы (алгоритм 3). */
    public double fraction() {
        return value / 100.0;
    }

    public boolean isAtLeast(double threshold) {
        return value >= threshold;
    }
}