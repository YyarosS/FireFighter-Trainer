package ru.mirea.firefighter.domain.policy;

import ru.mirea.firefighter.domain.model.Scenario;
import ru.mirea.firefighter.domain.model.TrainingSession;
import ru.mirea.firefighter.domain.model.vo.Rating;

/**
 * Политика расчёта одного рейтинга тренировки.
 *
 * <p>Точка расширения: чтобы добавить новый вид оценки (например,
 * «рейтинг безопасности» или «рейтинг командной работы»), достаточно
 * добавить класс, реализующий этот интерфейс, не изменяя существующие
 * политики и сервис оценки. В процедурной версии аналогичное изменение
 * требовало правки функции {@code evaluateSession} и её сигнатуры.</p>
 */
public interface SessionRatingPolicy {

    /**
     * Рассчитать рейтинг по журналу выполненной тренировки.
     *
     * @param session тренировка (может быть завершённой или идущей)
     * @param scenario сценарий, задающий норматив и условия
     * @return рейтинг в диапазоне [0; 100]
     */
    Rating evaluate(TrainingSession session, Scenario scenario);
}