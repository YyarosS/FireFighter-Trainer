package ru.mirea.firefighter.application.dto;

import ru.mirea.firefighter.domain.model.vo.EventType;

/**
 * Событие, пришедшее от VR-клиента.
 *
 * <p>Клиент присылает только факт: тип, объект и время. Штрафной вес
 * необязателен — если он не задан, сервер берёт вес по умолчанию из типа
 * события, поэтому клиент не может «обнулить» штраф.</p>
 *
 * @param weight вес нарушения или {@code null}, если используется значение по умолчанию
 */
public record EventInput(
        EventType type,
        String targetObject,
        double atSeconds,
        Double weight) {

    public double effectiveWeight() {
        return weight != null ? weight : type.defaultWeight();
    }
}