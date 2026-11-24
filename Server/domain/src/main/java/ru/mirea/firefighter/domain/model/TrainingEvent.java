package ru.mirea.firefighter.domain.model;

import java.util.Objects;
import ru.mirea.firefighter.domain.model.vo.EventType;

/**
 * Событие журнала тренировки (сущность «Событие», п. 2.4 отчёта).
 *
 * <p>Неизменяемая запись: после регистрации событие нельзя исправить —
 * журнал является доказательной базой оценки действий стажёра.
 * Штрафной вклад события вычисляет сам объект ({@link #penaltyWeight()}),
 * а не внешний алгоритм, который перебирает типы событий.</p>
 *
 * @param sequence     порядковый номер события в сеансе (с 1)
 * @param type         тип события
 * @param targetObject объект сцены, с которым взаимодействовал стажёр
 * @param atSeconds    время события относительно начала сеанса, с
 * @param weight       вес нарушения; для корректных действий игнорируется
 */
public record TrainingEvent(
        long sequence,
        EventType type,
        String targetObject,
        double atSeconds,
        double weight) {

    public TrainingEvent {
        Objects.requireNonNull(type, "type");
        if (targetObject == null || targetObject.isBlank()) {
            throw new IllegalArgumentException("Объект события не может быть пустым");
        }
        if (sequence < 1) {
            throw new IllegalArgumentException("Номер события начинается с 1");
        }
        if (Double.isNaN(atSeconds) || atSeconds < 0.0) {
            throw new IllegalArgumentException("Время события не может быть отрицательным: " + atSeconds);
        }
        if (Double.isNaN(weight) || weight < 0.0) {
            throw new IllegalArgumentException("Вес события не может быть отрицательным: " + weight);
        }
    }

    /** Учитывается ли событие алгоритмом точности (алгоритм 2). */
    public boolean isError() {
        return type.isError();
    }

    /** Штрафной вклад события в сумму S; для корректных действий равен нулю. */
    public double penaltyWeight() {
        return isError() ? weight : 0.0;
    }
}