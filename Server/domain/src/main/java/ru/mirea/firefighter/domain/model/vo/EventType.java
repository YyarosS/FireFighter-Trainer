package ru.mirea.firefighter.domain.model.vo;

/**
 * Тип события журнала тренировки (сущность «Событие», п. 2.4 отчёта).
 *
 * <p>Признак «является ошибкой» и штрафной вес по умолчанию — свойства
 * самого типа, а не внешний список {@code ERROR_EVENT_TYPES}, как было
 * в процедурной реализации. Добавление нового типа ошибки не требует
 * правки алгоритма точности.</p>
 */
public enum EventType {

    /** Корректное действие стажёра. */
    ACTION(false, 0.0),

    /** Нарушение техники безопасности. */
    SAFETY_VIOLATION(true, 2.0),

    /** Использовано неверное оборудование. */
    WRONG_EQUIPMENT(true, 1.0),

    /** Пропущен обязательный этап упражнения. */
    MISSED_STAGE(true, 3.0);

    private final boolean error;
    private final double defaultWeight;

    EventType(boolean error, double defaultWeight) {
        this.error = error;
        this.defaultWeight = defaultWeight;
    }

    /** Учитывается ли событие алгоритмом точности (алгоритм 2). */
    public boolean isError() {
        return error;
    }

    /** Штрафной вес по умолчанию, если клиент не передал собственный. */
    public double defaultWeight() {
        return defaultWeight;
    }
}