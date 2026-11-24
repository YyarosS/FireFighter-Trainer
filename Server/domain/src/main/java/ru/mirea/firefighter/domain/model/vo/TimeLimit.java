package ru.mirea.firefighter.domain.model.vo;

/**
 * Предельное время выполнения сценария T_max (алгоритм 1, п. 2.5 отчёта).
 *
 * <p>Положительная величина: нулевой или отрицательный норматив делает
 * рейтинг скорости неопределённым. Проверка вынесена в объект-значение,
 * поэтому процедурная проверка {@code tDeadline <= 0} больше не дублируется
 * в каждом вызывающем методе.</p>
 */
public record TimeLimit(double seconds) {

    private static final double MAX_SECONDS = 24 * 60 * 60;

    public TimeLimit {
        if (Double.isNaN(seconds) || seconds <= 0.0 || seconds > MAX_SECONDS) {
            throw new IllegalArgumentException(
                    "Норматив времени должен быть положительным и не превышать сутки, получено: " + seconds);
        }
    }
}